package fr.nocsy.almpet.data.config;

import fr.nocsy.almpet.AdvancedPet;
import lombok.Getter;

import java.util.ArrayList;

public class BlacklistConfig extends AbstractConfig {

    public static BlacklistConfig instance;

    @Getter
    private ArrayList<String> blackListedWords = new ArrayList<>();

    public static BlacklistConfig getInstance()
    {

        if(instance == null)
            instance = new BlacklistConfig();

        return instance;
    }

    public void init()
    {
        super.init("", "blacklist.yml");
        save();
        reload();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void reload() {

        loadConfig();

        blackListedWords.clear();
        blackListedWords.addAll(getConfig().getStringList("Blacklist"));

        AdvancedPet.getLog().info(AdvancedPet.getLogName() + "Blacklist file reloaded.");
    }

}
