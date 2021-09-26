package fr.nocsy.almpet.data;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import fr.nocsy.almpet.AlmPet;
import fr.nocsy.almpet.data.inventories.PlayerData;
import fr.nocsy.almpet.utils.Utils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import io.lumine.xikage.mythicmobs.skills.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Pet {

    //---------------------------------------------------------------------
    public static final int MOB_SPAWN = 0;
    public static final int DESPAWNED_PREVIOUS = 1;
    public static final int OWNER_NULL = -1;
    public static final int MYTHIC_MOB_NULL = -2;
    public static final int NO_MOB_MATCH = -3;
    public static final int NOT_ALLOWED = -4;
    //---------------------------------------------------------------------

    //********** Static values **********

    @Getter
    private static HashMap<UUID, Pet> activePets = new HashMap<UUID, Pet>();
    @Getter
    private static ArrayList<Pet> objectPets = new ArrayList<Pet>();

    //********** Global Pet **********

    @Getter
    private Pet instance;

    @Getter
    private String id;

    @Setter
    @Getter
    private String mythicMobName;

    @Setter
    @Getter
    private String permission;

    @Setter
    @Getter
    private boolean mountable;

    @Getter
    @Setter
    private int distance;

    @Setter
    @Getter
    private ItemStack icon;

    @Getter
    @Setter
    private String currentName;

    @Getter
    @Setter
    private Skill despawnSkill;

    //********** Living entity **********

    @Setter
    @Getter
    private UUID owner;

    @Setter
    @Getter
    private ActiveMob activeMob;

    @Getter
    private boolean invulnerable;

    /**
     * Constructor only used to create a fundamental Pet. If you wish to use a pet instance, please refer to copy()
     * @param id
     */
    protected Pet(String id)
    {
        this.id = id;
        this.instance = this;
    }


    /**
     * Spawn the pet if possible. Return values are indicated in this class.
     * @param loc
     * @return
     */
    public int spawn(Location loc)
    {
        if(owner != null &&
                Bukkit.getPlayer(owner) != null &&
                !Bukkit.getPlayer(owner).hasPermission(permission))
            return NOT_ALLOWED;

        if(mythicMobName == null)
            return MYTHIC_MOB_NULL;
        else if(owner == null)
            return OWNER_NULL;

        try {

            Entity ent = MythicMobs.inst().getAPIHelper().spawnMythicMob(mythicMobName,  Utils.bruised(loc, getDistance()));
            if(ent == null)
            {
                return MYTHIC_MOB_NULL;
            }
            Optional<ActiveMob> maybeHere = MythicMobs.inst().getMobManager().getActiveMob(ent.getUniqueId());
            maybeHere.ifPresent(mob -> activeMob = mob);
            ent.setMetadata("AlmPet", new FixedMetadataValue(AlmPet.getInstance(), this));
            if(ent.isInvulnerable() && GlobalConfig.getInstance().isLeftClickToOpen())
            {
                this.invulnerable = true;
                ent.setInvulnerable(false);
            }
            activeMob.setOwner(owner);
            this.ia();

            if(activePets.containsKey(owner))
            {
                Pet previous = activePets.get(owner);
                previous.despawn();

                activePets.remove(owner, this);
                activePets.put(owner, this);

                return DESPAWNED_PREVIOUS;
            }

            activePets.put(owner, this);

            PlayerData pd = PlayerData.get(owner);
            String name = pd.getMapOfRegisteredNames().get(this.id);
            if(name != null)
            {
                setDisplayName(name, false);
            }
            else
            {
                setDisplayName(Language.TAG_TO_REMOVE_NAME.getMessage(), false);
            }


            return MOB_SPAWN;

        } catch (InvalidMobTypeException e) {
            return NO_MOB_MATCH;
        }

    }

    private int task;
    /**
     * Activate the following IA of the mob
     */
    public void ia()
    {

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(AlmPet.getInstance(), new Runnable() {

            @Override
            public void run() {
                if(!getInstance().isStillHere())
                {
                    Bukkit.getScheduler().cancelTask(task);
                    return;
                }

                Player p = Bukkit.getPlayer(owner);
                if(p != null)
                {
                    Location ownerLoc = p.getLocation();
                    Location petLoc = getInstance().getActiveMob().getEntity().getBukkitEntity().getLocation();

                    if(!ownerLoc.getWorld().getName().equals(petLoc.getWorld().getName()))
                    {
                        getInstance().despawn();
                        getInstance().spawn(p, p.getLocation());
                        return;
                    }

                    double distance = Utils.distance(ownerLoc, petLoc);

                    if(distance < getInstance().distance)
                    {
                        MythicMobs.inst().getVolatileCodeHandler().getAIHandler().navigateToLocation(getInstance().getActiveMob().getEntity(), getInstance().getActiveMob().getEntity().getLocation(), Double.POSITIVE_INFINITY);
                    }
                    else if(distance > getInstance().getDistance() &&
                        distance < GlobalConfig.getInstance().getDistanceTeleport())
                    {
                        AbstractLocation aloc = new AbstractLocation(getInstance().getActiveMob().getEntity().getWorld(),
                                                                    p.getLocation().getX(),
                                                                    p.getLocation().getY(),
                                                                    p.getLocation().getZ());
                        MythicMobs.inst().getVolatileCodeHandler().getAIHandler().navigateToLocation(getInstance().getActiveMob().getEntity(), aloc, Double.POSITIVE_INFINITY);
                    }
                    else if(distance > GlobalConfig.getInstance().getDistanceTeleport() && !p.isFlying())
                    {
                        getInstance().teleportToPlayer(p);
                    }
                }
                else
                {
                    getInstance().despawn();
                    Bukkit.getScheduler().cancelTask(task);
                }

            }
        }, 0L, 10L);
    }

    /**
     * Spawn the pet at specified location and attributing player as the owner of the pet
     * @param owner
     * @param loc
     * @return
     */
    public int spawn(@NotNull Player owner, Location loc)
    {
        this.owner = owner.getUniqueId();
        return spawn(loc);
    }

    /**
     * Despawn the pet
     * @return
     */
    public boolean despawn()
    {
        if(activeMob != null)
        {

            if(despawnSkill != null)
            {
                despawnSkill.execute(new SkillMetadata(SkillTrigger.CUSTOM, activeMob, activeMob.getEntity()));
            }
            else
            {
                if(activeMob.getEntity() != null)
                    activeMob.getEntity().remove();
                if(activeMob.getEntity() != null && activeMob.getEntity().getBukkitEntity() != null)
                    activeMob.getEntity().getBukkitEntity().remove();
            }

            Player ownerPlayer = Bukkit.getPlayer(owner);
            if(ownerPlayer != null)
            {
                this.dismount(ownerPlayer);
            }

            activePets.remove(owner);
            return true;
        }
        activePets.remove(owner);
        return false;
    }

    /**
     * Teleport the pet to the specific location
     * @param loc
     */
    public void teleport(Location loc)
    {
        if(isStillHere())
        {
            this.despawn();
            this.spawn(loc);
        }
    }

    /**
     * Teleport the pet to the player
     */
    public void teleportToPlayer(Player p)
    {
        Location loc = Utils.bruised(p.getLocation(), getDistance());

        if(isStillHere())
            this.teleport(loc);
    }

    /**
     * Say whether or not the entity is still present
     * @return
     */
    public boolean isStillHere()
    {
        return activeMob != null && activeMob.getEntity() != null && activeMob.getEntity().getBukkitEntity() != null && getActivePets().containsValue(this);
    }

    /**
     * Set the display name of the pet
     */
    public void setDisplayName(final String name, final boolean save)
    {
        try {

            if (name != null && name.length() > GlobalConfig.instance.getMaxNameLenght()) {
                setDisplayName(name.substring(0, GlobalConfig.instance.getMaxNameLenght()), save);
                return;
            }

            currentName = name;

            if (isStillHere()) {

                if (name == null || name.equalsIgnoreCase(Language.TAG_TO_REMOVE_NAME.getMessage())) {
                    activeMob.getEntity().getBukkitEntity().setCustomName(GlobalConfig.getInstance().getDefaultName().replace("%player%", Bukkit.getOfflinePlayer(owner).getName()));

                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            setNameTag(name, false);
                        }
                    }.runTaskLater(AlmPet.getInstance(), 20L);

                    if(save)
                    {
                        PlayerData pd = PlayerData.get(owner);
                        pd.getMapOfRegisteredNames().remove(getId());
                        pd.save();
                    }

                    return;
                }

                activeMob.getEntity().getBukkitEntity().setCustomName(name);

                new BukkitRunnable() {

                    @Override
                    public void run() {
                        setNameTag(name, true);
                    }
                }.runTaskLater(AlmPet.getInstance(), 20L);

                if(save)
                {
                    PlayerData pd = PlayerData.get(owner);
                    pd.getMapOfRegisteredNames().put(getId(), name);
                    pd.save();
                }
            }

        }
        catch (Exception ex)
        {
            AlmPet.getLog().warning("[AlmPet] : Une exception " + ex.getClass().getSimpleName() + " a été soulevé par setDisplayName(" + Language.TAG_TO_REMOVE_NAME.getMessage() + "), concernant le pet " + this.id);
            ex.printStackTrace();
        }
    }

    /**
     * Return a copy of the current pet. Useful to implement a player pet in game
     * @return
     */
    public Pet copy()
    {
        Pet pet = new Pet(id);
        pet.setMythicMobName(mythicMobName);
        pet.setPermission(permission);
        pet.setDistance(distance);
        pet.setDespawnSkill(despawnSkill);
        pet.setMountable(mountable);
        pet.setIcon(icon);
        pet.setOwner(owner);
        pet.setActiveMob(activeMob);
        return pet;
    }

    /**
     * Set the specified entity riding on the pet
     * @param ent
     */
    public boolean setMount(Entity ent)
    {
        if(isStillHere())
        {
            UUID petUUID = activeMob.getEntity().getUniqueId();
            ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(petUUID);
            if (localModeledEntity == null) {
                return false;
            }
            localModeledEntity.setMountController("standard");
            localModeledEntity.addPassenger(ent);
            return true;
        }
        return false;
    }

    /**
     * Say if the specified entity is riding on the pet
     * @param ent
     */
    public boolean hasMount(Entity ent)
    {
        if(isStillHere())
        {
            UUID petUUID = activeMob.getEntity().getUniqueId();
            ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(petUUID);
            if (localModeledEntity == null) {
                return false;
            }
            localModeledEntity.setMountController("standard");
            return localModeledEntity.hasPassenger(ent);
        }
        return false;
    }

    /**
     * Unset the specified entity riding on the pet
     */
    public void dismount(Entity ent)
    {
        if(isStillHere())
        {
            UUID localUUID = activeMob.getEntity().getUniqueId();
            ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(localUUID);
            if (localModeledEntity == null) {
                return;
            }
            localModeledEntity.removePassenger(ent);
        }

    }

    public void setNameTag(String name, boolean visible)
    {
        if(isStillHere())
        {
            ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(this.activeMob.getEntity().getUniqueId());
            if (localModeledEntity == null) {
                return;
            }
            activeMob.getEntity().getBukkitEntity().setCustomNameVisible(visible);
            localModeledEntity.setNametagVisible(visible);
            localModeledEntity.setNametag(name);
            localModeledEntity.setInvisible(true);
        }
    }

    /**
     * Setup the icon for the menu of selection. It shouldn't have to be updated out of the data package.
     * @param iconName
     * @param description
     * @param textureBase64
     */
    protected void buildIcon(String iconName, List<String> description, String textureBase64)
    {
        icon = Utils.createHead(iconName, description, textureBase64);
        ItemMeta meta = icon.getItemMeta();
        meta.setLocalizedName(this.toString());
        icon.setItemMeta(meta);
    }

    /**
     * Format : "AlmPet;petId"
     * @return
     */
    public String toString()
    {
        return "AlmPet;" + id;
    }

    /**
     * Compare using mythicmobs name
     * @param other
     * @return
     */
    public boolean equals(Pet other)
    {
        return this.id.equals(other.getId());
    }

    /**
     * Get the pet from a serialized toString version
     * @param seria
     * @return
     */
    public static Pet fromString(String seria)
    {
        if(seria.startsWith("AlmPet;"))
        {
            String id = seria.split(";")[1];
            return getFromId(id);
        }
        return null;
    }

    /**
     * Get pet object from the id of the pet
     * @param id
     * @return
     */
    public static Pet getFromId(String id)
    {
        for(Pet pet : objectPets)
        {
            if(pet.getId().equals(id))
            {
                return pet;
            }
        }
        return null;
    }

    /**
     * Get the pet from the ItemStack icon
     * @param icon
     * @return
     */
    public static Pet getFromIcon(ItemStack icon)
    {
        if(icon.getItemMeta().hasLocalizedName())
        {
            return fromString(icon.getItemMeta().getLocalizedName());
        }
        return null;
    }

    /**
     * Get the pet from the specified entity
     * @param ent
     * @return
     */
    public static Pet getFromEntity(Entity ent)
    {
        if(ent != null &&
            ent.hasMetadata("AlmPet") &&
            ent.getMetadata("AlmPet").size() > 0 &&
            ent.getMetadata("AlmPet").get(0) != null &&
            ent.getMetadata("AlmPet").get(0).value() != null)
        {
            return (Pet) ent.getMetadata("AlmPet").get(0).value();
        }
        return null;
    }

    /**
     * Get the pet of the specified owner if it exists
     * @param owner
     * @return
     */
    public static Pet fromOwner(UUID owner)
    {
        return Pet.getActivePets().get(owner);
    }

    /**
     * Get the pet from the last one that the player interacted with
     * @param p
     * @return
     */
    public static Pet getFromLastInteractedWith(Player p)
    {
        if(p != null &&
                p.hasMetadata("AlmPetInteracted") &&
                p.getMetadata("AlmPetInteracted").size() > 0 &&
                p.getMetadata("AlmPetInteracted").get(0) != null &&
                p.getMetadata("AlmPetInteracted").get(0).value() != null)
        {
            return (Pet) p.getMetadata("AlmPetInteracted").get(0).value();
        }
        return null;
    }

    /**
     * List of pets available for the specified player (using permissions)
     * @param p
     * @return
     */
    public static List<Pet> getAvailablePets(Player p)
    {
        ArrayList<Pet> pets = new ArrayList<>();

        for(Pet pet : objectPets)
        {
            if(p.hasPermission(pet.getPermission()))
                pets.add(pet);
        }

        return pets;
    }

    public static void clearPets()
    {
        for(Pet pet : Pet.getActivePets().values())
        {
            pet.despawn();
        }
    }

}
