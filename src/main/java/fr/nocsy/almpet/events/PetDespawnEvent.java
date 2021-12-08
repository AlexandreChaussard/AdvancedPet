package fr.nocsy.almpet.events;

import fr.nocsy.almpet.data.Pet;
import fr.nocsy.almpet.data.PetDespawnReason;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PetDespawnEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private final Pet pet;
    @Getter
    private final PetDespawnReason reason;

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public PetDespawnEvent(Pet pet, PetDespawnReason reason)
    {
        this.pet = pet;
        this.reason = reason;
    }

}
