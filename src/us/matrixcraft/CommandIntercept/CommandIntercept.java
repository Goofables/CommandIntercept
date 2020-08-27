package us.matrixcraft.CommandIntercept;

/**
 * Command Intercept
 * <p>
 * //TODO:
 */

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommandIntercept extends JavaPlugin {
    
    Map<CommandSender, CommandSender> playerMap = new HashMap<>();
    ArrayList<StoredCommand> storedCommands = new ArrayList<>();
    
    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(new CommandInterceptor(this), this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) return false;
        switch (command.getName().toLowerCase()) {
            case "intercept":
                if (args[0].toLowerCase().equals("off")) {
                    for (CommandSender key : playerMap.keySet()) {
                        if (playerMap.get(key).equals(sender)) playerMap.remove(key);
                    }
                    sender.sendMessage("Removed all interceptors associated with " + sender.getName());
                    return true;
                }
                CommandSender target = null;
                if (args[0].toLowerCase().equals("console")) target = getServer().getConsoleSender();
                Player targetPlayer = getServer().getPlayer(args[0]);
                if (targetPlayer != null) target = targetPlayer;
                if (target == null) {
                    sender.sendMessage("§4Error. Player `" + args[0] + "` not found!");
                    return true;
                }
                playerMap.put(target, sender);
                sender.sendMessage("§aSuccess! `" + sender.getName() + "` will now intercept all commands from `" +
                        target.getName() + "`");
                return true;
            case "allow":
            case "deny":
            case "sdeny":
                int id = 0;
                try {
                    id = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cID `" + args[0] + "` is not valid");
                    return true;
                }
                if (storedCommands.size() <= id || id < 0) {
                    sender.sendMessage("§cCommand id(" + id + ") not found!");
                    return true;
                }
                if (storedCommands.get(id).isExecuted()) {
                    sender.sendMessage("§cCommand had already been executed!");
                    return true;
                }
                if (command.getName().toLowerCase().equals("allow")) {
                    storedCommands.get(id).allow();
                    sender.sendMessage("§aAllowed command §6" + id + "§a!");
                } else {
                    storedCommands.get(id).deny();
                    if (command.getName().toLowerCase().equals("deny"))
                        storedCommands.get(id).getSender().sendMessage("§cPermission denied.");
                    sender.sendMessage("§c" + (command.getName().toLowerCase().equals("deny")?"":"Silently ") +
                            "Denied command §6" + id + "§c!");
                }
                return true;
        }
        return false;
    }
}

