package com.hirises.combat.damage.data;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.CombatManager;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class Damage implements DataUnit {
    private double damage;
    private DamageTag damageTag;

    public Damage(){
        this.damage = 0;
        this.damageTag = new DamageTag();
    }

    public Damage(double damage, DamageTag damageTag){
        this.damage = damage;
        this.damageTag = damageTag;
    }

    public double getFinalDamage(LivingEntity entity, List<DefencePenetrate> penetrates){
        if(damageTag.equalAttackType(DamageTag.AttackType.Const)){
            return damage;
        }
        return (damage * 100) / (((CombatManager) AdvancedCombat.getCombatManager()).getDefence(entity, damageTag, penetrates) + 100);
    }

    public DamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damage = yml.getToNumber(root + ".데미지");
        this.damageTag = yml.getOrDefault(new DamageTag(), root);
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