package com.hirises.combat.damage.data;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.damage.CombatManager;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.Util;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FoodData implements DataUnit {
    private double instantHeal;
    private double healPerSecond;
    private int healDuration;
    private int coolDown;
    private List<Material> coolWith;

    public FoodData(){
        this.instantHeal = 0;
        this.healPerSecond = 0;
        this.healDuration = 0;
        this.coolDown = 0;
        this.coolWith = new ArrayList<>();
    }

    public double getInstantHeal() {
        return instantHeal;
    }

    public double getHealPerSecond() {
        return healPerSecond;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public int getHealDuration() {
        return healDuration;
    }

    public List<Material> getCoolWith() {
        return coolWith;
    }

    public void eat(Player player){
        CombatManager.heal(player, instantHeal);
        startCoolDown(player);
        double heal = healPerSecond / (20.0 / ConfigManager.foodDelay);
        int count = healDuration / ConfigManager.foodDelay + 1;
        new CancelableTask(AdvancedCombat.getInst(), ConfigManager.foodDelay, ConfigManager.foodDelay){
            int _count = count;

            @Override
            public void run() {
                if(!player.isOnline()){
                    cancel();
                }
                CombatManager.heal(player, heal);
                _count--;
                if(_count <= 0){
                    cancel();
                }
            }
        };
    }

    public void startCoolDown(Player player){
        for(Material mat : coolWith){
            player.setCooldown(mat, coolDown);
        }
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.instantHeal = yml.getToNumber(root + ".회복");
        this.healPerSecond = yml.getToNumber(root + ".초당회복");
        this.healDuration = (int)yml.getOrDefault(new TimeUnit(), root + ".회복시간").getToTick();
        this.coolDown = (int)yml.getOrDefault(new TimeUnit(), root + ".쿨타임").getToTick();
        this.coolWith = new ArrayList<>();
        String matStr = root.substring(root.lastIndexOf(".") + 1);
        coolWith.add(Material.valueOf(matStr));
        for(String mat : yml.getConfig().getStringList(root + ".쿨공유")){
            coolWith.add(Material.valueOf(mat));
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}
