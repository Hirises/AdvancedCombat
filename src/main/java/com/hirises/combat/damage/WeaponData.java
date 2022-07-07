package com.hirises.combat.damage;

import com.hirises.combat.damage.impl.SimpleDamageApplier;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;

public class WeaponData implements DataUnit {
    private double reach;
    private int weight;
    private double attackSpeed;
    private SimpleDamageApplier damage;

    public WeaponData(){
        this.reach = 2;
        this.weight = 0;
        this.attackSpeed = 1;
        this.damage = new SimpleDamageApplier();
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.reach = yml.getToNumber(root + ".리치");
        this.weight = yml.get(Integer.class, root + ".무게");
        this.attackSpeed = yml.getToNumber(root + ".공속");
        this.damage = yml.getOrDefault(new SimpleDamageApplier(), root + ".데미지");
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    public double getReach() {
        return reach;
    }

    public int getWeight() {
        return weight;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public SimpleDamageApplier getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "WeaponData{" +
                "reach=" + reach +
                ", weight=" + weight +
                ", attackSpeed=" + attackSpeed +
                ", damage=" + damage +
                '}';
    }
}
