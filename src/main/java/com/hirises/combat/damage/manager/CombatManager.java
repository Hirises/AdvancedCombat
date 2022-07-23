package com.hirises.combat.damage.manager;

import com.google.common.util.concurrent.AtomicDouble;
import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.calculate.DamageTag;
import com.hirises.combat.damage.calculate.DefencePenetrate;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//전투에 관련된 처리를 담당하는 유틸 클래스
public class CombatManager {
    public final static int DAMAGE_MODIFIER = 10;   //기본 데미지 배수

    //<editor-fold desc="음식 초당 힐 처리">
    public final static Map<LivingEntity, FoodTask> foodMap = new HashMap<>();  //음식 초당 힐 테스크 맵
    private static class FoodTask {     //음식 초당힐 처리 객체
        private LivingEntity entity;    //대상 엔티티
        private AtomicDouble heal;      //현재 초당 힐량
        private CancelableTask task;    //처리 테스크

        public FoodTask(LivingEntity entity){
            this.entity = entity;
            this.heal = new AtomicDouble();
            this.task = null;
        }

        //유효성 검사
        public boolean validCheck() {
            if(this.entity == null){
                heal = null;
                if(task != null){
                    task.cancel();
                }
                task = null;
                entity = null;
                foodMap.remove(this);
                return true;
            }
            return false;
        }

        //초당 힐량 추가
        public void addHeal(double value){
            if (validCheck()) return;   //유효성 검사

            this.heal.addAndGet(value);

            synchronized (entity){
                if(this.task == null){
                    this.task = new CancelableTask(AdvancedCombat.getInst(), ConfigManager.foodDelay, ConfigManager.foodDelay) {
                        @Override
                        public void run() {
                            double amount = heal.get();
                            if(amount <= 0.01){ //힐량이 0일 경우
                                cancel();
                                task = null;
                                heal.set(0);
                                return;
                            }
                            if(!entity.isValid()){  //유효성 검사
                                cancel();
                                heal = null;
                                task = null;
                                entity = null;
                                foodMap.remove(this);
                                return;
                            }

                            heal(entity, amount);
                        }
                    };
                }
            }
        }

        //초당 힐량 제거
        public void removeHeal(double value){
            if (validCheck()) return;   //유효성 검사

            this.heal.addAndGet(-value);
        }

        //테스크 취소
        public void cancelTask(){
            heal = null;
            if(task != null){
                task.cancel();
            }
            task = null;
            entity = null;
            foodMap.remove(this);
        }
    }

    //초당 힐 시작
    public static void startHealGradually(LivingEntity entity, double heal){
        foodMap.computeIfAbsent(entity, value -> new FoodTask(value)).addHeal(heal);
    }

    //초당 힐 종료
    public static void endHealGradually(LivingEntity entity, double heal){
        if(!foodMap.containsKey(entity)){
            return;
        }
        foodMap.get(entity).removeHeal(heal);
    }
    //</editor-fold>

