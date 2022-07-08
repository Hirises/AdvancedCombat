package com.hirises.combat.damage.data;

import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.damage.CombatManager;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.YamlStore;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DamageApplier implements DataUnit {
    private List<Damage> damages;
    private List<DefencePenetrate> penetrates;

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

    public DamageApplier(List<Damage> damages, List<DefencePenetrate> penetrates){
        this.damages = damages;
        this.penetrates = penetrates;
    }

    public List<Damage> getDamages() {
        return damages;
    }

    public void setDamages(List<Damage> damages) {
        this.damages = damages;
    }

    public double getFinalDamage(LivingEntity entity){
        double finalDamage = 0;
        for(Damage damage : damages){
            finalDamage += damage.getFinalDamage(entity, penetrates);
        }
        return finalDamage;
    }

    public void apply(LivingEntity entity){
        apply(entity, 1);
    }

    public void apply(LivingEntity entity, double amplification){
        CombatManager.damage(entity, getFinalDamage(entity) * amplification);

        if(ConfigManager.useDamageMeter){
            for(Damage damage : damages){
                double splitDamage = damage.getFinalDamage(entity, penetrates) * amplification;
                if(splitDamage > 0){
                    CombatManager.spawnDamageMeter(entity.getEyeLocation(),
                            ConfigManager.damageMeterData.getDamageMeterString(damage.getDamageTag(), splitDamage));
                }
            }
        }
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
                "damages=" + builder2 +
                '}';
    }
}
