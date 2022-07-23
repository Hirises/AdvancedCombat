package com.hirises.combat.damage.data;

import com.hirises.combat.damage.calculate.Damage;
import com.hirises.combat.damage.calculate.DamageApplier;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

//무기 데미지 데이터
@Immutable
@ThreadSafe
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

    public WeaponData(double attackDistance, int weight, double attackSpeed, DamageApplier damage){
        this.attackSpeed = attackSpeed;
        this.weight = weight;
        this.attackDistance = attackDistance;
        this.damage = damage;
    }

    //해당 데미지를 추가 (원본 보존)
    public WeaponData merge(List<Damage> data){
        List<Damage> copy = new ArrayList<>();
        copy.addAll(this.damage.getDamages());
        copy.addAll(data);
        return new WeaponData(attackDistance, weight, attackSpeed, new DamageApplier(copy, this.damage.getPenetrates()));
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
