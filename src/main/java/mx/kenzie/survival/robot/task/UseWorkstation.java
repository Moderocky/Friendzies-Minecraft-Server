package mx.kenzie.survival.robot.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.golem.CopperGolem;

import java.util.Optional;

public abstract class UseWorkstation extends Behavior<CopperGolem> {
    private static final int CHECK_COOLDOWN = 80;
    private static final double DISTANCE = 1.8;
    private long lastCheck;

    public UseWorkstation() {
        super(ImmutableMap.of(MemoryModuleType.JOB_SITE, MemoryStatus.VALUE_PRESENT, MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED));
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, CopperGolem body) {
        if (level.getGameTime() - this.lastCheck < CHECK_COOLDOWN) {
            return false;
        } else {
            this.lastCheck = level.getGameTime();
            GlobalPos target = body.getBrain().getMemory(MemoryModuleType.JOB_SITE).get();
            return target.dimension() == level.dimension() && target.pos().closerToCenterThan(body.position(), DISTANCE);
        }
    }

    @Override
    protected void start(ServerLevel level, CopperGolem body, long timestamp) {
        Brain<CopperGolem> brain = body.getBrain();
        brain.setMemory(MemoryModuleType.LAST_WORKED_AT_POI, timestamp);
        brain.getMemory(MemoryModuleType.JOB_SITE).ifPresent((globalPos) -> brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(globalPos.pos())));
        this.useWorkstation(level, body);
    }

    protected void useWorkstation(ServerLevel level, CopperGolem body) {
    }

    @Override
    protected boolean canStillUse(ServerLevel level, CopperGolem body, long timestamp) {
        Optional<GlobalPos> jobSiteMemory = body.getBrain().getMemory(MemoryModuleType.JOB_SITE);
        if (jobSiteMemory.isEmpty()) {
            return false;
        } else {
            GlobalPos target = jobSiteMemory.get();
            return target.dimension() == level.dimension() && target.pos().closerToCenterThan(body.position(), DISTANCE);
        }
    }
}
