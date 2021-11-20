package fr.nocsy.almpet.mythicmobs;

import fr.nocsy.almpet.mythicmobs.targeters.TargeterPetOwner;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.xikage.mythicmobs.skills.SkillTargeter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicListener implements Listener {

    @EventHandler
    public void onMythicTargeterLoad(MythicTargeterLoadEvent paramMythicTargeterLoadEvent) {

        String str = paramMythicTargeterLoadEvent.getTargeterName();

        if ("PETOWNER".equals(str.toUpperCase())) {
            paramMythicTargeterLoadEvent.register((SkillTargeter) new TargeterPetOwner(paramMythicTargeterLoadEvent.getConfig()));
        }

    }

}
