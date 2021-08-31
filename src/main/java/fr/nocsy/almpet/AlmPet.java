package fr.nocsy.almpet;

import fr.nocsy.almpet.commands.CommandHandler;
import fr.nocsy.almpet.data.GlobalConfig;
import fr.nocsy.almpet.data.Pet;
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

    @Getter
    private static String prefix = "§8[§»";

    public void onEnable(){

        instance = this;
        CommandHandler.init(this);
        EventListener.init(this);

        createConfigs();
        getLog().info("-=-=-=-= AlmPet loaded =-=-=-=-");
        getLog().info("    Nocsy sur la place !");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

    }

    public void onDisable(){
        getLog().info("-=-=-=-= AlmPet disable =-=-=-=-");
        getLog().info("Je sens que ça va être sex");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

        Pet.clearPets();

    }

    private void createConfigs(){
        GlobalConfig.getInstance().init();
    }

}
