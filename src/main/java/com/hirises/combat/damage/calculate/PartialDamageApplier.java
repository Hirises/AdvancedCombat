package com.hirises.combat.damage.calculate;

import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.damage.manager.CombatManager;
import org.bukkit.entity.LivingEntity;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.*;

//복합적 분할 데미지 객체 (방어 관통을 각각 적용할 수 있음)
@NotThreadSafe
public class PartialDamageApplier{
    private List<Damage> damages;
    private List<List<DefencePenetrate>> partialPenetrates;

    public PartialDamageApplier(){
        this(new ArrayList<>(), new ArrayList<>());
    }

    public PartialDamageApplier(List<Damage> damages, List<List<DefencePenetrate>> partialPenetrates){
        this.damages = damages;
        this.partialPenetrates = partialPenetrates;
    }

    //해당 데미지 객체를 이 객체에 적용시킨다
    public void merge(DamageApplier other) {
        for(int i = 0; i < other.getDamages().size(); i++){
            damages.add(other.getDamages().get(i));
            partialPenetrates.add(other.getPenetrates());
        }
    }

    //해당 엔티티에 적용될 최종 데미지를 반환
    public double getFinalDamage(LivingEntity entity) {
        double finalDamage = 0;
        for(int i = 0; i < getDamages().size(); i++){
            Damage damage = getDamages().get(i);
            finalDamage += damage.getFinalDamage(entity, partialPenetrates.get(i));
        }
        if(finalDamage < 0){
            return 0;
        }
        return finalDamage;
    }

    //해당 엔티티에 데미지를 적용
    public void apply(LivingEntity entity, double amplification) {
        double finalRate = amplification * CombatManager.getDamageReduceRate(entity);   //최종 데미지 배수
        CombatManager.damage(entity, getFinalDamage(entity) * finalRate);

        if(ConfigManager.useDamageMeter){
            //데미지 미터 생성
            Map<DamageTag, Double> finalDamageType = new HashMap<>();
            for(int i = 0; i < getDamages().size(); i++){   //각 데미지 태그 종류별로 데미지 합산
                Damage damage = getDamages().get(i);
                double splitDamage = damage.getFinalDamage(entity, partialPenetrates.get(i)) * finalRate;
                finalDamageType.put(damage.getDamageTag(), finalDamageType.getOrDefault(damage.getDamageTag(), 0.0) + splitDamage);
            }

            for(DamageTag tag : finalDamageType.keySet()){
                if(finalDamageType.get(tag) > 0){
                    CombatManager.spawnDamageMeter(entity.getEyeLocation(),
                            ConfigManager.damageMeterData.getDamageMeterString(tag, finalDamageType.get(tag)));
                }
            }
        }
    }

    public List<List<DefencePenetrate>> getPartialPenetrates() {
        return Collections.unmodifiableList(partialPenetrates);
    }

    public List<Damage> getDamages() {
        return Collections.unmodifiableList(damages);
    }
}
