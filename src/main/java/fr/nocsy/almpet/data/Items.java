package fr.nocsy.almpet.data;

import fr.nocsy.almpet.data.inventories.PlayerData;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

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
        meta.setDisplayName("§6Chevaucher");

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Cliquez ici pour monter");
        lore.add("§7sur votre mascotte");

        meta.setLore(lore);

        it.setItemMeta(meta);
        return it;
    }

    private static ItemStack rename()
    {
        ItemStack it = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName("§6Renommer");

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Cliquez ici pour renommer");
        lore.add("§7votre mascotte");

        meta.setLore(lore);

        it.setItemMeta(meta);
        return it;
    }

    public static ItemStack page(int index)
    {
        ItemStack it = new ItemStack(Material.PAPER);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName("§6Tourner la page");

        meta.setLocalizedName("AlmPetPage;" + index);

        ArrayList<String> lore = new ArrayList<>();
        lore.add("§7Cliquez §edroit§7 pour §eavancer");
        lore.add("§7Cliquez §agauche§7 pour §areculer");

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
            lore.add("§9Surnom : §7" + pd.getMapOfRegisteredNames().get(pet.getId()));
            lore.add(" ");
        }

        lore.add("§cCliquez ici pour révoquer");
        lore.add("§cvotre mascotte");

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
