package com.github.realms;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Bliss extends JavaPlugin {
    private static Bliss plugin;
    public HashMap<UUID, Sleeper> cache = new HashMap<>();
    public HashMap<UUID, String> deadplayers = new HashMap<>();

    @Override
    public void onEnable() {
        plugin = this;

        if (getDataFolder().listFiles() != null) {
            JSONParser parser = new JSONParser();
            for (File file : new File(getDataFolder().getPath() + "/data").listFiles()) {
                // Filter out the dead.yml
                if (!file.getName().contains("dead")) {
                    try {
                        FileReader reader = new FileReader(file);
                        JSONObject json = (JSONObject) parser.parse(reader);
                        cache.put(UUID.fromString((String) json.get("uuid")),new Sleeper(json));
                    } catch (IOException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Load Dead Players.
        if (new File("plugins/Bliss/data/dead.yml").exists()) {
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load("plugins/Bliss/data/dead.yml");
                config.getStringList("dead").forEach(s -> deadplayers.put(UUID.fromString(s.split(";")[0]), s.split(";")[1]));
            } catch (IOException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        getServer().getPluginManager().registerEvents(new BlissListener(), this);
    }

    @Override
    public void onDisable() {
        // Removing all Existing Data Files, in order to avoid any duplicates.
        if (getDataFolder().exists()) for (File file : new File(getDataFolder().getPath() + "/data").listFiles())
            if (!file.getName().contains("dead")) file.delete();

        // Serializing Cache
        saveData();

        // Remove Existing Entities
        Bukkit.getWorlds().forEach(world -> world.getLivingEntities().forEach(livingEntity -> cache.forEach((uuid, sleeper) -> {
            if (sleeper.entityid == livingEntity.getEntityId())
                livingEntity.remove();
        })));
    }

    private void saveData() {
        cache.forEach((uuid, sleeper) -> {
            try {
                File data = new File("plugins/Bliss/data/" + sleeper.getUuid().toString() + ".json");

                if (!data.exists()) new YamlConfiguration().save(data);

                FileWriter file = new FileWriter("plugins/Bliss/data/" + sleeper.getUuid().toString() + ".json");
                file.write(sleeper.toJson().toJSONString());
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
                getLogger().severe("Something went wrong during serialization of data. " + sleeper.getUuid().toString());
            }
        });

        // Dead Players saving, using YAML this time for ease of use.
        List<String> stringList = new ArrayList<>();
        deadplayers.forEach((uuid, s) -> stringList.add(uuid.toString() + ";" + s));

        YamlConfiguration config = new YamlConfiguration();
        config.set("dead", stringList);
        try {
            config.save("plugins/Bliss/data/dead.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Bliss getInstance() {
        return plugin;
    }

    public HashMap<UUID, Sleeper> getCache() {
        return cache;
    }

    public void setCache(HashMap<UUID, Sleeper> cache) {
        this.cache = cache;
    }

    public void addtoCache(Sleeper sleeper, UUID uuid) {
        List<Sleeper> remove = new ArrayList<>();
        cache.forEach((uuid1, sleeper1) -> {
            if (uuid1 == sleeper.getUuid())
                remove.add(sleeper1);
        });
        remove.forEach(sleeper1 -> removeCache(sleeper1));

        cache.put(uuid, sleeper);
    }

    public void removeCache(Sleeper sleeper) {
        List<Sleeper> remove = new ArrayList<>();

        cache.forEach((uuid, sleeper1) -> {
            if (uuid == sleeper.getUuid()) {
                remove.add(sleeper1);
                sleeper1.entity.remove();
            }
        });

        remove.forEach(sleeper1 -> cache.remove(sleeper1));
    }

    @Nullable
    public Sleeper EntitytoSleeper(Entity entity) {
        final Sleeper[] re = {null};
        cache.forEach((uuid, sleeper) -> {
            if (entity.getEntityId() == sleeper.entityid)
                re[0] = sleeper;
        });
        return re[0];
    }

    public boolean isPlayerDead(UUID uuid) {
        return deadplayers.containsKey(uuid);
    }

    public void addDeadPlayer(UUID player, String killer) {
        deadplayers.remove(player);
        deadplayers.put(player, killer);
    }

    public void removeDeadPlayer(UUID player) {
        deadplayers.remove(player);
    }

    public List<UUID> uuidCache() {
        List<UUID> re = new ArrayList<>();

        cache.forEach((uuid, sleeper) -> re.add(sleeper.getUuid()));

        return re;
    }
}
