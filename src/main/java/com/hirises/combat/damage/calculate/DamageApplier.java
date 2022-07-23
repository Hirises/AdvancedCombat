package com.hirises.combat.damage.calculate;

import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.damage.manager.CombatManager;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

//최종 (복합) 데미지 객체
public class DamageApplier implements DataUnit {
    private List<Damage> damages;   //적용될 데미지들
    private List<DefencePenetrate> penetrates;  //적용될 방어관통 (모든 데미지에 대해 적용됨)

    public DamageApplier(){
        this.damages = new ArrayList<>();
        this.penetrates = new ArrayList<>();
    }

    public DamageApplier(double damage, DamageTag damageTag){
        this(Arrays.asList(new Damage(damage, damageTag)));
    }

    public DamageApplier(List<Damage> damages){
        this(damages, new ArrayList<>());
    }

    public DamageApplier(DamageApplier origin){
        this.damages = new ArrayList<>();
        this.damages.addAll(origin.damages);
        this.penetrates = new ArrayList<>();
        this.penetrates.addAll(origin.penetrates);
    }

    public DamageApplier(List<Damage> damages, List<DefencePenetrate> penetrates){
        this.damages = damages;
        this.penetrates = penetrates;
    }

    //N배한 데미지를 반환 (원본 보존)
    public DamageApplier multiply(double value){
        return new DamageApplier(damages.stream().map(data -> data.multiply(value)).collect(Collectors.toList()), penetrates);
    }

    //해당 엔티티에 적용될 최종 데미지를 반환
    public double getFinalDamage(LivingEntity entity){
        //모든 단일 데미지 객체의 최종 데미지를 합쳐서 반환
        double finalDamage = 0;
        for(Damage damage : damages){
            finalDamage += damage.getFinalDamage(entity, penetrates);
        }
        return finalDamage;
    }

    //해당 엔티티에 이 데미지 적용
    public void apply(LivingEntity entity){
        apply(entity, 1);
    }

    //해당 엔티티에 이 데미지 적용 (최종적으로 N배 해서 적용한다)
    public void apply(LivingEntity entity, double amplification){
        double finalRate = amplification * CombatManager.getDamageReduceRate(entity);   //최종 데미지 배수
        CombatManager.damage(entity, getFinalDamage(entity) * finalRate);

        if(ConfigManager.useDamageMeter){
            //데미지 미터기 생성
            for(Damage damage : damages){
                double splitDamage = damage.getFinalDamage(entity, penetrates) * finalRate;
                if(splitDamage > 0){
                    CombatManager.spawnDamageMeter(entity.getEyeLocation(),
                            ConfigManager.damageMeterData.getDamageMeterString(damage.getDamageTag(), splitDamage));
                }
            }
        }
    }

    public List<DefencePenetrate> getPenetrates() {
        return Collections.unmodifiableList(penetrates);
    }

    public List<Damage> getDamages() {
        return Collections.unmodifiableList(damages);
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.damages = new ArrayList<>();
        this.penetrates = new ArrayList<>();
        for(String key : yml.getKeys(root)){
            if(key.equalsIgnoreCase("방어관통")){
                for(String _key : yml.getKeys(root + ".방어관통")){
                    this.penetrates.add(yml.getOrDefault(new DefencePenetrate(), root + ".방어관통." + _key));
                }
                continue;
            }

            damages.add(yml.getOrDefault(new Damage(), root + "." + key));
        }
    }

    @Override
    public void save(YamlStore yml, String root) {
        //No Use
    }

    @Override
    public String toString() {
        StringBuilder builder1 = new StringBuilder("[");
        for(Damage damage : damages){
            builder1.append(damage.toString());
            builder1.append(", ");
        }
        builder1.append("]");
        StringBuilder builder2 = new StringBuilder("[");
        for(DefencePenetrate penetrate : penetrates){
            builder2.append(penetrate.toString());
            builder2.append(", ");
        }
        builder2.append("]");
        return "SimpleDamageApplier{" +
                "damages=" + builder1 +
                "penetrate=" + builder2 +
                '}';
    }
}
