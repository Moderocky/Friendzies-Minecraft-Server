package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;

import java.util.Random;

public class ExperienceMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect {

    private final Random random = new Random();

    public ExperienceMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public ExperienceMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        int level = amplification + 1;
        boolean b = super.applyEffectTick(serverLevel, mob, amplification);
        for (int i = 0; i < level * 2; i++) {
            int xpCount = level + this.random.nextInt(1, 5);
            ExperienceOrb.awardWithDirection(serverLevel, mob.getEyePosition(), mob.getDeltaMovement(), xpCount, org.bukkit.entity.ExperienceOrb.SpawnReason.EXP_BOTTLE, mob, mob);
        }
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
