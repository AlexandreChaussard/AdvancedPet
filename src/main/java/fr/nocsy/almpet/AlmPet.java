package fr.nocsy.almpet;

import fr.nocsy.almpet.commands.CommandHandler;
import fr.nocsy.almpet.listeners.EventListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AlmPet extends JavaPlugin {

    @Getter
    private static AlmPet instance;
    @Getter
    private static Logger log = Bukkit.getLogger();

    public void onEnable(){

        instance = this;
        CommandHandler.init(this);
        EventListener.init(this);

        createConfigs();
        getLog().info("-=-=-=-= AlmCosmV2 loaded =-=-=-=-");
        getLog().info("    Par votre petit Aracneon");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

    }

    public void onDisable(){
        getLog().info("-=-=-=-= AlmCosmV2 disable =-=-=-=-");
        getLog().info("En espérant qu'il marche mieux !");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");
    }


    private void createConfigs(){

    }

}
