package com.hirises.combat;

import com.hirises.combat.item.CustomItemManager;
import com.hirises.combat.item.ItemSpawnListener;
import com.hirises.core.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdvancedCombat extends JavaPlugin {

    private static AdvancedCombat plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        Bukkit.getPluginManager().registerEvents(new ItemSpawnListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CustomItemManager(), plugin);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static AdvancedCombat getInst(){
        return plugin;
    }
}
