package com.hirises.combat.config;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.data.ArmorData;
import com.hirises.combat.damage.data.WeaponData;
import com.hirises.combat.damage.data.DamageTag;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    public final static YamlStore config = new YamlStore(AdvancedCombat.getInst(), "config.yml");
    public static YamlStore settings = new YamlStore(AdvancedCombat.getInst(), "normal_settings.yml");

    public static boolean useDamageMeter = false;
    public record DamageMeterData(
      String format,
      int duration,

      String normalColor,
      String physicsColor,
      String magicColor,
      String constColor,
      String healColor,

      String fireSymbol,
      String projectileSymbol,
      String explosionSymbol
    )
    {
        public String getHealMeterString(double heal){
            return Util.remapStrings(format, Util.toRemap(
                    "color",  healColor,
                    "symbol", "",
                    "damage", Util.safeToString(heal)
            ));
        }

        public String getDamageMeterString(DamageTag tag, double damage){
            String color = "";
            String symbol = "";

            switch (tag.getAttackType()){
                case Normal -> color = normalColor;
                case Physics -> color = physicsColor;
                case Magic -> color = magicColor;
                case Const -> color = constColor;
            }

            for(DamageTag.DamageType type : tag.getDamageTypes()){
                switch (type){
                    case Fire -> symbol += fireSymbol;
                    case Projectile -> symbol += projectileSymbol;
                    case Explosion -> symbol += explosionSymbol;
                }
            }

            return Util.remapStrings(format, Util.toRemap(
                    "color",  color,
                    "symbol", symbol,
                    "damage", Util.safeToString(damage)
            ));
        }
    }
    public static DamageMeterData damageMeterData;
    public static WeaponData bearHand;
    public static Map<Material, WeaponData> weaponDataMap;
    public static Map<Material, ArmorData> armorDataMap;
    public record WeightData(
        int normalSpeedRate,
        int maxWeight,
        double speedRatePerWeight
    )
    {
        public double getSpeedRate(int weight){
            if(weight > maxWeight){
                weight = maxWeight;
            }
            return normalSpeedRate - (speedRatePerWeight * weight);
        }
    }
    public static WeightData weightData;

    public static void init() {
        config.load(false);
        settings.load(false);

        useDamageMeter = settings.get(Boolean.class, "데미지미터기.사용");
        if (useDamageMeter) {
            damageMeterData = new DamageMeterData(
                    settings.getToString("데미지미터기.형태"),
                    (int) settings.getOrDefault(new TimeUnit(), "데미지미터기.지속시간").getToTick(),

                    Util.remapColor(settings.getToString("데미지미터기.색상.일반")),
                    Util.remapColor(settings.getToString("데미지미터기.색상.물리")),
                    Util.remapColor(settings.getToString("데미지미터기.색상.마법")),
                    Util.remapColor(settings.getToString("데미지미터기.색상.고정")),
                    Util.remapColor(settings.getToString("데미지미터기.색상.회복")),

                    Util.remapColor(settings.getToString("데미지미터기.심볼.화염")),
                    Util.remapColor(settings.getToString("데미지미터기.심볼.원거리")),
                    Util.remapColor(settings.getToString("데미지미터기.심볼.폭발"))
            );
        }

        weightData = new WeightData(
                settings.get(Integer.class, "무게.기본이속"),
                settings.get(Integer.class, "무게.최대무게"),
                settings.getToNumber("무게.무게당이속감소")
        );

        bearHand = settings.getOrDefault(new WeaponData(), "무기.맨손");
        weaponDataMap = new HashMap<>();
        for (String key : settings.getKeys("무기")) {
            if (key.equalsIgnoreCase("맨손")) {
                continue;
            }

            weaponDataMap.put(Material.valueOf(key), settings.getOrDefault(new WeaponData(), "무기." + key));
        }

        armorDataMap = new HashMap<>();
        for (String key : settings.getKeys("갑옷")) {
            armorDataMap.put(Material.valueOf(key), settings.getOrDefault(new ArmorData(), "갑옷." + key));
        }
    }
}
