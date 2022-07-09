package com.hirises.combat.config;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.damage.CombatManager;
import com.hirises.combat.damage.data.*;
import com.hirises.combat.item.CustomItemManager;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.store.YamlStore;
import com.hirises.core.util.ItemUtil;
import com.hirises.core.util.Util;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

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
                    "damage", Util.safeToString((int)Math.floor(heal))
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
                    "damage", Util.safeToString((int)Math.floor(damage))
            ));
        }
    }
    public static DamageMeterData damageMeterData;
    public static WeaponData bearHand;
    public static ProjectileData normalArrow;
    public static ArmorData shield;
    public static Map<Material, WeaponData> weaponDataMap;
    public static Map<Material, ArmorData> armorDataMap;
    public static Map<Material, ProjectileData> projectileDataMap;
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
    public static boolean useItemLore = false;
    public record ItemLoreData(
        List<String> loreFormat,

        String attackDamageLine,
        String penetrateLine,
        String projectileDamageLine,
        String defenceLine,
        String attributeLine,

        String attackSpeedFormat,
        String attackDistanceFormat,
        String weightFormat,

        String damagePropertiesFormat,
        String fireTag,
        String projectileTag,
        String explosionTag,
        String tagSeparator,
        String tagPrefix,
        String tagSuffix,

        String normalFormat,
        String physicsFormat,
        String magicFormat,
        String constFormat,
        String attackTypeSeparator
    )
    {
        public List<String> getItemLore(ItemStack item){
            int weight = 0;
            double attackSpeed = -1;
            double attackDistance = -1;
            StringBuilder builder = new StringBuilder();
            if(CombatManager.hasArmorData(item)){
                ArmorData data = CombatManager.getArmorData(item);
                builder.append(defenceLine);
                builder.append("/n");
                builder.append(getIHasDamageTagValueLore(data.getDefences()));
                builder.append("/n");

                weight += data.getWeight();
            }
            if(CombatManager.hasWeaponData(item)){
                WeaponData data = CombatManager.getWeaponData(item);
                builder.append(attackDamageLine);
                builder.append("/n");
                builder.append(getIHasDamageTagValueLore(data.getDamage().getDamages()));
                if(data.getDamage().getPenetrates().size() > 0){
                    builder.append(penetrateLine);
                    builder.append("/n");
                    builder.append(getIHasDamageTagValueLore(data.getDamage().getPenetrates()));
                    builder.append("/n");
                }

                weight += data.getWeight();
                attackSpeed = data.getAttackSpeed();
                attackDistance = data.getAttackDistance();
            }
            if(CombatManager.hasProjectileData(item)){
                ProjectileData data = CombatManager.getProjectileData(item);
                builder.append(projectileDamageLine);
                builder.append("/n");
                builder.append(getIHasDamageTagValueLore(data.getDamage().getDamages()));
                if(data.getDamage().getPenetrates().size() > 0){
                    builder.append(penetrateLine);
                    builder.append("/n");
                    builder.append(getIHasDamageTagValueLore(data.getDamage().getPenetrates()));
                    builder.append("/n");
                }
            }
            if(weight > 0 || attackSpeed > 0 || attackDistance > 0){
                builder.append(attributeLine);
                builder.append("/n");
                if(attackSpeed > 0){
                    builder.append(Util.remapString(attackSpeedFormat, "value", String.format("%.2f", attackSpeed)));
                    builder.append("/n");
                }
                if(attackDistance > 0){
                    builder.append(Util.remapString(attackDistanceFormat, "value", String.format("%.2f", attackDistance)));
                    builder.append("/n");
                }
                builder.append(Util.remapString(weightFormat, "value", Util.safeToString(weight)));
                builder.append("/n");
            }
            return Util.trimAllLine(Util.stringToList(builder.toString()));
        }

        public String getIHasDamageTagValueLore(List<? extends IHasDamageTagValue> values){
            Map<Integer, List<IHasDamageTagValue>> tagMap = new HashMap<>();
            StringBuilder builder = new StringBuilder();
            for(IHasDamageTagValue data : values) {
                tagMap.compute(data.getDamageTag().getDamageTypeFlag(), (key, value) -> {
                    if (value == null) {
                        value = new ArrayList<>();
                    }
                    value.add(data);
                    return value;
                });
            }
            for(int key : tagMap.keySet().stream().sorted(Integer::compareTo).collect(Collectors.toList())){
                EnumSet<DamageTag.DamageType> tags = DamageTag.getDamageTypeFromFlag(key);
                StringBuilder innerBuilder = new StringBuilder();
                if(tags.size() > 0){
                    innerBuilder.append(tags.stream().map((value) -> {
                        switch (value){
                            case Explosion -> {
                                return explosionTag;
                            }
                            case Fire -> {
                                return fireTag;
                            }
                            case Projectile -> {
                                return projectileTag;
                            }
                            default -> {
                                return "";
                            }
                        }
                    }).collect(Collectors.joining(tagSeparator, tagPrefix, tagSuffix)));
                }
                List<String> valueForType = new ArrayList<>();
                for(DamageTag.AttackType attackType : DamageTag.AttackType.values()){
                    double value = 0;
                    for(IHasDamageTagValue data : tagMap.get(key)){
                        if(data.getDamageTag().equalAttackType(attackType)){
                            value += data.getValue();
                        }
                    }
                    if(value != 0){
                        switch (attackType){
                            case Normal -> valueForType.add(Util.remapString(normalFormat, "value", String.format("%d", (int)Math.floor(value))));
                            case Physics -> valueForType.add(Util.remapString(physicsFormat, "value", String.format("%d", (int)Math.floor(value))));
                            case Magic -> valueForType.add(Util.remapString(magicFormat, "value", String.format("%d", (int)Math.floor(value))));
                            case Const -> valueForType.add(Util.remapString(constFormat, "value", String.format("%d", (int)Math.floor(value))));
                        }
                    }
                }
                innerBuilder.append(valueForType.stream().collect(Collectors.joining(attackTypeSeparator)));
                builder.append(innerBuilder);
                builder.append("/n");
            }

            return builder.toString();
        }
    }
    public static ItemLoreData itemLoreData;

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

        useItemLore = settings.get(Boolean.class, "아이템로어.사용");
        if (useItemLore) {
            itemLoreData = new ItemLoreData(
                    Util.remapColor(settings.getConfig().getStringList("아이템로어.형태.전체")),

                    Util.remapColor(settings.getToString("아이템로어.형태.구분선.공격력")),
                    Util.remapColor(settings.getToString("아이템로어.형태.구분선.방어관통")),
                    Util.remapColor(settings.getToString("아이템로어.형태.구분선.발사체")),
                    Util.remapColor(settings.getToString("아이템로어.형태.구분선.방어력")),
                    Util.remapColor(settings.getToString("아이템로어.형태.구분선.속성")),

                    Util.remapColor(settings.getToString("아이템로어.형태.공격속도")),
                    Util.remapColor(settings.getToString("아이템로어.형태.공격거리")),
                    Util.remapColor(settings.getToString("아이템로어.형태.무게")),

                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.전체")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.태그.화염")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.태그.발사체")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.태그.폭발")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.태그.구분")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.태그.접두사")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.태그.접미사")),

                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.데미지.일반")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.데미지.물리")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.데미지.마법")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.데미지.고정")),
                    Util.remapColor(settings.getToString("아이템로어.형태.데미지속성.데미지.구분"))
            );
        }

        weightData = new WeightData(
                settings.get(Integer.class, "무게.기본이속"),
                settings.get(Integer.class, "무게.최대무게"),
                settings.getToNumber("무게.무게당이속감소")
        );

        weaponDataMap = new HashMap<>();
        armorDataMap = new HashMap<>();
        projectileDataMap = new HashMap<>();
        bearHand = settings.getOrDefault(new WeaponData(), "무기.맨손");
        normalArrow = settings.getOrDefault(new ProjectileData(), "발사체.기본");

        for (String key : settings.getKeys("무기")) {
            if (key.equalsIgnoreCase("맨손")) {
                continue;
            }

            Material mat = Material.valueOf(key);
            WeaponData data = settings.getOrDefault(new WeaponData(), "무기." + key);
            weaponDataMap.put(mat, data);

            ItemStack item = CustomItemManager.hasRegisteredItemReplacement(mat) ? CustomItemManager.getRegisteredItemReplacement(mat) : new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,
                    new AttributeModifier(
                            UUID.randomUUID(),
                            Keys.Attribute_Modifier.toString(),
                            data.getAttackSpeed() - bearHand.getAttackSpeed(),
                            AttributeModifier.Operation.ADD_NUMBER,
                            EquipmentSlot.HAND
                    )
            );
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if(useItemLore){
                meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
            }
            item.setItemMeta(meta);

            CustomItemManager.registerItemReplacement(mat, item);
        }

        for (String key : settings.getKeys("갑옷")) {
            Material mat = Material.valueOf(key);
            ArmorData data = settings.getOrDefault(new ArmorData(), "갑옷." + key);
            armorDataMap.put(mat, data);

            if(useItemLore){
                ItemStack item = CustomItemManager.hasRegisteredItemReplacement(mat) ? CustomItemManager.getRegisteredItemReplacement(mat) : new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                item.setItemMeta(meta);

                CustomItemManager.registerItemReplacement(mat, item);
            }
        }

        for (String key : settings.getKeys("발사체")) {
            if(key.equals("기본")){
                continue;
            }

            Material mat = Material.valueOf(key);
            ProjectileData data = settings.getOrDefault(new ProjectileData(), "발사체." + key);
            projectileDataMap.put(mat, data);

            if(useItemLore){
                ItemStack item = CustomItemManager.hasRegisteredItemReplacement(mat) ? CustomItemManager.getRegisteredItemReplacement(mat) : new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
                item.setItemMeta(meta);

                CustomItemManager.registerItemReplacement(mat, item);
            }
        }

        shield = settings.getOrDefault(new ArmorData(), "기타.방패");
        armorDataMap.put(Material.SHIELD, shield);
        ItemStack item = CustomItemManager.hasRegisteredItemReplacement(Material.SHIELD) ?
                CustomItemManager.getRegisteredItemReplacement(Material.SHIELD) : new ItemStack(Material.SHIELD);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.setLore(ConfigManager.itemLoreData.getItemLore(item));
        item.setItemMeta(meta);
        CustomItemManager.registerItemReplacement(Material.SHIELD, item);
        WeaponData data = new WeaponData(bearHand.getAttackDistance(), shield.getWeight(), bearHand.getAttackDistance(), new DamageApplier(bearHand.getDamage()));
        weaponDataMap.put(Material.SHIELD, data);
    }
}
