package fr.nocsy.almpet;

import fr.nocsy.almpet.commands.CommandHandler;
import fr.nocsy.almpet.data.GlobalConfig;
import fr.nocsy.almpet.data.LanguageConfig;
import fr.nocsy.almpet.data.Pet;
import fr.nocsy.almpet.data.flags.FlagsManager;
import fr.nocsy.almpet.listeners.EventListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Logger;

public class AlmPet extends JavaPlugin {

    @Getter
    private static AlmPet instance;
    @Getter
    private static Logger log = Bukkit.getLogger();

    @Getter
    private static String prefix = "§8[§»";

    @Override
    public void onEnable(){

        instance = this;
        CommandHandler.init(this);
        EventListener.init(this);

        createConfigs();
        getLog().info("-=-=-=-= AlmPet loaded =-=-=-=-");
        getLog().info("    Nocsy sur la place !");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

        try
        {
            FlagsManager.init(this);
        } catch (IllegalPluginAccessException ex)
        {
            getLog().warning("[AlmPet] : Le flag manager semble avoir rencontré une exception du type " + ex.getClass().getSimpleName());
        }

    }

    @Override
    public void onDisable(){
        getLog().info("-=-=-=-= AlmPet disable =-=-=-=-");
        getLog().info("Je sens que ça va être sex");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

        Pet.clearPets();
        FlagsManager.stopFlags();

    }

    private void createConfigs(){
        GlobalConfig.getInstance().init();
        LanguageConfig.getInstance().init();
    }

}
