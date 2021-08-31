package fr.nocsy.almpet.data;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Language {

    public static void sendMessage(Player p, String message)
    {
        p.sendMessage(GlobalConfig.getInstance().getPrefix() + " " + message);
    }
    public static void sendMessage(CommandSender sender, String message)
    {
        sender.sendMessage(GlobalConfig.getInstance().getPrefix() + " " + message);
    }

}
