package fr.nocsy.almpet.commands;

import fr.nocsy.almpet.AdvancedPet;
import fr.nocsy.almpet.data.*;
import fr.nocsy.almpet.data.inventories.PetMenu;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdvancedPetCommand implements CCommand {
    @Override
    public String getName() {
        return "advancedpet";
    }

    @Override
    public String getPermission() {
        return "advancedpet.use";
    }

    public String getAdminPermission() {
        return "advancedpet.admin";
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase(getName()) && sender.hasPermission(getPermission()))
        {
            if(args.length == 4)
            {
                if(args[0].equalsIgnoreCase("spawn")
                        && sender.hasPermission(getAdminPermission()))
                {

                    String petId = args[1];
                    String playerName = args[2];
                    String booleanValue = args[3];

                    Player target = Bukkit.getPlayer(playerName);
                    if(target == null)
                    {
                        Language.PLAYER_NOT_CONNECTED.sendMessageFormated(sender, new FormatArg("%player%", playerName));
                        return;
                    }

                    Pet petObject = Pet.getFromId(petId);
                    if(petObject == null)
                    {
                        Language.PET_DOESNT_EXIST.sendMessage(sender);
                        return;
                    }
                    Pet pet = petObject.copy();

                    boolean checkPermission = booleanValue.equalsIgnoreCase("true");
                    if(checkPermission && !target.hasPermission(pet.getPermission()))
                    {
                        Language.NOT_ALLOWED.sendMessage(target);
                        return;
                    }
                    pet.setCheckPermission(checkPermission);
                    pet.spawnWithMessage(target, target.getLocation());
                    return;
                }
            }
            else if(args.length == 2)
            {
                if(args[0].equalsIgnoreCase("open")
                        && sender.hasPermission(getAdminPermission())
                        && sender instanceof Player)
                {
                    String playerName = args[1];
                    Player playerToOpen = Bukkit.getPlayer(playerName);
                    if(playerToOpen == null)
                    {
                        Language.PLAYER_NOT_CONNECTED.sendMessageFormated(sender, new FormatArg("%player%", playerName));
                        return;
                    }

                    PetMenu menu = new PetMenu(playerToOpen, 0, false);
                    menu.open((Player)sender);
                    return;
                }
            }
            else if(args.length == 1)
            {
                if(args[0].equalsIgnoreCase("reload")
                        && sender.hasPermission(getAdminPermission()) )
                {
                    AdvancedPet.loadConfigs();
                    Language.RELOAD_SUCCESS.sendMessage(sender);
                    Language.HOW_MANY_PETS_LOADED.sendMessageFormated(sender, new FormatArg("%numberofpets%", Integer.toString(Pet.getObjectPets().size())));
                    return;
                }
            }
            else if(args.length == 0
                    && sender instanceof Player)
            {
                PetMenu menu = new PetMenu((Player)sender, 0, false);
                menu.open((Player)sender);
                return;
            }

            if(sender.hasPermission(getAdminPermission()))
                Language.USAGE.sendMessage(sender);
        }
        else
        {
            Language.NO_PERM.sendMessage(sender);
        }
    }
}
