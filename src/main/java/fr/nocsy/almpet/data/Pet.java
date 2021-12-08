package fr.nocsy.almpet.data;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.mount.controller.MountController;
import com.ticxo.modelengine.api.mount.handler.IMountHandler;
import fr.nocsy.almpet.AdvancedPet;
import fr.nocsy.almpet.data.config.GlobalConfig;
import fr.nocsy.almpet.data.config.Language;
import fr.nocsy.almpet.data.inventories.PlayerData;
import fr.nocsy.almpet.events.*;
import fr.nocsy.almpet.utils.Utils;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractLocation;
import io.lumine.xikage.mythicmobs.api.exceptions.InvalidMobTypeException;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.skills.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Pet {

    //---------------------------------------------------------------------
    public static final String SIGNAL_STICK_TAG = "&AdvancedPet-SignalSticks&";

    //---------------------------------------------------------------------
    public static final int BLOCKED = 1;
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

    @Getter
    @Setter
    private int spawnRange;

    @Getter
    @Setter
    private int comingBackRange;

    @Setter
    @Getter
    private ItemStack icon;

    @Setter
    @Getter
    private ItemStack signalStick;

    @Getter
    @Setter
    private String currentName;

    @Getter
    @Setter
    private Skill despawnSkill;

    @Getter
    @Setter
    private boolean autoRide;

    @Setter
    @Getter
    private String mountType;

    @Getter
    @Setter
    private List<String> signals;

    //********** Living entity **********

    @Setter
    @Getter
    private UUID owner;

    @Setter
    @Getter
    private ActiveMob activeMob;

    @Getter
    private boolean invulnerable;

    @Getter
    @Setter
    private boolean removed;

    @Getter
    @Setter
    private boolean checkPermission;

    @Getter
    @Setter
    private boolean firstSpawn;

    // Make sure there is no flooding spawn
    private boolean antiDuplication = false;

    /**
     * Constructor only used to create a fundamental Pet. If you wish to use a pet instance, please refer to copy()
     * @param id
     */
    public Pet(String id)
    {
        this.id = id;
        this.instance = this;
        this.checkPermission = true;
        this.firstSpawn = true;
    }

    /**
     * Spawn the pet if possible. Return values are indicated in this class.
     * @param loc
     * @return
     */
    public int spawn(Location loc)
    {
        if(antiDuplication)
        {
            return BLOCKED;
        }

        new BukkitRunnable()
        {
            @Override
            public void run() {
                antiDuplication = false;
            }
        }.runTaskLater(AdvancedPet.getInstance(), 2L);

        PetSpawnEvent event = new PetSpawnEvent(this);
        Utils.callEvent(event);

        if(event.isCancelled())
            return BLOCKED;

        if(checkPermission && owner != null &&
                Bukkit.getPlayer(owner) != null &&
                !Bukkit.getPlayer(owner).hasPermission(permission))
            return NOT_ALLOWED;

        if(mythicMobName == null)
        {
            return MYTHIC_MOB_NULL;
        }
        else if(owner == null)
            return OWNER_NULL;

        try {

            Entity ent = null;
            if(autoRide)
            {
                ent = MythicMobs.inst().getAPIHelper().spawnMythicMob(mythicMobName,  loc);
            }
            else
            {
                ent = MythicMobs.inst().getAPIHelper().spawnMythicMob(mythicMobName,  Utils.bruised(loc, getSpawnRange()));
            }
            if(ent == null)
            {
                return MYTHIC_MOB_NULL;
            }
            Optional<ActiveMob> maybeHere = MythicMobs.inst().getMobManager().getActiveMob(ent.getUniqueId());
            maybeHere.ifPresent(mob -> activeMob = mob);
            if(activeMob == null)
            {
                return MYTHIC_MOB_NULL;
            }
            ent.setMetadata("AlmPet", new FixedMetadataValue(AdvancedPet.getInstance(), this));
            if(ent.isInvulnerable() && GlobalConfig.getInstance().isLeftClickToOpen())
            {
                this.invulnerable = true;
                ent.setInvulnerable(false);
            }
            activeMob.setOwner(owner);
            this.ia();

            boolean returnDespawned = false;

            if(activePets.containsKey(owner))
            {
                Pet previous = activePets.get(owner);
                previous.despawn(PetDespawnReason.REPLACED);

                activePets.remove(owner);
                returnDespawned = true;
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

            if(firstSpawn)
            {
                firstSpawn = false;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Player p = Bukkit.getPlayer(owner);
                        if(p != null && autoRide)
                        {
                            boolean mounted = setMount(p);
                            if(!mounted)
                                Language.NOT_MOUNTABLE.sendMessage(p);
                        }
                    }
                }.runTaskLater(AdvancedPet.getInstance(), 5L);
            }

            PlayerSignal.setDefaultSignal(owner, this);

            if(returnDespawned)
                return DESPAWNED_PREVIOUS;
            return MOB_SPAWN;

        } catch (InvalidMobTypeException e) {
            return NO_MOB_MATCH;
        }

    }

    /**
     * Spawn the pet and send the corresponding message on execution
     * @param p
     * @param loc
     */
    public void spawnWithMessage(Player p, Location loc)
    {
        int executed = this.spawn(p, p.getLocation());

        switch (executed) {
            case Pet.DESPAWNED_PREVIOUS -> Language.REVOKED_FOR_NEW_ONE.sendMessage(p);
            case Pet.MOB_SPAWN -> Language.SUMMONED.sendMessage(p);
            case Pet.MYTHIC_MOB_NULL -> Language.MYTHICMOB_NULL.sendMessage(p);
            case Pet.NO_MOB_MATCH -> Language.NO_MOB_MATCH.sendMessage(p);
            case Pet.NOT_ALLOWED -> Language.NOT_ALLOWED.sendMessage(p);
            case Pet.OWNER_NULL -> Language.OWNER_NOT_FOUND.sendMessage(p);
        }
    }

    private int task;
    /**
     * Activate the following IA of the mob
     */
    public void ia()
    {

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(AdvancedPet.getInstance(), new Runnable() {

            @Override
            public void run() {

                Player p = Bukkit.getPlayer(owner);

                if(!getInstance().isStillHere())
                {
                    Bukkit.getScheduler().cancelTask(task);
                    return;
                }

                if(p != null)
                {
                    if(p.isDead())
                        return;

                    Location ownerLoc = p.getLocation();
                    Location petLoc = getInstance().getActiveMob().getEntity().getBukkitEntity().getLocation();

                    if(!ownerLoc.getWorld().getName().equals(petLoc.getWorld().getName()))
                    {
                        getInstance().despawn(PetDespawnReason.TELEPORT);
                        getInstance().spawn(p, p.getLocation());
                        return;
                    }

                    double distance = Utils.distance(ownerLoc, petLoc);

                    if(distance < getInstance().getComingBackRange())
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
                        antiDuplication = true;
                        getInstance().teleportToPlayer(p);
                    }
                }
                else
                {
                    getInstance().despawn(PetDespawnReason.OWNER_NOT_HERE);
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
    public boolean despawn(PetDespawnReason reason)
    {
        PetDespawnEvent event = new PetDespawnEvent(this, reason);
        Utils.callEvent(event);

        Bukkit.getScheduler().cancelTask(task);
        removed = true;
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
                Pet.clearStickSignals(ownerPlayer);
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
            this.despawn(PetDespawnReason.TELEPORT);
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
    public void setDisplayName(String wanted_name, boolean save)
    {
        PetChangeNameEvent event = new PetChangeNameEvent(this, wanted_name, save);
        Utils.callEvent(event);

        if(event.isCancelled())
            return;

        final String name = event.getName();
        save = event.isSaveChanges();

        try {

            if (name != null && ChatColor.stripColor(name).length() > GlobalConfig.instance.getMaxNameLenght()) {
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
                    }.runTaskLater(AdvancedPet.getInstance(), 20L);

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
                }.runTaskLater(AdvancedPet.getInstance(), 20L);

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
            AdvancedPet.getLog().warning("[AlmPet] : Une exception " + ex.getClass().getSimpleName() + " a été soulevé par setDisplayName(" + Language.TAG_TO_REMOVE_NAME.getMessage() + "), concernant le pet " + this.id);
            ex.printStackTrace();
        }
    }

    /**
     * Return a copy of the current pet. Used to implement a player pet in game
     * @return
     */
    public Pet copy()
    {
        Pet pet = new Pet(id);
        pet.setMythicMobName(mythicMobName);
        pet.setPermission(permission);
        pet.setDistance(distance);
        pet.setSpawnRange(spawnRange);
        pet.setComingBackRange(comingBackRange);
        pet.setDespawnSkill(despawnSkill);
        pet.setMountable(mountable);
        pet.setMountType(mountType);
        pet.setAutoRide(autoRide);
        pet.setIcon(icon);
        pet.setSignalStick(signalStick);
        pet.setOwner(owner);
        pet.setActiveMob(activeMob);
        pet.setSignals(signals);
        return pet;
    }

    /**
     * Set the specified entity riding on the pet
     * @param ent
     */
    public boolean setMount(Entity ent)
    {
        EntityMountPetEvent event = new EntityMountPetEvent(ent, this);
        Utils.callEvent(event);

        if(event.isCancelled())
            return false;

        if(isStillHere())
        {
            UUID petUUID = activeMob.getEntity().getUniqueId();
            ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(petUUID);
            if (localModeledEntity == null) {
                return false;
            }
            IMountHandler localIMountHandler = localModeledEntity.getMountHandler();

            MountController localMountController = ModelEngineAPI.api.getControllerManager().createController(mountType);
            if (localMountController == null) {
                localMountController = ModelEngineAPI.api.getControllerManager().createController("walking");
            }
            localIMountHandler.setDriver(ent, localMountController);
            localIMountHandler.setCanDamageMount(ent, false);
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
            IMountHandler localIMountHandler = localModeledEntity.getMountHandler();

            return localIMountHandler.hasDriver() || localIMountHandler.hasPassengers();
        }
        return false;
    }

    /**
     * Unset the specified entity riding on the pet
     */
    public void dismount(Entity ent)
    {
        if(ent == null)
            return;

        // Try - catch to prevent onDisable no class def found print
        try
        {
            if(isStillHere())
            {
                UUID localUUID = activeMob.getEntity().getUniqueId();
                ModeledEntity localModeledEntity = ModelEngineAPI.api.getModelManager().getModeledEntity(localUUID);
                if (localModeledEntity == null) {
                    return;
                }
                IMountHandler localIMountHandler = localModeledEntity.getMountHandler();
                localIMountHandler.removePassenger(ent);
                localIMountHandler.setDriver(null);
            }

        } catch (NoClassDefFoundError ignored){}

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
     * Give a stick signal to the player refering to his pet
     * @param p
     */
    public void giveStickSignals(Player p)
    {
        if(getOwner() == null || getSignalStick() == null)
            return;

        if(p == null)
            return;

        clearStickSignals(p);

        p.getInventory().addItem(this.getSignalStick());

    }

    /**
     * Remove the stick signal from inventory
     * @param p
     */
    public static void clearStickSignals(Player p)
    {
        if(p == null)
            return;

        for(int i = 0; i < p.getInventory().getSize(); i++)
        {
            ItemStack item = p.getInventory().getItem(i);
            if(Items.isSignalStick(item))
            {
                p.getInventory().setItem(i, new ItemStack(Material.AIR));
            }
        }
    }

    /**
     * Get the pet to cast a skill by sending it a signal
     * @param signal
     * @return
     */
    public boolean castSkill(String signal)
    {
        PetCastSkillEvent event = new PetCastSkillEvent(this, signal);
        Utils.callEvent(event);

        if(event.isCancelled())
            return false;

        if(this.isStillHere())
        {
            ActiveMob mob = this.getActiveMob();
            mob.signalMob(mob.getEntity(), signal);
            return true;
        }
        return false;
    }

    /**
     * Setup the item with requirements
     * @param iconName
     * @param description
     * @param textureBase64
     */
    public ItemStack buildItem(ItemStack item, String localizedName, String iconName, List<String> description, String materialType, int customModelData, String textureBase64)
    {

        Material mat = materialType != null ? Material.getMaterial(materialType) : null;

        if(mat == null
                && textureBase64 != null)
        {
            item = Utils.createHead(iconName, description, textureBase64);
            ItemMeta meta = item.getItemMeta();
            meta.setLocalizedName(localizedName);
            item.setItemMeta(meta);
        }
        else if(mat != null)
        {
            item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setLocalizedName(localizedName);
            meta.setCustomModelData(customModelData);
            meta.setDisplayName(iconName);
            meta.setLore(description);
            item.setItemMeta(meta);
        }
        else
        {
            item = Utils.createHead(iconName, description, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWQ5Y2M1OGFkMjVhMWFiMTZkMzZiYjVkNmQ0OTNjOGY1ODk4YzJiZjMwMmI2NGUzMjU5MjFjNDFjMzU4NjcifX19");
            ItemMeta meta = item.getItemMeta();
            meta.setLocalizedName(localizedName);
            item.setItemMeta(meta);
        }
        return item;
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
            if(pet.isCheckPermission())
            {
                if(p.hasPermission(pet.getPermission()))
                    pets.add(pet);
            }
            else
            {
                pets.add(pet);
            }

        }

        return pets;
    }

    public static void clearPets()
    {
        for(Pet pet : Pet.getActivePets().values())
        {
            pet.despawn(PetDespawnReason.RELOAD);
        }
    }

}
