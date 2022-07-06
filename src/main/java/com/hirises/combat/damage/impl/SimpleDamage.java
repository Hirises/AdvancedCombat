package com.hirises.combat.damage.impl;

import com.hirises.combat.AdvancedCombat;
import org.bukkit.entity.LivingEntity;
public class SimpleDamage {

    private double damage;
    private SimpleDamageTag damageTag;

    public SimpleDamage(double damage, SimpleDamageTag damageTag){
        this.damage = damage;
        this.damageTag = damageTag;
    }

    public double getFinalDamage(LivingEntity entity){
        if(damageTag.equalAttackType(SimpleDamageTag.AttackType.Const)){
            return damage;
        }
        return damage / ((SimpleCombatManager) AdvancedCombat.getCombatManager()).getDefence(entity, damageTag) + 100;
    }

    public SimpleDamageTag getDamageTag() {
        return damageTag;
    }
}
