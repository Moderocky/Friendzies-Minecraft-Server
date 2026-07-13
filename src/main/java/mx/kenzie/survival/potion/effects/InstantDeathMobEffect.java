package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;

public class InstantDeathMobEffect extends SimpleInstantaneousMobEffect {

    public InstantDeathMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        boolean b = super.applyEffectTick(serverLevel, mob, amplification);
        mob.getBukkitLivingEntity().kill(DamageSource.builder(DamageType.MAGIC).build());
        return b;
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
