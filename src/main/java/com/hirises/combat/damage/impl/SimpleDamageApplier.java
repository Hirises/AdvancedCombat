package com.hirises.combat.damage.impl;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleDamageApplier implements DataUnit {
    private List<SimpleDamage> damages;

    public SimpleDamageApplier(){
        this.damages = new ArrayList<>();
    }

    public SimpleDamageApplier(double damage, SimpleDamageTag damageTag){
        this(Arrays.asList(new SimpleDamage(damage, damageTag)));
    }

    public SimpleDamageApplier(List<SimpleDamage> damages){
        this.damages = damages;
    }

    public List<SimpleDamage> getDamages() {
        return damages;
    }

    public void setDamages(List<SimpleDamage> damages) {
        this.damages = damages;
    }

    public double getFinalDamage(LivingEntity entity){
        double finalDamage = 0;
        for(SimpleDamage damage : damages){
            finalDamage += damage.getFinalDamage(entity);
        }
        return finalDamage;
    }

    public void apply(LivingEntity entity){
        apply(entity, 1);
    }

    public void apply(LivingEntity entity, double amplification){
        AdvancedCombat.getCombatManager().damage(entity, getFinalDamage(entity) * amplification);

        if(ConfigManager.useDamageMeter){
            for(SimpleDamage damage : damages){
                double splitDamage = damage.getFinalDamage(entity) * amplification;
                if(splitDamage > 0){
                    ((SimpleCombatManager) AdvancedCombat.getCombatManager()).spawnDamageMeter(entity.getEyeLocation(),
                            ConfigManager.damageMeterData.getDamageMeterString(damage.getDamageTag(), splitDamage));
                }
            }
        }
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damages = new ArrayList<>();
        for(String key : yml.getKeys(root)){
            damages.add(yml.getOrDefault(new SimpleDamage(), root + "." + key));
        }
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        for(SimpleDamage damage : damages){
            builder.append(damage.toString());
            builder.append(", ");
        }
        builder.append("]");
        return "SimpleDamageApplier{" +
                "damages=" + builder +
                '}';
    }
}
