package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import java.util.ArrayList;
import java.util.List;

public class ProjectileData implements DataUnit {
    private DamageApplier damage;

    public ProjectileData(){
        this.damage = new DamageApplier();
    }

    public ProjectileData(DamageApplier applier){
        this.damage = applier;
    }

    public ProjectileData merge(List<Damage> data){
        List<Damage> copy = new ArrayList<>();
        copy.addAll(this.damage.getDamages());
        copy.addAll(data);
        return new ProjectileData(new DamageApplier(copy, this.damage.getPenetrates()));
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damage = yml.getOrDefault(new DamageApplier(), root + ".데미지");
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    public DamageApplier getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "ProjectileData{" +
                ", damage=" + damage +
                '}';
    }
}
