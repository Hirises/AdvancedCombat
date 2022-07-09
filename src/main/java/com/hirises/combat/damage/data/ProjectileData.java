package com.hirises.combat.damage.data;

import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

public class ProjectileData implements DataUnit {
    private DamageApplier damage;

    public ProjectileData(){
        this.damage = new DamageApplier();
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
