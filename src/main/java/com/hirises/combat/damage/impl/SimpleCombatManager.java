package com.hirises.combat.damage.impl;

import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.AbstractCombatManager;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.util.Util;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;


public class SimpleCombatManager extends AbstractCombatManager {
    public static final int DAMAGE_MODIFIER = 10;

    @Override
    public void damage(LivingEntity entity, double damage) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            NBTTagStore.set(entity, Keys.Current_Health.toString(), getMaxHealth(entity));
        }
        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        Util.logging("E", entity.getType(), "D", damage, "H", health);
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
        health += heal;
        if(health > getMaxHealth(entity)){
            health = getMaxHealth(entity);
        }
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);
        applyHealth(entity);
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
}
