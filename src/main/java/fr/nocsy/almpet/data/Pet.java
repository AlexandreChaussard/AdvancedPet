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
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    //********** Living entity **********

    @Setter
    @Getter
    private UUID owner;

    @Setter
    @Getter
    private ActiveMob activeMob;

    /**
     * Constructor only used to create a fundamental Pet. If you wish to use a pet instance, please refer to copy()
     * @param id
     */
    protected Pet(String id)
    {
        this.id = id;
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
                !Objects.requireNonNull(Bukkit.getPlayer(owner)).hasPermission(permission))
            return NOT_ALLOWED;

        if(mythicMobName == null)
            return MYTHIC_MOB_NULL;
        else if(owner == null)
            return OWNER_NULL;

        try {

            Entity ent = MythicMobs.inst().getAPIHelper().spawnMythicMob(mythicMobName, loc);
            Optional<ActiveMob> maybeHere = MythicMobs.inst().getMobManager().getActiveMob(ent.getUniqueId());
            maybeHere.ifPresent(mob -> activeMob = mob);
            ent.setMetadata("AlmPet", new FixedMetadataValue(AlmPet.getInstance(), this));

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

            PlayerData pd = new PlayerData(owner);
            String name = pd.getMapOfRegisteredNames().get(this.id);
            if(name != null)
            {
                setDisplayName(name);
            }
            else
            {
                setDisplayName("Aucun");
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

        Pet pet = this;

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(AlmPet.getInstance(), new Runnable() {

            @Override
            public void run() {
                if(!pet.isStillHere())
                {
                    Bukkit.getScheduler().cancelTask(task);
                    return;
                }

                Player p = Bukkit.getPlayer(owner);
                if(p != null)
                {
                    Location ownerLoc = p.getLocation();
                    Location petLoc = pet.getActiveMob().getEntity().getBukkitEntity().getLocation();

                    if(!ownerLoc.getWorld().getName().equals(petLoc.getWorld().getName()))
                    {
                        pet.despawn();
                        pet.spawn(p, p.getLocation());
                        return;
                    }

                    double distance = Utils.distance(ownerLoc, petLoc);

                    if(distance < pet.distance)
                    {
                        MythicMobs.inst().getVolatileCodeHandler().getAIHandler().navigateToLocation(pet.getActiveMob().getEntity(), pet.getActiveMob().getEntity().getLocation(), Double.POSITIVE_INFINITY);
                    }
                    else if(distance > pet.getDistance() &&
                        distance < GlobalConfig.getInstance().distanceTeleport)
                    {
                        AbstractLocation aloc = new AbstractLocation(pet.getActiveMob().getEntity().getWorld(),
                                                                    p.getLocation().getX(),
                                                                    p.getLocation().getY(),
                                                                    p.getLocation().getZ());
                        MythicMobs.inst().getVolatileCodeHandler().getAIHandler().navigateToLocation(pet.getActiveMob().getEntity(), aloc, Double.POSITIVE_INFINITY);
                    }
                    else if(distance > GlobalConfig.getInstance().distanceTeleport)
                    {
                        pet.teleportToPlayer(p);
                    }
                }
                else
                {
                    pet.despawn();
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
            if(activeMob.getEntity() != null)
                activeMob.getEntity().remove();
            if(activeMob.getEntity() != null && activeMob.getEntity().getBukkitEntity() != null)
                activeMob.getEntity().getBukkitEntity().remove();

            activeMob.setDespawned();

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
        if(activeMob != null && activeMob.getEntity() != null && activeMob.getEntity().getBukkitEntity() != null)
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
        if(activeMob != null && activeMob.getEntity() != null && activeMob.getEntity().getBukkitEntity() != null)
            this.teleport(p.getLocation());
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
    public void setDisplayName(String name)
    {
        if(this.isStillHere())
        {
            if(name.equalsIgnoreCase("Aucun"))
            {
                activeMob.setShowCustomNameplate(false);
                activeMob.getEntity().getBukkitEntity().setCustomName(GlobalConfig.getInstance().getDefaultName().replace("%player%", Bukkit.getOfflinePlayer(owner).getName()));
                activeMob.getEntity().getBukkitEntity().setCustomNameVisible(false);
                setNameTagVisible(false);

                PlayerData pd = new PlayerData(owner);
                pd.getMapOfRegisteredNames().remove(this.id);
                pd.save();

                return;
            }
            activeMob.setShowCustomNameplate(true);
            activeMob.getEntity().getBukkitEntity().setCustomName(name);
            activeMob.getEntity().getBukkitEntity().setCustomNameVisible(true);
            new BukkitRunnable()
            {

                @Override
                public void run() {
                    setNameTagVisible(true);
                }
            }.runTaskLater(AlmPet.getInstance(), 20L);

            PlayerData pd = new PlayerData(owner);
            pd.getMapOfRegisteredNames().put(this.id, name);
            pd.save();

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

    public void setNameTagVisible(boolean visible)
    {
        if(isStillHere())
        {
            ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(this.activeMob.getEntity().getUniqueId());
            if (localModeledEntity == null) {
                return;
            }
            activeMob.getEntity().getBukkitEntity().setCustomNameVisible(visible);
            localModeledEntity.setNametagVisible(visible);
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
