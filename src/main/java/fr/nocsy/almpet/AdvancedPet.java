package fr.nocsy.almpet;

import fr.nocsy.almpet.commands.CommandHandler;
import fr.nocsy.almpet.data.*;
import fr.nocsy.almpet.data.flags.FlagsManager;
import fr.nocsy.almpet.listeners.EventListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class AdvancedPet extends JavaPlugin {

    @Getter
    private static AdvancedPet instance;
    @Getter
    private static Logger log = Bukkit.getLogger();

    @Getter
    private static String prefix = "§8[§»";

    @Getter
    private static String logName = "[AdvancedPet] : ";

    @Override
    public void onEnable(){

        instance = this;
        CommandHandler.init(this);
        EventListener.init(this);

        loadConfigs();
        getLog().info("-=-=-=-= AdvancedPet loaded =-=-=-=-");
        getLog().info("        Plugin made by Nocsy");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

        try
        {
            FlagsManager.init(this);
        } catch (IllegalPluginAccessException ex)
        {
            getLog().warning(getLogName() + "Flag manager encountered an exception " + ex.getClass().getSimpleName());
        }

    }

    @Override
    public void onDisable(){
        getLog().info("-=-=-=-= AdvancedPet disable =-=-=-=-");
        getLog().info("            See you soon");
        getLog().info("-=-=-=-= -=-=-=-=-=-=- =-=-=-=-");

        Pet.clearPets();
        FlagsManager.stopFlags();

    }

    public static void loadConfigs(){
        GlobalConfig.getInstance().init();
        LanguageConfig.getInstance().init();
        PetConfig.loadPets(AbstractConfig.getPath() + "Pets/", true);
    }

}
