package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

public class WeaponData implements DataUnit {
    private double attackDistance;
    private int weight;
    private double attackSpeed;
    private DamageApplier damage;

    public WeaponData(){
        this.attackDistance = 2;
        this.weight = 0;
        this.attackSpeed = 1;
        this.damage = new DamageApplier();
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.attackDistance = yml.getToNumber(root + ".리치");
        this.weight = yml.get(Integer.class, root + ".무게");
        this.attackSpeed = yml.getToNumber(root + ".공속");
        this.damage = yml.getOrDefault(new DamageApplier(), root + ".데미지");
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    public double getAttackDistance() {
        return attackDistance;
    }

    public int getWeight() {
        return weight;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public DamageApplier getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "WeaponData{" +
                "attackDistance=" + attackDistance +
                ", weight=" + weight +
                ", attackSpeed=" + attackSpeed +
                ", damage=" + damage +
                '}';
    }
}
