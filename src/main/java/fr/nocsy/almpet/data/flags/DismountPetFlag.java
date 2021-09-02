package fr.nocsy.almpet.data.flags;

import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import fr.nocsy.almpet.AlmPet;
import fr.nocsy.almpet.data.Language;
import fr.nocsy.almpet.data.Pet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DismountPetFlag extends AbstractFlag implements StoppableFlag {

    public DismountPetFlag(AlmPet instance) {
        super("dismountPet", false, instance);
    }

    @Override
    public void register()
    {
        super.register();
        launch();
    }

    private int task;
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

                    if(!pet.isMountable())
                        continue;

                    Player p = Bukkit.getPlayer(owner);

                    if(p != null)
                    {
                        if(!pet.hasMount(p))
                            continue;

                        boolean hasToBeEjected = testState(p);

                        if(hasToBeEjected)
                        {
                            pet.dismount(p);
                            Language.NOT_MOUNTABLE_HERE.sendMessage(p);
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
