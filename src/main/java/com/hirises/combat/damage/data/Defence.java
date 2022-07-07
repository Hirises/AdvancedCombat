package com.hirises.combat.damage.data;

import java.util.List;

public class Defence {
    private double defence;
    private DamageTag damageTag;

    public Defence(){
        this.defence = 0;
        damageTag = new DamageTag(DamageTag.AttackType.Normal);
    }

    public Defence(double defence, DamageTag damageTag){
        this.defence = defence;
        this.damageTag = damageTag;
    }

    public DamageTag getDamageTag() {
        return damageTag;
    }

    public double getDefence() {
        return defence;
    }

    public double getFinalDefence(List<DefencePenetrate> penetrates){
        double output = defence;
        for(DefencePenetrate penetrate : penetrates){
            output = penetrate.reduceDefence(output, damageTag);
        }
        return output;
    }
}
