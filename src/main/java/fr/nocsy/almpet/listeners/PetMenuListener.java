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
                            Language.REVOKED_FOR_NEW_ONE.sendMessage(p);
                            break;
                        case Pet.MOB_SPAWN:
                            Language.SUMMONED.sendMessage(p);
                            break;
                        case Pet.MYTHIC_MOB_NULL:
                            Language.MYTHICMOB_NULL.sendMessage(p);
                            break;
                        case Pet.NO_MOB_MATCH:
                            Language.NO_MOB_MATCH.sendMessage(p);
                            break;
                        case Pet.NOT_ALLOWED:
                            Language.NOT_ALLOWED.sendMessage(p);
                            break;
                        case Pet.OWNER_NULL:
                            Language.OWNER_NOT_FOUND.sendMessage(p);
                            break;
                    }
                }
            }

        }
    }

}
