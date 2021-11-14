package fr.nocsy.almpet.listeners;

import fr.nocsy.almpet.data.FormatArg;
import fr.nocsy.almpet.data.Items;
import fr.nocsy.almpet.data.Language;
import fr.nocsy.almpet.data.Pet;
import fr.nocsy.almpet.data.inventories.PetInteractionMenu;
import fr.nocsy.almpet.data.inventories.PetMenu;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class PetInteractionMenuListener implements Listener {

    @EventHandler
    public void click(InventoryClickEvent e)
    {
        if(e.getView().getTitle().equalsIgnoreCase(PetInteractionMenu.getTitle()))
        {
            e.setCancelled(true);

            Player p = (Player) e.getWhoClicked();

            if(e.getClickedInventory() == null)
            {
                openBackPetMenu(p);
                return;
            }

            ItemStack it = e.getCurrentItem();
            if(it != null && it.hasItemMeta() && it.getItemMeta().hasDisplayName())
            {
                if(it.getItemMeta().hasLocalizedName() && it.getItemMeta().getLocalizedName().equals(Items.PETMENU.getItem().getItemMeta().getLocalizedName()))
                {
                    openBackPetMenu(p);
                    return;
                }

                Pet pet = Pet.getFromLastInteractedWith(p);
                if(pet == null)
                {
                    p.closeInventory();
                    return;
                }

                if(!pet.isStillHere())
                {
                    Language.REVOKED_BEFORE_CHANGES.sendMessage(p);
                    p.closeInventory();
                    return;
                }

                if(it.isSimilar(Items.MOUNT.getItem()))
                {
                    if(!pet.setMount(p))
                    {
                        Language.NOT_MOUNTABLE.sendMessage(p);
                    }
                }
                else if(it.isSimilar(Items.RENAME.getItem()))
                {
                    if(!waitingForAnswer.contains(p.getUniqueId()))
                        waitingForAnswer.add(p.getUniqueId());

                    Language.TYPE_NAME_IN_CHAT.sendMessage(p);
                    Language.IF_WISH_TO_REMOVE_NAME.sendMessageFormated(p, new FormatArg("%tag%", Language.TAG_TO_REMOVE_NAME.getMessage()));
                }
                else if(e.getSlot() == 2)
                {
                    pet.despawn();
                    Language.REVOKED.sendMessage(p);
                }
                p.closeInventory();
            }

        }

    }

    @Getter
    private ArrayList<UUID> waitingForAnswer = new ArrayList<>();

    @EventHandler
    public void chat(AsyncPlayerChatEvent e)
    {
        Player p = e.getPlayer();

        if(waitingForAnswer.contains(p.getUniqueId()))
        {
            waitingForAnswer.remove(p.getUniqueId());
            e.setCancelled(true);

            String name = e.getMessage();
            name = ChatColor.translateAlternateColorCodes('&', name);

            Pet pet = Pet.getFromLastInteractedWith(p);

            if(pet != null && pet.isStillHere())
            {
                pet.setDisplayName(name, true);

                Language.NICKNAME_CHANGED_SUCCESSFULY.sendMessage(p);
            }
            else
            {
                Language.REVOKED_BEFORE_CHANGES.sendMessage(p);
            }

        }
    }

    private void openBackPetMenu(Player p)
    {
        UUID uuid = p.getUniqueId();

        PetMenu menu = new PetMenu(p, 0, false);
        menu.open(p);
    }

}
