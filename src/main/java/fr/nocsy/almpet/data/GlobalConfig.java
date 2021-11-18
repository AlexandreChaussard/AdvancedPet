package fr.nocsy.almpet.data;

import fr.nocsy.almpet.AdvancedPet;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.skills.Skill;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GlobalConfig extends AbstractConfig {

    public static GlobalConfig instance;

    @Getter
    private String prefix = "§8[§6AdvancedPet§8] »";
    @Getter
    private String defaultName = "§9Pet of %player%";
    @Getter
    private boolean nameable;
    @Getter
    private boolean mountable;
    @Getter
    private boolean rightClickToOpen;
    @Getter
    private boolean leftClickToOpen;
    @Getter
    private int distanceTeleport = 30;
    @Getter
    private int maxNameLenght = 16;

    public static GlobalConfig getInstance()
    {

        if(instance == null)
            instance = new GlobalConfig();

        return instance;
    }

    public void init()
    {
        super.init("", "config.yml");

        if(getConfig().get("Prefix") == null)
            getConfig().set("Prefix", prefix);
        if(getConfig().get("DefaultName") == null)
            getConfig().set("DefaultName", defaultName);
        if(getConfig().get("RightClickToOpenMenu") == null)
            getConfig().set("RightClickToOpenMenu", true);
        if(getConfig().get("LeftClickToOpenMenu") == null)
            getConfig().set("LeftClickToOpenMenu", true);
        if(getConfig().get("Nameable") == null)
            getConfig().set("Nameable", true);
        if(getConfig().get("Mountable") == null)
            getConfig().set("Mountable", true);
        if(getConfig().get("DistanceTeleport") == null)
            getConfig().set("DistanceTeleport", 30);
        if(getConfig().get("MaxNameLenght") == null)
            getConfig().set("MaxNameLenght", maxNameLenght);

        save();
        reload();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void reload() {

        loadConfig();

        prefix              = getConfig().getString("Prefix");
        defaultName         = getConfig().getString("DefaultName");
        rightClickToOpen    = getConfig().getBoolean("RightClickToOpenMenu");
        leftClickToOpen     = getConfig().getBoolean("LeftClickToOpenMenu");
        nameable            = getConfig().getBoolean("Nameable");
        mountable           = getConfig().getBoolean("Mountable");
        distanceTeleport    = getConfig().getInt("DistanceTeleport");
        maxNameLenght       = getConfig().getInt("MaxNameLenght");

    }

}
