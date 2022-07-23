package com.hirises.combat;

import com.hirises.combat.command.CommandManager;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.manager.EventListener;
import com.hirises.combat.item.CustomItemManager;
import com.hirises.combat.item.ItemListener;
import com.hirises.core.store.NBTTagStore;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

//메인 클래스
public final class AdvancedCombat extends JavaPlugin {

    private static AdvancedCombat plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        ConfigManager.init();

        //이벤트 등록
        Bukkit.getPluginManager().registerEvents(new ItemListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CustomItemManager(), plugin);
        Bukkit.getPluginManager().registerEvents(new EventListener(), plugin);

        //커멘드 등록
        getCommand("combat").setExecutor(new CommandManager());
        getCommand("combat").setTabCompleter(new CommandManager());

        //혹시라도 남아있는 데미지 미터기 제거
        for(World world : Bukkit.getWorlds()){
            for(Entity entity : world.getEntities()){
                if(entity instanceof ArmorStand){
                    if(NBTTagStore.containKey(entity, Keys.DamageMeter.toString())){
                        entity.remove();
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static AdvancedCombat getInst(){
        return plugin;
    }
}
