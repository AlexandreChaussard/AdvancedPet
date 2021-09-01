package fr.nocsy.almpet.data.flags;

import com.sk89q.worldguard.protection.flags.StateFlag;
import fr.nocsy.almpet.AlmPet;

import java.util.ArrayList;

public class FlagsManager {

    public static StateFlag ALMPET;

    private static ArrayList<AbstractFlag> flags = new ArrayList<>();

    public static void init(AlmPet instance)
    {
        ArrayList<AbstractFlag> flags = new ArrayList<>();

        if(instance == null)
        {
            AlmPet.getLog().warning("L'instance de la classe mère est null. Impossible d'enregistrer les flags.");
            return;
        }

        flags.add(new DismountPetFlag(instance));
        flags.add(new DespawnPetFlag(instance));

        for(AbstractFlag flag : flags)
        {
            flag.register();
        }

    }

    public static void stopFlags()
    {
        for(AbstractFlag flag : flags)
        {
            if(flag instanceof StoppableFlag)
            {
                ((StoppableFlag) flag).stop();
            }
        }
    }

}
