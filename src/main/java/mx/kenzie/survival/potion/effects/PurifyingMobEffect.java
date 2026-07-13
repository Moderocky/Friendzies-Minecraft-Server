package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class PurifyingMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect {

    public PurifyingMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public PurifyingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        if (mob instanceof ZombieVillager villager) {
            villager.startConverting(null, 10, true);
            return super.applyEffectTick(serverLevel, mob, amplification);
        }
        if (mob instanceof Piglin piglin) {
            piglin.setImmuneToZombification(true);
        }
        if (mob instanceof ZombifiedPiglin zombie) {
            Piglin piglin = zombie.convertTo(EntityTypes.PIGLIN, ConversionParams.single(zombie, true, true), (ConversionParams.AfterConversion<Piglin>) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.CURED, CreatureSpawnEvent.SpawnReason.CURED);
            if (piglin != null)
                piglin.setImmuneToZombification(true);
        }
        CraftLivingEntity living = mob.getBukkitLivingEntity();
        for (ItemStack stack : this.getEquipment(living)) {
            if (stack == null) continue;
            ItemMeta meta = stack.getItemMeta();
            if (meta == null) continue;
            if (!meta.hasEnchants()) continue;
            for (Enchantment enchantment : meta.getEnchants().keySet()) {
                if (enchantment.isCursed()) meta.removeEnchant(enchantment);
            }
        }
        for (PotionEffect effect : living.getActivePotionEffects()) {
            living.removePotionEffect(effect.getType());
        }
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    private Iterable<ItemStack> getEquipment(CraftLivingEntity entity) {
        EntityEquipment equipment = entity.getEquipment();
        List<ItemStack> items = new ArrayList<>();
        items.add(equipment.getItemInOffHand());
        items.add(equipment.getItemInMainHand());
        for (ItemStack armorContent : equipment.getArmorContents()) {
            if (armorContent == null) continue;
            items.add(armorContent);
        }
        return items;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return super.shouldApplyEffectTickThisTick(tickCount, amplification);
    }

    @Override
    public void onEffectAdded(LivingEntity mob, int amplifier) {
        super.onEffectAdded(mob, amplifier);
    }
}
