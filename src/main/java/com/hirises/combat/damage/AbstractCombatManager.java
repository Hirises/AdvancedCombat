package com.hirises.combat.damage;

import org.bukkit.entity.LivingEntity;

public abstract class AbstractCombatManager {
    public static final int DAMAGE_MODIFIER = 10;

    public abstract void damage(LivingEntity entity, double damage);

    public abstract void heal(LivingEntity entity, double heal);
}
