package com.hirises.combat.damage.data;

import com.hirises.combat.damage.calculate.Damage;
import com.hirises.combat.damage.calculate.DamageApplier;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

//발사체 데미지 데이터
@Immutable
@ThreadSafe
public class ProjectileData implements DataUnit {
    private DamageApplier damage;

    public ProjectileData(){
        this.damage = new DamageApplier();
    }

    public ProjectileData(DamageApplier applier){
        this.damage = applier;
    }

    //해당 데미지를 추가 (원본 보존)
    public ProjectileData merge(List<Damage> data){
        List<Damage> copy = new ArrayList<>();
        copy.addAll(this.damage.getDamages());
        copy.addAll(data);
        return new ProjectileData(new DamageApplier(copy, this.damage.getPenetrates()));
    }

    public DamageApplier getDamage() {
        return damage;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damage = yml.getOrDefault(new DamageApplier(), root + ".데미지");
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    @Override
    public String toString() {
        return "ProjectileData{" +
                ", damage=" + damage +
                '}';
    }
}
