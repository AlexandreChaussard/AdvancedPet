package fr.nocsy.almpet.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Utils {

    @SuppressWarnings("deprecation")
    public static ItemStack createHead(String name, List<String> lore, String base64)
    {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        item.setDurability((short)3);
        SkullMeta headMeta = (SkullMeta)item.getItemMeta();

        headMeta.setDisplayName(name);
        headMeta.setLore(lore);

        item.setItemMeta(headMeta);
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", base64));
        Field profileField = null;
        try
        {
            profileField = headMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(headMeta, profile);
        }
        catch (NoSuchFieldException localNoSuchFieldException) {}catch (IllegalArgumentException localIllegalArgumentException) {}catch (IllegalAccessException localIllegalAccessException) {}
        item.setItemMeta(headMeta);
        return item;
    }

    public static double distance(Location loc1, Location loc2)
    {
        double x1 = loc1.getX();
        double y1 = loc1.getY();
        double z1 = loc1.getZ();

        double x2 = loc2.getX();
        double y2 = loc2.getY();
        double z2 = loc2.getZ();

        double square = (Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2) + Math.pow(z1 - z2, 2));

        return Math.sqrt(square);

    }

    /**
     * Location bruiser
     * @param loc
     * @return
     */
    public static Location bruised(Location loc, double distance)
    {
        Random random = new Random();
        double r = Math.min(1, distance) + (Math.max(distance-0.1, 1) - Math.min(1, distance)) * random.nextDouble();
        double theta = 2*Math.PI * random.nextDouble();

        double x = r*Math.cos(theta) + loc.getX();
        double z = r*Math.sin(theta) + loc.getZ();
        double y = loc.getY();

        loc = new Location(loc.getWorld(), x, y, z);

        int maxY = 0;
        while(!loc.getBlock().isPassable() && maxY < loc.getWorld().getMaxHeight())
        {
            loc.add(0, 1, 0);
            maxY++;
        }
        return loc;
    }

}
