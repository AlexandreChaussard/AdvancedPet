package fr.nocsy.almpet.data.flags;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.nocsy.almpet.AlmPet;
import fr.nocsy.almpet.data.Language;
import fr.nocsy.almpet.data.Pet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DespawnPetFlag extends AbstractFlag implements StoppableFlag{

    public DespawnPetFlag(AlmPet instance) {
        super("despawnPet", false, instance);
    }

    @Override
    public void register()
    {
        super.register();
        launch();
    }

    int task;
    private void launch()
    {
        if(getFlag() == null)
        {
            AlmPet.getLog().warning("[AlmPet] : Impossible de lancer le flag " + getFlagName() + " car le flag est null. Contacter Nocsy.");
            return;
        }
        else
        {
            AlmPet.getLog().info("[AlmPet] : Lancement du flag " + getFlagName() + ".");
        }

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(getAlmPetInstance(), new Runnable() {
            @Override
            public void run() {

                for(UUID owner : Pet.getActivePets().keySet())
                {
                    Pet pet = Pet.getActivePets().get(owner);
                    Player p = Bukkit.getPlayer(owner);

                    if(p != null)
                    {
                        boolean hasToBeRemoved = testState(p);

                        if(hasToBeRemoved)
                        {
                            pet.despawn();
                            Language.sendMessage(p, "§cVotre mascotte ne peut vous suivre dans cette zone.");
                        }

                    }

                }

            }
        }, 0L, 20L);
    }

    @Override
    public void stop() {
        Bukkit.getServer().getScheduler().cancelTask(task);
    }
}
