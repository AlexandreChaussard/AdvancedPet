package fr.nocsy.almpet.data;

import fr.nocsy.almpet.data.inventories.PlayerData;
import lombok.Getter;
import org.apache.commons.codec.language.bm.Lang;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;

public enum Items {

    MOUNT("mount"),
    RENAME("rename");

    @Getter
    private ItemStack item;

    Items(String name)
    {
        switch (name)
        {
            case "mount":
                item = mount();
                break;
            case "rename":
                item = rename();
                break;
        }
    }

    private static ItemStack mount()
    {
        ItemStack it = new ItemStack(Material.SADDLE);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Language.MOUNT_ITEM_NAME.getMessage());

        ArrayList<String> lore = new ArrayList<>(Arrays.asList(Language.MOUNT_ITEM_DESCRIPTION.getMessage().split("\n")));
        meta.setLore(lore);

        it.setItemMeta(meta);
        return it;
    }

    private static ItemStack rename()
    {
        ItemStack it = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Language.RENAME_ITEM_NAME.getMessage());

        ArrayList<String> lore = new ArrayList<>(Arrays.asList(Language.RENAME_ITEM_DESCRIPTION.getMessage().split("\n")));
        meta.setLore(lore);

        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack page(int index)
    {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(Language.TURNPAGE_ITEM_NAME.getMessage());

        meta.setLocalizedName("AlmPetPage;" + index);

        ArrayList<String> lore = new ArrayList<>(Arrays.asList(Language.TURNPAGE_ITEM_DESCRIPTION.getMessage().split("\n")));
        meta.setLore(lore);

        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack petInfo(Pet pet)
    {
        Pet objectPet = Pet.getFromId(pet.getId());

        ItemStack it = objectPet.getIcon().clone();
        ItemMeta meta = it.getItemMeta();

        ArrayList<String> lore = new ArrayList<>(meta.getLore());

        PlayerData pd = new PlayerData(pet.getOwner());

        if(pd.getMapOfRegisteredNames().containsKey(pet.getId()))
        {
            lore.add(" ");
            lore.add(Language.NICKNAME.getMessageFormatted(new FormatArg("%nickname%", pd.getMapOfRegisteredNames().get(pet.getId()))));
            lore.add(" ");
        }

        lore.addAll(Arrays.asList(Language.NICKNAME_ITEM_LORE.getMessage().split("\n")));

        meta.setLore(lore);

        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack deco(Material mat)
    {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName("§0");

        ArrayList<String> lore = new ArrayList<>();
        meta.setLore(lore);

        it.setItemMeta(meta);

        return it;
    }

}
