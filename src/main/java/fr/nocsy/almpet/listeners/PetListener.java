package fr.nocsy.almpet.listeners;

import fr.nocsy.almpet.AdvancedPet;
import fr.nocsy.almpet.data.GlobalConfig;
import fr.nocsy.almpet.data.Pet;
import fr.nocsy.almpet.data.inventories.PetInteractionMenu;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.UUID;

public class PetListener implements Listener {

    @EventHandler
    public void interact(PlayerInteractEntityEvent e)
    {
        if(!GlobalConfig.getInstance().isRightClickToOpen())
            return;

        Player p = e.getPlayer();
        Entity ent = e.getRightClicked();

        Pet pet = Pet.getFromEntity(ent);

        if(pet != null &&
            (pet.getOwner().equals(p.getUniqueId()) || p.isOp()))
        {
            PetInteractionMenu menu = new PetInteractionMenu(pet);
            p.setMetadata("AlmPetInteracted", new FixedMetadataValue(AdvancedPet.getInstance(), pet));
            menu.open(p);
        }
    }

    @EventHandler
    public void interact(EntityDamageByEntityEvent e)
    {
        if(!GlobalConfig.getInstance().isLeftClickToOpen())
            return;

        if(!(e.getDamager() instanceof Player))
            return;

        Player p = (Player)e.getDamager();
        Entity ent = e.getEntity();

        Pet pet = Pet.getFromEntity(ent);

        if(pet != null &&
                (pet.getOwner().equals(p.getUniqueId()) || p.isOp()))
        {
            PetInteractionMenu menu = new PetInteractionMenu(pet);
            p.setMetadata("AlmPetInteracted", new FixedMetadataValue(AdvancedPet.getInstance(), pet));
            menu.open(p);
            e.setCancelled(true);
            e.setDamage(0);
        }
    }

    @EventHandler
    public void teleport(PlayerChangedWorldEvent e)
    {
        Player p = e.getPlayer();
        if(Pet.getActivePets().containsKey(p.getUniqueId()))
        {
            Pet pet = Pet.getActivePets().get(p.getUniqueId());
            pet.despawn();
            pet.spawn(p, p.getLocation());
        }

    }

    @EventHandler
    public void riding(EntityDamageEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            Player p = (Player) e.getEntity();
            if(p.isInsideVehicle() && Pet.fromOwner(p.getUniqueId()) != null)
            {
                Pet pet = Pet.fromOwner(p.getUniqueId());
                pet.dismount(p);
            }
            return;
        }

    }

    @EventHandler
    public void damaged(EntityDamageEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            Pet pet = Pet.getFromEntity(e.getEntity());
            if(pet != null && pet.isInvulnerable())
            {
                e.setDamage(0);
                e.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler
    public void gamemode(PlayerGameModeChangeEvent e)
    {
        UUID uuid = e.getPlayer().getUniqueId();
        if(Pet.getActivePets().containsKey(uuid) && e.getNewGameMode() == GameMode.SPECTATOR)
        {
            Pet pet = Pet.getActivePets().get(uuid);
            pet.despawn();
        }
    }

}
