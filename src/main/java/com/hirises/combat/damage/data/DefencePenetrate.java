package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

public class DefencePenetrate implements DataUnit {
    private double rate;
    private DamageTag damageTag;

    public DefencePenetrate(){
        this.rate = 0;
        this.damageTag = new DamageTag(DamageTag.AttackType.Const);
    }

    public DefencePenetrate(double rate, DamageTag damageTag){
        this.rate = rate;
        this.damageTag = damageTag;
    }

    public DamageTag getDamageTag() {
        return damageTag;
    }

    public double getRate() {
        return rate;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.rate = yml.getToNumber(root + ".관통률");
        this.damageTag = yml.getOrDefault(new DamageTag(), root);
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }

    @Override
    public String toString() {
        return "SimpleDefencePenetrate{" +
                "rate=" + rate +
                ", damageTag=" + damageTag.toString() +
                '}';
    }
}
