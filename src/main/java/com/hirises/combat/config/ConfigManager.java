package com.hirises.combat.config;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.impl.SimpleDamage;
import com.hirises.combat.damage.impl.SimpleDamageTag;
import com.hirises.core.armorstand.ArmorStandWrapper;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class ConfigManager {
    public final static YamlStore settings = new YamlStore(AdvancedCombat.getInst(), "settings.yml");

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
    ) {
        public String getHealMeterString(double heal){
            return Util.remapStrings(format, Util.toRemap(
                    "color",  healColor,
                    "symbol", "",
                    "damage", Util.safeToString(heal)
            ));
        }

        public String getDamageMeterString(SimpleDamageTag tag, double damage){
            String color = "";
            String symbol = "";

            switch (tag.getAttackType()){
                case Normal -> color = normalColor;
                case Physics -> color = physicsColor;
                case Magic -> color = magicColor;
                case Const -> color = constColor;
            }

            for(SimpleDamageTag.DamageType type : tag.getDamageTypes()){
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

    public static void init(){
        settings.load(false);

        useDamageMeter = settings.get(Boolean.class, "데미지미터기.사용");
        if(useDamageMeter){
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
    }
}
