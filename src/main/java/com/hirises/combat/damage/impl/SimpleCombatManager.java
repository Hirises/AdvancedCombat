package com.hirises.combat.damage.impl;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.AbstractCombatManager;
import com.hirises.core.armorstand.ArmorStandWrapper;
import com.hirises.core.store.NBTTagStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;


public class SimpleCombatManager extends AbstractCombatManager {
    @Override
    public void damage(LivingEntity entity, double damage) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            NBTTagStore.set(entity, Keys.Current_Health.toString(), getMaxHealth(entity));
        }
        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        if(health <= damage){
            entity.remove();
            return;
        }
        health -= damage;
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);
        applyHealth(entity);
    }

    @Override
    public void heal(LivingEntity entity, double heal) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            return;
        }
        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        if(health + heal > getMaxHealth(entity)){
            heal = getMaxHealth(entity) - health;
        }
        health += heal;
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);
        applyHealth(entity);
        if(ConfigManager.useDamageMeter && heal > 0){
            ((SimpleCombatManager) AdvancedCombat.getCombatManager()).spawnDamageMeter(entity.getEyeLocation(),
                    ConfigManager.damageMeterData.getHealMeterString(heal));
        }
    }

    public void applyHealth(LivingEntity entity){
        if(entity == null){
            return;
        }
        entity.setHealth(NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class) / DAMAGE_MODIFIER);
    }

    public double getMaxHealth(LivingEntity entity){
        return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * DAMAGE_MODIFIER;
    }

    public int getDefence(LivingEntity entity, SimpleDamageTag tag){
        return 0;
    }

    public void spawnDamageMeter(Location loc, String string){
        ArmorStandWrapper meter = ArmorStandWrapper.getInstance(
                loc.add(Vector.getRandom().add(new Vector(-0.5, 0, -0.5)))
        );
        meter.asMark();
        meter.get().setCustomName(string);
        meter.get().setCustomNameVisible(true);
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            meter.get().remove();
        }, ConfigManager.damageMeterData.duration());
    }
}