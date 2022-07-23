package com.hirises.combat.damage.data;

import com.hirises.combat.AdvancedCombat;
import com.hirises.combat.config.ConfigManager;
import com.hirises.combat.config.Keys;
import com.hirises.combat.damage.manager.CombatManager;
import com.hirises.core.data.TimeUnit;
import com.hirises.core.data.unit.DataUnit;
import com.hirises.core.store.NBTTagStore;
import com.hirises.core.store.YamlStore;
import com.hirises.core.task.CancelableTask;
import com.hirises.core.util.Util;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.List;

//음식 데이터
@Immutable
@ThreadSafe
public class FoodData implements DataUnit {
    private double instantHeal;     //즉시 회복
    private double instantHunger;   //허기 회복량
    private double healPerSecond;   //초당 회복량
    private int healDuration;       //초당 회복 지속 시간 (틱)
    private int coolDown;           //쿨다운 (틱)
    private int maxConsumeAmount;   //1회 섭취량
    private List<Material> coolWith;    //쿨다운 공유 아이템

    public FoodData(){
        this.instantHeal = 0;
        this.healPerSecond = 0;
        this.healDuration = 0;
        this.coolDown = 0;
        this.maxConsumeAmount = 1;
        this.coolWith = new ArrayList<>();
    }

    //해당 음식의 효과를 적용
    public void eat(LivingEntity entity, int amount){
        if(instantHeal > 0){
            //즉시 회복
            CombatManager.heal(entity, instantHeal * amount);
        }
        if(entity instanceof Player){
            Player player = (Player) entity;
            if(instantHunger > 0){
                //허기 회복
                if(player.getFoodLevel() + (instantHunger * amount) > 19){
                    player.setFoodLevel(19);
                }else{
                    player.setFoodLevel(player.getFoodLevel() + (int)Math.floor(instantHunger * amount));
                }
            }
            if(player.getGameMode() != GameMode.CREATIVE){
                //쿨다운 시작
                startCoolDown(player);
            }
        }
        if(healPerSecond > 0){
            //초당 힐 적용
            double heal = (healPerSecond * amount) / (20.0 / ConfigManager.foodDelay);
            CombatManager.startHealGradually(entity, heal);
            new CancelableTask(AdvancedCombat.getInst(), healDuration + 1){
                @Override
                public void run() {
                    CombatManager.endHealGradually(entity, heal);
                }
            };
        }
    }

    //쿨다운 시작
    public void startCoolDown(Player player){
        if(coolDown > 0){
            for(Material mat : coolWith){
                NBTTagStore.set(player, Keys.MaterialCoolDown + mat.toString(), Util.getCurrentTick() + coolDown);
                player.setCooldown(mat, coolDown);
            }
        }
    }

    public double getInstantHunger() {
        return instantHunger;
    }

    public double getInstantHeal() {
        return instantHeal;
    }

    public double getHealPerSecond() {
        return healPerSecond;
    }

    public int getCoolDown() {
        return coolDown;
    }

    public int getHealDuration() {
        return healDuration;
    }

    public List<Material> getCoolWith() {
        return coolWith;
    }

    public int getMaxConsumeAmount() {
        return maxConsumeAmount;
    }

    @Override
    public void load(YamlStore yml, String root) {
        this.instantHunger = yml.getToNumber(root + ".허기");
        this.instantHeal = yml.getToNumber(root + ".회복");
        this.healPerSecond = yml.getToNumber(root + ".초당회복");
        this.maxConsumeAmount = yml.getOrDefault(Integer.class, 1, root + ".최대섭취개수");
        this.healDuration = (int)yml.getOrDefault(new TimeUnit(), root + ".회복시간").getToTick();
        this.coolDown = (int)yml.getOrDefault(new TimeUnit(), root + ".쿨타임").getToTick();
        this.coolWith = new ArrayList<>();
        String matStr = root.substring(root.lastIndexOf(".") + 1);
        coolWith.add(Material.valueOf(matStr));
        if(yml.containKey(root + ".쿨공유")){
            for(String mat : yml.getConfig().getStringList(root + ".쿨공유")){
                coolWith.add(Material.valueOf(mat));
            }
        }
    }

    @Override
    public void save(YamlStore yamlStore, String s) {
        //No Use
    }
}
