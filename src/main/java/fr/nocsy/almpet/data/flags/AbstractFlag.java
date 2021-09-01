package fr.nocsy.almpet.data.flags;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.flags.registry.SimpleFlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.nocsy.almpet.AlmPet;
import lombok.Getter;
import org.bukkit.entity.Player;

public abstract class AbstractFlag {

    @Getter
    private StateFlag flag;

    @Getter
    private  final AlmPet almPetInstance;
    @Getter
    private final String flagName;
    @Getter
    private final boolean defaultValue;

    public AbstractFlag(String flagName, boolean defaultValue, AlmPet instance)
    {
        this.flagName       = flagName;
        this.defaultValue   = defaultValue;
        almPetInstance      = instance;
    }

    /**
     * Register the given flag in WorldGuard
     */
    public void register()
    {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        ((SimpleFlagRegistry) WorldGuard.getInstance().getFlagRegistry()).setInitialized(false);

        try {

            // create a flag with the name "flagName", defaulting to defaultValue
            StateFlag flag = new StateFlag(flagName, defaultValue);
            registry.register(flag);
            this.flag = flag; // only set our field if there was no error

            AlmPet.getLog().info("[AlmPet] : " + getFlagName() + " flag enregistré avec succès !");

        } catch (Exception e) {
            AlmPet.getLog().warning("[AlmPet] : Une exception a été soulevée du type : " + e.getClass().getSimpleName());
            AlmPet.getLog().warning("[AlmPet] : " + getFlagName() + " semble être en conflit avec une instance précédente de AlmPet. Essayons d'attacher le flag à l'instance précédente...");
            // some other plugin registered a flag by the same name already.
            // you can use the existing flag, but this may cause conflicts - be sure to check type
            Flag<?> existing = registry.get(flagName);
            AlmPet.getLog().warning("[AlmPet] : " + getFlagName() + " a été comptabilisé comme " + existing + " par Worldguard");
            if (existing instanceof StateFlag) {
                this.flag = (StateFlag) existing;
                AlmPet.getLog().info("[AlmPet] : " + getFlagName() + " flag attaché avec succès !");
            } else {
                // types don't match - this is bad news! some other plugin conflicts with you
                // hopefully this never actually happens
                AlmPet.getLog().warning("[AlmPet] : " + getFlagName() + " flag n'a pas pu être attaché... Redémarrer le serveur pour fixer le problème.");
            }

        }
    }

    /**
     * Test if the state flag is allowed at player's location
     * @param p
     * @return
     */
    public boolean testState(Player p)
    {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        Location loc = localPlayer.getLocation();
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        return query.testState(loc, localPlayer, getFlag());
    }

}
