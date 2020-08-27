package us.matrixcraft.CommandIntercept;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;

class CommandInterceptor implements Listener {
    private final CommandIntercept plugin;
    
    CommandInterceptor(CommandIntercept plugin) {
        this.plugin = plugin;
        
    }
    
    @EventHandler
    void onPlayerQuit(PlayerQuitEvent e) {
        for (CommandSender key : plugin.playerMap.keySet()) {
            if (key.equals(e.getPlayer()) || plugin.playerMap.get(key).equals(e.getPlayer()))
                plugin.playerMap.remove(key);
        }
    }
    
    @EventHandler
    void onServerCommand(ServerCommandEvent e) {
        if (plugin.playerMap.containsKey(e.getSender())) commandHandler(e.getSender(), e.getCommand(), e);
    }
    
    @EventHandler
    void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
        if (plugin.playerMap.containsKey(e.getPlayer())) commandHandler(e.getPlayer(), e.getMessage(), e);
    }
    
    void commandHandler(CommandSender sender, String command, Cancellable e) {
        if (!plugin.playerMap.containsKey(sender)) return;
        CommandSender interceptor = plugin.playerMap.get(sender);
        
        int id = plugin.storedCommands.size();
        plugin.storedCommands.add(new StoredCommand(sender, (command.charAt(0) == '/')?command.substring(1):command));
        Bukkit.getScheduler().runTaskLater(plugin, new RunCommandLater(plugin, id), 60);
        e.setCancelled(true);
        interceptor.sendMessage(
                "§6User `§c" + sender.getName() + "§6` is trying to allow the command `§r" + command + "§6`.");
        
        String commandAllow = "/allow " + id;
        String commandDeny = "/deny " + id;
        String commandDenyS = "/sdeny " + id;
        TextComponent allow = new TextComponent("Allow");
        allow.setColor(ChatColor.GREEN);
        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandAllow));
        allow.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(commandAllow).create()));
        
        
        TextComponent deny = new TextComponent("Deny");
        deny.setColor(ChatColor.RED);
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandDeny));
        deny.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(commandDeny).create()));
        
        TextComponent denyS = new TextComponent("Silent deny");
        denyS.setColor(ChatColor.RED);
        denyS.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandDenyS));
        denyS.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(commandDenyS).create()));
        
        TextComponent idC = new TextComponent("<" + id + "> ");
        idC.setColor(ChatColor.GOLD);
        TextComponent bar = new TextComponent(" | ");
        bar.setColor(ChatColor.GOLD);
        TextComponent end = new TextComponent(" command will auto run in 3 seconds.");
        end.setColor(ChatColor.GOLD);
        TextComponent message = new TextComponent(idC, allow, bar, deny, bar, denyS, end);
        
        interceptor.spigot().sendMessage(message);
        
    }
}

class StoredCommand {
    private final String command;
    private final CommandSender sender;
    private boolean executed;
    
    StoredCommand(CommandSender sender, String command) {
        this.sender = sender;
        this.command = command;
    }
    
    void allow() {
        if (sender == null || executed) return;
        Bukkit.dispatchCommand(sender, command);
        executed = true;
        
    }
    
    void deny() {
        executed = true;
    }
    
    boolean isExecuted() {
        return executed;
    }
    
    CommandSender getSender() {
        return sender;
    }
   
   /*String getBaseCommand() {
      return command.substring(0, (command.indexOf(' ')) > 0?command.indexOf(' '):command.length() - 1);
   }*/
}

class RunCommandLater implements Runnable {
    private final CommandIntercept plugin;
    private final int id;
    
    RunCommandLater(CommandIntercept plugin, int id) {
        this.plugin = plugin;
        this.id = id;
    }
    
    @Override
    public void run() {
        plugin.storedCommands.get(id).allow();
        for (StoredCommand s : plugin.storedCommands) {
            if (!s.isExecuted()) return;
        }
        plugin.storedCommands = new ArrayList<>();
    }
}