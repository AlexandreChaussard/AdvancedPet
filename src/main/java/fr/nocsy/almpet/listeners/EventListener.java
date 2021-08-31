package fr.nocsy.almpet.listeners;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class EventListener implements Listener {

    private static ArrayList<Listener> listeners = new ArrayList<>();

    public static void init(JavaPlugin plugin) {

        listeners.add(null);

        for (Listener l : listeners) {
            plugin.getServer().getPluginManager().registerEvents(l, plugin);
        }

    }

}
