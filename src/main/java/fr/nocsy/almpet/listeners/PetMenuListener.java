package fr.nocsy.almpet.listeners;

import fr.nocsy.almpet.data.GlobalConfig;
import fr.nocsy.almpet.data.Language;
import fr.nocsy.almpet.data.Pet;
import fr.nocsy.almpet.data.inventories.PetMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PetMenuListener implements Listener {

    @EventHandler
    public void click(InventoryClickEvent e)
    {
        if(e.getView().getTitle().equals(PetMenu.getTitle()))
        {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack it = e.getCurrentItem();
            if(it != null)
            {
                if(it.hasItemMeta() && it.getItemMeta().hasLocalizedName() && it.getItemMeta().getLocalizedName().contains("AlmPetPage;"))
                {
                    int page = Integer.parseInt(it.getItemMeta().getLocalizedName().split(";")[1]);
                    p.closeInventory();
                    if(e.getClick() == ClickType.LEFT)
                    {
                        PetMenu menu = new PetMenu(p, Math.max(page-1, 0), true);
                        menu.open(p);
                    }
                    else
                    {
                        PetMenu menu = new PetMenu(p, page+1, true);
                        menu.open(p);
                    }
                    return;
                }

                Pet petObject = Pet.getFromIcon(it);
                if(petObject != null)
                {
                    p.closeInventory();
                    Pet pet = petObject.copy();
                    int executed = pet.spawn(p, p.getLocation());

                    switch(executed)
                    {
                        case Pet.DESPAWNED_PREVIOUS:
                            Language.sendMessage(p, "§7Votre compagnon précédent a été révoqué afin d'invoquer le nouveau.");
                            break;
                        case Pet.MOB_SPAWN:
                            Language.sendMessage(p, "§7Un compagnon vient d'être invoqué !");
                            break;
                        case Pet.MYTHIC_MOB_NULL:
                            Language.sendMessage(p, "§cImpossible d'invoquer ce compagnon. Le mythicMob associé est null.");
                            break;
                        case Pet.NO_MOB_MATCH:
                            Language.sendMessage(p, "§cImpossible d'invoquer ce compagnon. Le mythicMob associé n'a pas été trouvé dans MythicMobs.");
                            break;
                        case Pet.NOT_ALLOWED:
                            Language.sendMessage(p, "§cVous n'avez pas encore débloqué ce compagnon.");
                            break;
                        case Pet.OWNER_NULL:
                            Language.sendMessage(p, "§cImpossible d'invoquer ce compagnon. Le propriétaire n'a pas été trouvé.");
                            break;
                    }
                }
            }

        }
    }

}
