package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BounceMobEffect extends SimpleMobEffect implements CustomMobEffect {
    public BounceMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectAdded(LivingEntity mob, int amplifier) {
        mob.fallDistance = 0.0F;
        mob.resetFallDistance();
        super.onEffectAdded(mob, amplifier);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        mob.resetFallDistance();
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

}
