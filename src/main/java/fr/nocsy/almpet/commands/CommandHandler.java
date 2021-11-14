package fr.nocsy.almpet.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CommandHandler implements CommandExecutor {

    public static ArrayList<CCommand> commands = new ArrayList<>();

    public static void init(JavaPlugin plugin) {

        commands.add(new AdvancedPetCommand());
        for (CCommand c : commands) {
            plugin.getCommand(c.getName()).setExecutor(new CommandHandler());
        }

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        for (CCommand cmd : commands) {
            if (cmd.getName().equalsIgnoreCase(command.getName())) {
                cmd.execute(sender, command, label, args);
            }
        }
        return true;
    }
}
