package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;

import java.util.List;

public class Defence implements DataUnit {
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

    public double getFinalDefence(DamageTag damageTag, List<DefencePenetrate> penetrates){
        if(this.damageTag.checkDefenceType(damageTag)){
            double output = defence;
            for(DefencePenetrate penetrate : penetrates){
                output = penetrate.reduceDefence(output, this.damageTag);
            }
            return output;
        }
        return 0;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.defence = yml.getToNumber(root + ".방어");
        this.damageTag = yml.getOrDefault(new DamageTag(), root);
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}
