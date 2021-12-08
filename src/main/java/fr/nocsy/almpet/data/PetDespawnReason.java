package fr.nocsy.almpet.data;

import lombok.Getter;

public enum PetDespawnReason {

    TELEPORT("teleport"),
    DEATH("death"),
    REVOKE("revoke"),
    REPLACED("replaced"),
    OWNER_NOT_HERE("owner not here"),
    RELOAD("reload"),
    FLAG("flag"),
    GAMEMODE("gamemode"),
    MYTHICMOBS("mythicmobs"),
    UNKNOWN("unkown");


    @Getter
    private String reason;

    PetDespawnReason(String reason)
    {
        this.reason = reason;
    }


}
