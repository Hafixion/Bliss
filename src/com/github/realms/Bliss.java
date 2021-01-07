package com.github.realms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class Bliss extends JavaPlugin {
    public static Bliss plugin;
    public static List<Integer> entities = new ArrayList<>();

    @Override
    public void onEnable() {
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new BlissListener(), this);
    }

    public static Bliss getInstance() {
        return plugin;
    }
}
