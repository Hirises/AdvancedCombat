package com.hirises.combat.damage.impl;

import com.hirises.combat.damage.AbstractCombatManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.MetadataStore;


public class SimpleCombatManager extends AbstractCombatManager {
    private static final int DAMAGE_MODIFIER = 10;

    @Override
    public void damage(LivingEntity entity, double damage) {
        
    }

    public int getDefence(LivingEntity entity, SimpleDamageTag tag){
        return 0;
    }
}
