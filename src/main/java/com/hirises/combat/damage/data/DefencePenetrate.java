package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

public class DefencePenetrate implements DataUnit, IHasDamageTagValue {
    private double rate;    //0.2, 0.3 같은 형식. '깍을' 수치이다. 실제 적용시 '1-rate'로 사용
    private DamageTag damageTag;

    public DefencePenetrate(){
        this.rate = 0;
        this.damageTag = new DamageTag(DamageTag.AttackType.Const);
    }

    public DefencePenetrate(double rate, DamageTag damageTag){
        this.rate = rate;
        this.damageTag = damageTag;
    }

    @Override
    public DamageTag getDamageTag() {
        return damageTag;
    }

    @Override
    public double getValue() {
        return rate * 100;
    }

    public double getRate() {
        return rate;
    }

    public double reduceDefence(double defence, DamageTag damageTag){
        if(damageTag.checkDefencePenetrateType(this.damageTag)){
            if(defence > 0){
                return defence * (1 - rate);
            }else{
                return defence * (1 + rate);
            }
        }
        return defence;
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
