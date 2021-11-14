package fr.nocsy.almpet.data;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Language {

    INVENTORY_PETS_MENU("§0☀ §4Pets §0☀"),
    INVENTORY_PETS_MENU_INTERACTIONS("§0☀ §4Pet §0☀"),

    MOUNT_ITEM_NAME("§6Mount"),
    MOUNT_ITEM_DESCRIPTION("§7Click to mount your pet"),

    RENAME_ITEM_NAME("§6Rename"),
    RENAME_ITEM_DESCRIPTION("§7Click to rename your pet"),

    BACK_TO_PETMENU_ITEM_NAME("§cBack to menu"),
    BACK_TO_PETMENU_ITEM_DESCRIPTION("§7Click to get back to the menu"),

    TURNPAGE_ITEM_NAME("§6Next page"),
    TURNPAGE_ITEM_DESCRIPTION("§eRight click§7 to go forward \n§aLeft click§7 to go backward"),

    NICKNAME("§9Nickname : §7%nickname%"),
    NICKNAME_ITEM_LORE("§cClick here to revoke your pet"),

    SUMMONED("§7A pet has been summoned !"),
    REVOKED("§7Your pet was revoked."),
    REVOKED_FOR_NEW_ONE("§7Your previous pet was revoked to summon the new one."),
    MYTHICMOB_NULL("§cThis pet could not be summoned. The associated mythicMob is null."),
    NO_MOB_MATCH("§cThis pet could not be summoned. The associated mythicmob isn't registered in MythicMobs."),
    NOT_ALLOWED("§cYou're not allowed to summon this pet."),
    OWNER_NOT_FOUND("§cThis pet could not be summoned. The summoner couldn't be found."),
    REVOKED_BEFORE_CHANGES("§cYour pet was revoked before the modifications could take place."),
    NOT_MOUNTABLE("§cThis pet has no mounting point."),
    NOT_MOUNTABLE_HERE("§cYou can't ride a pet in this area."),
    CANT_FOLLOW_HERE("§cYour pet can't follow you in this area."),
    TYPE_NAME_IN_CHAT("§aRight down in the chat the name of your pet."),
    IF_WISH_TO_REMOVE_NAME("§aSIf you wish to remove it, right §c%tag%§a in the chat."),
    NICKNAME_CHANGED_SUCCESSFULY("§aNickname successfully changed !"),
    TAG_TO_REMOVE_NAME("None"),

    PLAYER_NOT_CONNECTED("§cThe player §6%player%§c isn't connected."),

    RELOAD_SUCCESS("§aReloaded successfully."),
    HOW_MANY_PETS_LOADED("§a%numberofpets% were registered successfully"),

    USAGE("§7Usage : §6/almpet <reload/open/opento> <player>"),
    NO_PERM("§cYou're not allowed to use this command.");

    @Getter
    private String message;

    Language(String message)
    {
        this.message = message;
    }

    public void reload()
    {
        if(LanguageConfig.getInstance().getMap().containsKey(this.name().toLowerCase()))
        {
            this.message = LanguageConfig.getInstance().getMap().get(this.name().toLowerCase());
        }
    }

    public void sendMessage(Player p)
    {
        p.sendMessage(GlobalConfig.getInstance().getPrefix() + " " + message);
    }
    public void sendMessage(CommandSender sender)
    {
        sender.sendMessage(GlobalConfig.getInstance().getPrefix() + " " + message);
    }

    public void sendMessageFormated(CommandSender sender, FormatArg... args)
    {
        String toSend = new String(message);
        for(FormatArg arg : args)
        {
            toSend = arg.applyToString(toSend);
        }
        sender.sendMessage(GlobalConfig.getInstance().getPrefix() + " " + toSend);
    }

    public String getMessageFormatted(FormatArg... args)
    {
        String toSend = new String(message);
        for(FormatArg arg : args)
        {
            toSend = arg.applyToString(toSend);
        }
        return toSend;
    }

}
