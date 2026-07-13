package mx.kenzie.survival.potion.effects;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Optional;
import java.util.OptionalLong;

public class TimeMobEffect extends SimpleInstantaneousMobEffect {

    protected final ResourceKey<ClockTimeMarker> timeMarkerId;

    public TimeMobEffect(MobEffectCategory category, ResourceKey<ClockTimeMarker> timeMarkerId, int r, int g, int b) {
        super(category, r, g, b);
        this.timeMarkerId = timeMarkerId;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity mob, int amplification) {
        Holder<DimensionType> dimensionType = level.dimensionTypeRegistration();
        Optional<Holder<WorldClock>> clockHolder = dimensionType.value().defaultClock();
        if (clockHolder.isEmpty()) return false;
        ServerClockManager clockManager = level.clockManager();
        Holder<WorldClock> clock = clockHolder.get();
        OptionalLong targetTime = clockManager.getTotalTicksToTimeMarker(clock, timeMarkerId);
        if (targetTime.isEmpty()) return false;
        long currentTime = clockManager.getTotalTicks(clock);
        clockManager.setTotalTicks(clock, targetTime.getAsLong() - currentTime);
        return super.applyEffectTick(level, mob, amplification);
    }

}
