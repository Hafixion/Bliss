package com.github.realms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Bliss extends JavaPlugin {
    public static Bliss plugin;

    @Override
    public void onEnable() {
        plugin = this;
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(new BlissListener(), this);
    }
}
