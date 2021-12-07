package fr.nocsy.almpet;

import lombok.Getter;

public enum PPermission {

    USE("advancedpet.use"),
    ADMIN("advancedpet.admin"),
    COLOR("advancedpet.color");

    @Getter
    private final String permission;

    PPermission(String permission)
    {
        this.permission = permission;
    }

}
