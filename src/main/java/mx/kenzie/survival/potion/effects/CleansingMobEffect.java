package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectTypeCategory;

public class CleansingMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect {

    public CleansingMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public CleansingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        CraftLivingEntity living = mob.getBukkitLivingEntity();
        for (PotionEffect effect : living.getActivePotionEffects()) {
            if (effect.getType().getCategory() == PotionEffectTypeCategory.HARMFUL)
                living.removePotionEffect(effect.getType());
        }
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

}
