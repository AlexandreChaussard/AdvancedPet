package fr.nocsy.almpet.data;

import fr.nocsy.almpet.AlmPet;
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
    private String prefix = "§8[§6Mascotte§8] »";
    @Getter
    private String defaultName = "§9Mascotte de %player%";
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

    @Getter
    public int howManyLoaded;

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
        if(getConfig().get("Pets") == null)
            getConfig().set("Pets", new ArrayList<String>());

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

        howManyLoaded = 0;

        prefix              = getConfig().getString("Prefix");
        defaultName         = getConfig().getString("DefaultName");
        rightClickToOpen    = getConfig().getBoolean("RightClickToOpenMenu");
        leftClickToOpen     = getConfig().getBoolean("LeftClickToOpenMenu");
        nameable            = getConfig().getBoolean("Nameable");
        mountable           = getConfig().getBoolean("Mountable");
        distanceTeleport    = getConfig().getInt("DistanceTeleport");
        maxNameLenght       = getConfig().getInt("MaxNameLenght");

        Pet.getObjectPets().clear();

        if(getConfig().getConfigurationSection("Pets") != null)
        {
            Object[] keyArray = getConfig().getConfigurationSection("Pets").getKeys(false).toArray();
            for(Object key : keyArray)
            {
                String id                   = key.toString();
                String mobType              = getConfig().getString("Pets." + key + ".MythicMob");
                String permission           = getConfig().getString("Pets." + key + ".Permission");
                int distance                = getConfig().getInt("Pets." + key + ".Distance");
                String despawnSkillName     = getConfig().getString("Pets." + key + ".DespawnSkill");
                String iconName             = getConfig().getString("Pets." + key + ".Icon.Name");
                String textureBase64        = getConfig().getString("Pets." + key + ".Icon.TextureBase64");
                List<String> description    = getConfig().getStringList("Pets." + key + ".Icon.Description");

                if( id              == null ||
                        mobType         == null ||
                        permission      == null ||
                        iconName        == null ||
                        textureBase64   == null ||
                        description     == null)
                {
                    AlmPet.getLog().warning("[AlmPet] : Impossible d'enregistrer ce pet. Veuillez vérifier le fichier de configuration");
                    AlmPet.getLog().warning("[AlmPet] : Informations sur le pet non enregistré : ");
                    AlmPet.getLog().warning("id : " + id);
                    AlmPet.getLog().warning("mobType : " + mobType);
                    AlmPet.getLog().warning("permission : " + permission);
                    continue;
                }

                Pet pet = new Pet(id);
                pet.setMythicMobName(mobType);
                pet.setPermission(permission);
                if(getConfig().get("Pets." + key + ".Mountable") == null) {
                    pet.setMountable(this.mountable);
                } else {
                    pet.setMountable(getConfig().getBoolean("Pets." + key + ".Mountable"));
                }
                pet.setDistance(distance);

                if(despawnSkillName != null)
                {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Optional<Skill> optionalSkill = MythicMobs.inst().getSkillManager().getSkill(despawnSkillName);
                            if(optionalSkill.isPresent())
                            {
                                pet.setDespawnSkill(optionalSkill.get());
                            }
                            if(pet.getDespawnSkill() == null)
                            {
                                AlmPet.getLog().warning("[AlmPet] : Impossible d'associer le despawn skill \"" + despawnSkillName + "\" au pet \"" + pet.getId() + "\", car le skill n'existe pas.");
                            }
                        }
                    }.runTaskLater(AlmPet.getInstance(), 5L);
                }

                pet.buildIcon(iconName, description, textureBase64);

                Pet.getObjectPets().add(pet);
                howManyLoaded++;
            }
        }

        AlmPet.getLog().info("[AlmPet] : " + howManyLoaded + " pets enregistrés avec succès !");
    }

}
