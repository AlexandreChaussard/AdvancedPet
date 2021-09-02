package fr.nocsy.almpet.data;

import fr.nocsy.almpet.AlmPet;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum Language {

    INVENTORY_PETS_MENU("§0☀ §4Mascottes §0☀"),
    INVENTORY_PETS_MENU_INTERACTIONS("§0☀ §4Mascotte §0☀"),

    MOUNT_ITEM_NAME("§6Chevaucher"),
    MOUNT_ITEM_DESCRIPTION("§7Cliquez ici pour monter \n§7sur votre mascotte"),

    RENAME_ITEM_NAME("§6Renommer"),
    RENAME_ITEM_DESCRIPTION("§7Cliquez ici pour renommer \n§7votre mascotte"),

    TURNPAGE_ITEM_NAME("§6Tourner la page"),
    TURNPAGE_ITEM_DESCRIPTION("§7Cliquez §edroit§7 pour §eavancer \n§7Cliquez §agauche§7 pour §areculer"),

    NICKNAME("§9Surnom : §7%nickname%"),
    NICKNAME_ITEM_LORE("§cCliquez ici pour révoquer \n§cvotre mascotte"),

    SUMMONED("§7Une mascotte vient d'être invoquée !"),
    REVOKED("§7Votre mascotte a été révoquée."),
    REVOKED_FOR_NEW_ONE("§7Votre mascotte précédente a été révoquée afin d'invoquer la nouvelle."),
    MYTHICMOB_NULL("§cImpossible d'invoquer cette mascotte. Le mythicMob associé est null."),
    NO_MOB_MATCH("§cImpossible d'invoquer cette mascotte. Le mythicMob associé n'a pas été trouvé dans MythicMobs."),
    NOT_ALLOWED("§cVous n'avez pas encore débloqué cette mascotte."),
    OWNER_NOT_FOUND("§cImpossible d'invoquer cette mascotte. Le propriétaire n'a pas été trouvé."),
    REVOKED_BEFORE_CHANGES("§cVotre mascotte a été révoquée avant que vous ne puissiez effectuer des modifications."),
    NOT_MOUNTABLE("§cImpossible de monter sur cette mascotte."),
    NOT_MOUNTABLE_HERE("§cVous ne pouvez pas monter votre mascotte dans cette zone."),
    CANT_FOLLOW_HERE("§cVotre mascotte ne peut vous suivre dans cette zone."),
    TYPE_NAME_IN_CHAT("§aÉcrivez dans le chat le nom que vous souhaitez donner à votre mascotte"),
    IF_WISH_TO_REMOVE_NAME("§aSi vous souhaitez le retirer, écrivez §cAucun§a dans le chat."),
    NICKNAME_CHANGED_SUCCESSFULY("§aSurnom changé avec succès !"),

    PLAYER_NOT_CONNECTED("§cLe joueur §6%player%§c n'est pas connecté."),

    RELOAD_SUCCESS("§aReload effectué avec succès."),
    HOW_MANY_PETS_LOADED("§a%numberofpets% pets ont été enregistrés avec succès."),

    USAGE("§7Utilisation : §6/almpet <reload/open/opento> <player>");

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
