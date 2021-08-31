package fr.nocsy.almpet.data.inventories;

import fr.nocsy.almpet.data.AbstractConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class PlayerData extends AbstractConfig {

    @Getter
    public HashMap<String, String> mapOfRegisteredNames = new HashMap<>();

    @Getter
    private final UUID uuid;

    public PlayerData(UUID uuid)
    {
        this.uuid = uuid;
        init();
        save();
    }

    public void init()
    {
        super.init("PlayerData", uuid.toString() + ".yml");

        if(getConfig().get("Names") == null)
            getConfig().set("Names", new ArrayList<String>());

        reload();
    }

    @Override
    public void save() {

        ArrayList<String> serializedMap = new ArrayList<>();

        for(String id : mapOfRegisteredNames.keySet())
        {
            String name = mapOfRegisteredNames.get(id);
            String seria = id + ";" + name;
            serializedMap.add(seria);
        }

        getConfig().set("Names", serializedMap);

        super.save();
    }

    @Override
    public void reload() {

        mapOfRegisteredNames.clear();

        for(String seria : getConfig().getStringList("Names"))
        {
            String[] table = seria.split(";");
            String id = table[0];
            String name = table[1];

            mapOfRegisteredNames.put(id, name);
        }

    }
}
