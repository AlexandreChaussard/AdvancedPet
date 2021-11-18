package fr.nocsy.almpet.data;

import fr.nocsy.almpet.AdvancedPet;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.skills.Skill;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PetConfig extends AbstractConfig {

    @Getter
    private Pet pet = null;

    /**
     * Base constructor of a pet configuration (one to one)
     * It will initialize the variables while loading the data
     * @param fileName
     */
    public PetConfig(String folderName, String fileName)
    {
        super.init(folderName, fileName);
        reload();
    }

    /**
     * Save the data within the file (unused for a PetConfig)
     */
    @Override
    public void save() {
        super.save();
    }

    /**
     * Load the data from the file to create an associated Pet
     */
    @Override
    public void reload() {
        // Loading the YAMLConfiguration object
        loadConfig();

        // Setting up the data
        String id                   = getConfig().getString("Id");
        String mobType              = getConfig().getString("MythicMob");
        String permission           = getConfig().getString("Permission");
        int distance                = getConfig().getInt("Distance");
        String despawnSkillName     = getConfig().getString("DespawnSkill");
        String iconName             = getConfig().getString("Icon.Name");
        String textureBase64        = getConfig().getString("Icon.TextureBase64");
        boolean autoRide            = getConfig().getBoolean("AutoRide");
        List<String> description    = getConfig().getStringList("Icon.Description");

        if( id              == null ||
                mobType         == null ||
                permission      == null ||
                iconName        == null ||
                textureBase64   == null ||
                description     == null)
        {
            // Warning case on which something essential would be missing
            AdvancedPet.getLog().warning(AdvancedPet.getLogName() + "This pet could not be registered. Please check the configuration file to make sure you didn't miss anything.");
            AdvancedPet.getLog().warning(AdvancedPet.getLogName() + "Information about the registered pet : ");
            AdvancedPet.getLog().warning("id : " + id);
            AdvancedPet.getLog().warning("mobType : " + mobType);
            AdvancedPet.getLog().warning("permission : " + permission);
            return;
        }

        Pet pet = new Pet(id);
        pet.setMythicMobName(mobType);
        pet.setPermission(permission);
        if(getConfig().get("Mountable") == null) {
            pet.setMountable(GlobalConfig.getInstance().isMountable());
        } else {
            pet.setMountable(getConfig().getBoolean("Mountable"));
        }
        pet.setAutoRide(autoRide);
        pet.setDistance(distance);

        if(despawnSkillName != null)
        {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Optional<Skill> optionalSkill = MythicMobs.inst().getSkillManager().getSkill(despawnSkillName);
                    optionalSkill.ifPresent(pet::setDespawnSkill);
                    if(pet.getDespawnSkill() == null)
                    {
                        AdvancedPet.getLog().warning(AdvancedPet.getLogName() + "Impossible to link the despawn skill \"" + despawnSkillName + "\" to the pet \"" + pet.getId() + "\", because this skill doesn't exist.");
                    }
                }
            }.runTaskLater(AdvancedPet.getInstance(), 5L);
        }

        pet.buildIcon(iconName, description, textureBase64);

        this.pet = pet;
    }

    /**
     * Load all the existing pets
     * @param folderPath : folder where to seek for the pets
     * @param clearPets : whether or not the loaded pets should be cleared (only first call should do that)
     */
    public static void loadPets(String folderPath, boolean clearPets)
    {
        if(clearPets)
        {
            Pet.getObjectPets().clear();
        }

        File folder = new File(folderPath);
        if(!folder.exists())
            folder.mkdirs();

        for(File file : folder.listFiles())
        {
            if(file.isDirectory())
            {
                loadPets(file.getPath().replace("\\", "/"), false);
                continue;
            }

            PetConfig petConfig = new PetConfig(folder.getPath().replace("\\", "/").replace(AbstractConfig.getPath(), ""), file.getName());

            if(petConfig.getPet() != null)
                Pet.getObjectPets().add(petConfig.getPet());

        }

        if(clearPets)
            AdvancedPet.getLog().info(AdvancedPet.getLogName() + Pet.getObjectPets().size() + " pets registered successfully !");
    }
}
