package com.hirises.combat.damage.data;

import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.damage.CombatManager;
import com.hirises.core.util.Util;
import org.bukkit.entity.LivingEntity;

import java.util.*;

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

    public List<List<DefencePenetrate>> getPartialPenetrates() {
        return Collections.unmodifiableList(partialPenetrates);
    }

    public List<Damage> getDamages() {
        return Collections.unmodifiableList(damages);
    }

    public void merge(DamageApplier other) {
        for(int i = 0; i < other.getDamages().size(); i++){
            damages.add(other.getDamages().get(i));
            partialPenetrates.add(other.getPenetrates());
        }
    }

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

    public void apply(LivingEntity entity, double amplification) {
        double finalRate = amplification * CombatManager.getDamageReduceRate(entity);
        CombatManager.damage(entity, getFinalDamage(entity) * finalRate);

        if(ConfigManager.useDamageMeter){
            Map<DamageTag, Double> finalDamageType = new HashMap<>();
            for(int i = 0; i < getDamages().size(); i++){
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
}
