package fr.nocsy.almpet.data.flags;

import fr.nocsy.almpet.AdvancedPet;
import fr.nocsy.almpet.data.Language;
import fr.nocsy.almpet.data.Pet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DespawnPetFlag extends AbstractFlag implements StoppableFlag{

    public DespawnPetFlag(AdvancedPet instance) {
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
            AdvancedPet.getLog().warning(AdvancedPet.getLogName() + "Flag " + getFlagName() + " couldn't not be launched as it's null. Please contact Nocsy.");
            return;
        }
        else
        {
            AdvancedPet.getLog().info(AdvancedPet.getLogName() + "Starting flag " + getFlagName() + ".");
        }

        task = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(getAdvancedPetInstance(), new Runnable() {
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
                            Language.CANT_FOLLOW_HERE.sendMessage(p);
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
