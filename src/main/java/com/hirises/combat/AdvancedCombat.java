package com.hirises.combat;

import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.damage.EventListener;
import com.hirises.combat.damage.CombatManager;
import com.hirises.combat.item.CustomItemManager;
import com.hirises.combat.item.ItemListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdvancedCombat extends JavaPlugin {

    private static AdvancedCombat plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        ConfigManager.init();

        Bukkit.getPluginManager().registerEvents(new ItemListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CustomItemManager(), plugin);
        Bukkit.getPluginManager().registerEvents(new EventListener(), plugin);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static AdvancedCombat getInst(){
        return plugin;
    }
}