    //대상 엔티티에 대미지 적용
    public static void damage(LivingEntity entity, double damage) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            NBTTagStore.set(entity, Keys.Current_Health.toString(), getMaxHealth(entity));
        }

        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        health -= damage;
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);

        applyHealth(entity);
    }

    //대상 엔티티에 힐 적용
    public static void heal(LivingEntity entity, double heal) {
        if(!NBTTagStore.containKey(entity, Keys.Current_Health.toString())){
            return;
        }

        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class);
        if(health + heal > getMaxHealth(entity)){   //최대체력 검증
            heal = getMaxHealth(entity) - health;
        }
        health += heal;
        NBTTagStore.set(entity, Keys.Current_Health.toString(), health);

        applyHealth(entity);

        if(ConfigManager.useDamageMeter && heal > 0){
            //데미지 미터 소환
            spawnDamageMeter(entity.getEyeLocation(), ConfigManager.damageMeterData.getHealMeterString(heal));
        }
    }

    //커스텀 체력 적용
    public static void applyHealth(LivingEntity entity){
        if(entity == null){
            return;
        }

        double health = NBTTagStore.get(entity, Keys.Current_Health.toString(), Double.class) / DAMAGE_MODIFIER;

        if(health <= 0){
            health = 0;
            //<editor-fold desc="불사의 토템 검사">
            if(entity instanceof InventoryHolder){
                if(!(entity instanceof Player) || ((Player)entity).getCooldown(Material.TOTEM_OF_UNDYING) == 0){  //쿨다운 검사
                    InventoryHolder holder = (InventoryHolder) entity;
                    if(holder.getInventory().contains(Material.TOTEM_OF_UNDYING)){  //인벤 검사
                        holder.getInventory().removeItem(new ItemStack(Material.TOTEM_OF_UNDYING));
                        //토템 적용
                        applyTotemOfUndying(entity);
                        return;
                    }else if(entity.getEquipment().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)){ //왼손 검사
                        entity.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                        //토템 적용
                        applyTotemOfUndying(entity);
                        return;
                    }
                }
            }else{
                if(entity.getEquipment().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)){  //오른손 검사
                    entity.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }else if(entity.getEquipment().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING)){ //왼손 검사
                    entity.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
                    //토템 적용
                    applyTotemOfUndying(entity);
                    return;
                }
            }
            //</editor-fold>
        }
        entity.setHealth(health);
    }

    //불사의 토템 적용
    public static void applyTotemOfUndying(LivingEntity entity){
        NBTTagStore.set(entity, Keys.Current_Health.toString(), 0);
        ConfigManager.undyingTotem.eat(entity, 1);
    }

    //배수 처리된 최대 체력 가져오기
    public static double getMaxHealth(LivingEntity entity){
        return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * DAMAGE_MODIFIER;
    }

    public static double getDefence(LivingEntity entity, DamageTag tag, List<DefencePenetrate> penetrates){
        EntityEquipment equipment = entity.getEquipment();
        double defence = 0;
        if(ItemUtil.isExist(equipment.getHelmet())){
            defence += ConfigManager.getNewArmorData(equipment.getHelmet()).getFinalDefence(tag, penetrates);
        }
        if(ItemUtil.isExist(equipment.getChestplate())){
            defence += ConfigManager.getNewArmorData(equipment.getChestplate()).getFinalDefence(tag, penetrates);
        }
        if(ItemUtil.isExist(equipment.getLeggings())){
            defence += ConfigManager.getNewArmorData(equipment.getLeggings()).getFinalDefence(tag, penetrates);
        }
        if(ItemUtil.isExist(equipment.getBoots())){;
            defence += ConfigManager.getNewArmorData(equipment.getBoots()).getFinalDefence(tag, penetrates);
        }
        if(entity instanceof Player){
            Player player = (Player) entity;
            if(player.isBlocking()){
                defence += ConfigManager.getNewArmorData(new ItemStack(Material.SHIELD)).getFinalDefence(tag, penetrates);
            }
        }
        return defence;
    }

    public static int getWeight(LivingEntity entity){
        EntityEquipment equipment = entity.getEquipment();
        return getWeight(equipment.getItemInMainHand(), equipment.getItemInOffHand(), equipment.getHelmet(),
                equipment.getChestplate(), equipment.getLeggings(), equipment.getBoots());
    }

    public static int getWeight(ItemStack main, ItemStack off, ItemStack helmet, ItemStack chest, ItemStack leggings, ItemStack boots){
        int weight = 0;
        if(ItemUtil.isExist(main)){
            weight += ConfigManager.getWeaponData(main).getWeight();
        }
        if(ItemUtil.isExist(off)){
            weight += ConfigManager.getWeaponData(off).getWeight();
        }
        if(ItemUtil.isExist(helmet)){
            weight += ConfigManager.getNewArmorData(helmet).getWeight();
        }
        if(ItemUtil.isExist(chest)){
            weight += ConfigManager.getNewArmorData(chest).getWeight();
        }
        if(ItemUtil.isExist(leggings)){
            weight += ConfigManager.getNewArmorData(leggings).getWeight();
        }
        if(ItemUtil.isExist(boots)){
            weight += ConfigManager.getNewArmorData(boots).getWeight();
        }
        return weight;
    }

    public static double getDamageReduceRate(LivingEntity entity){
        double rate = 1;
        if(entity.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)){
            rate -= (entity.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE).getAmplifier() + 1) * 0.2;
        }
        if(rate < 0){
            return 0;
        }
        return rate;
    }

    public static double getDamageIncrease(LivingEntity entity){
        double increase = 1;
        if(entity.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)){
            increase += (entity.getPotionEffect(PotionEffectType.INCREASE_DAMAGE).getAmplifier() + 1) * 0.1;
        }
        if(entity.hasPotionEffect(PotionEffectType.WEAKNESS)){
            increase -= (entity.getPotionEffect(PotionEffectType.WEAKNESS).getAmplifier() + 1) * 0.15;
        }
        if(increase < 0){
            return 0;
        }
        return increase;
    }

    public static void spawnDamageMeter(Location loc, String string){
        ArmorStand armor = loc.getWorld().spawn(loc.add(Vector.getRandom().add(new Vector(-0.5, 0, -0.5))), ArmorStand.class, meter -> {
            meter.setInvulnerable(true);
            meter.setVisible(false);
            meter.setBasePlate(false);
            meter.setGravity(false);
            meter.setSmall(true);
            meter.setMarker(true);
            meter.setCustomName(string);
            meter.setCustomNameVisible(true);
            NBTTagStore.set(meter, Keys.DamageMeter.toString(), true);
        });
        Bukkit.getScheduler().runTaskLater(AdvancedCombat.getInst(), () -> {
            armor.remove();
        }, ConfigManager.damageMeterData.duration());
    }

}
