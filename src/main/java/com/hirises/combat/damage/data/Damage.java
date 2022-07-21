package com.hirises.combat.damage.data;

import com.hirises.combat.damage.CombatManager;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class Damage implements DataUnit, IHasDamageTagValue {
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
        if(damageTag.equalAttackType(DamageTag.AttackType.Const) && !damageTag.hasDamageType(DamageTag.DamageType.Fall)){
            return damage;
        }
        double defence = (CombatManager.getDefence(entity, damageTag, penetrates) + 100);
        if(defence < 34){
            defence = 34;
        }
        return (damage * 100) / defence;
    }

    public Damage multiply(double value){
        return new Damage(damage * value, damageTag);
    }

    @Override
    public DamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public double getValue() {
        return damage;
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
