package com.hirises.combat.damage;

import org.bukkit.entity.LivingEntity;

public abstract class AbstractCombatManager {
    public abstract void damage(LivingEntity entity, double damage);
}
