package mx.kenzie.survival.potion.effects;

import mx.kenzie.survival.potion.entity.DefendingArrow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ArcheryMobEffect extends SimpleMobEffect implements CustomMobEffect, Listener {
    public static final int MAX_ARROWS = 3;
    protected final WeakHashMap<LivingEntity, List<DefendingArrow>> arrows = new WeakHashMap<>();
    public ArcheryMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        this.addArrow(serverLevel, mob, amplification);
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    protected void addArrow(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        List<DefendingArrow> list = arrows.getOrDefault(mob, new ArrayList<>());
        if (list.size() >= (MAX_ARROWS * amplification)) return;


        DefendingArrow arrow = new DefendingArrow(EntityTypes.ARROW, serverLevel);
        arrow.setPos(mob.getX(), mob.getY(), mob.getZ());
        if (serverLevel.addFreshEntity(arrow)) {
            arrow.tickMovement();
            list.add(arrow);
            arrows.put(mob, list);
        }

    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return tickCount % (20 * (3 - amplification)) == 0;
    }

    @Override
    public void onMobRemoved(ServerLevel level, LivingEntity mob, int amplifier, Entity.RemovalReason reason) {
        List<DefendingArrow> list = arrows.getOrDefault(mob, new ArrayList<>());
        list.forEach(arrow -> arrow.remove(Entity.RemovalReason.DISCARDED));
        list.clear();
        arrows.remove(mob);
        super.onMobRemoved(level, mob, amplifier, reason);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void entityTarget(EntityTargetLivingEntityEvent event) {
        switch (event.getReason()) {
            case TARGET_DIED, FORGOT_TARGET:
                return;
        }
        org.bukkit.entity.LivingEntity target = event.getTarget();
        org.bukkit.entity.Entity entity = event.getEntity();

        if (target == null) return;
        if (!(entity instanceof CraftLivingEntity craft)) return;
        LivingEntity handle = ((CraftLivingEntity) target).getHandle();
        LivingEntity source = craft.getHandle();
        if (handle == null) return;
        if (arrows.containsKey(handle)) {
            List<DefendingArrow> list = arrows.get(handle);
            list.forEach(arrow -> arrow.fireAt(source));
        }
    }


}
