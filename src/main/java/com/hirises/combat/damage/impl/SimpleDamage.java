package com.hirises.combat.damage.impl;

import com.hirises.combat.AdvancedCombat;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.entity.LivingEntity;

public class SimpleDamage implements DataUnit {
    private double damage;
    private SimpleDamageTag damageTag;

    public SimpleDamage(){
        this.damage = 0;
        this.damageTag = new SimpleDamageTag();
    }

    public SimpleDamage(double damage, SimpleDamageTag damageTag){
        this.damage = damage;
        this.damageTag = damageTag;
    }

    public double getFinalDamage(LivingEntity entity){
        if(damageTag.equalAttackType(SimpleDamageTag.AttackType.Const)){
            return damage;
        }
        return (damage * 100) / (((SimpleCombatManager) AdvancedCombat.getCombatManager()).getDefence(entity, damageTag) + 100);
    }

    public SimpleDamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damage = yml.getToNumber(root + ".데미지");
        this.damageTag = yml.getOrDefault(new SimpleDamageTag(), root);
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    @Override
    public String toString() {
        return "SimpleDamage{" +
                "damage=" + damage +
                ", type=" + damageTag.getAttackType() +
                ", tags=" + damageTag.getDamageTypes().toArray() +
                '}';
    }
}
