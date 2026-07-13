package mx.kenzie.survival.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftMonster;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Piglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class FrightenMonstersListener implements Listener {

    @EventHandler
    public void event(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof Monster monster)) return;
        if (event.getEntity() instanceof Piglin) return;
        final net.minecraft.world.entity.monster.Monster handle = ((CraftMonster) monster).getHandle();
        handle.getGoalSelector().addGoal(2, new AvoidBlockGoal(handle, 9, 1.0, 1.1));
    }

    @EventHandler(ignoreCancelled = true)
    public void event(EntityPathfindEvent event) {
        if (!(event.getEntity() instanceof Monster)) return;
        if (!this.isPositionOkay(event.getLoc())) event.setCancelled(true);
    }

    public boolean isPositionOkay(Location location) {
        return this.getNearest(location, 7) == null;
    }

    public Block getNearest(Location location, int radius) {
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    final Location point = location.clone().add(x, y, z);
                    final Block block = point.getBlock();
                    if (AvoidBlockGoal.SCARY.contains(block.getType())) return block;
                }
            }
        }
        return null;
    }

    static class AvoidBlockGoal extends Goal {
        public static final EnumSet<Material> SCARY = EnumSet.of(Material.COPPER_TORCH, Material.COPPER_WALL_TORCH, Material.COPPER_LANTERN, Material.EXPOSED_COPPER_LANTERN, Material.WEATHERED_COPPER_LANTERN, Material.OXIDIZED_COPPER_LANTERN, Material.WAXED_COPPER_LANTERN, Material.WAXED_EXPOSED_COPPER_LANTERN, Material.WAXED_WEATHERED_COPPER_LANTERN, Material.WAXED_OXIDIZED_COPPER_LANTERN, Material.COPPER_BULB, Material.EXPOSED_COPPER_BULB, Material.WEATHERED_COPPER_BULB, Material.OXIDIZED_COPPER_BULB, Material.WAXED_COPPER_BULB, Material.WAXED_EXPOSED_COPPER_BULB, Material.WAXED_WEATHERED_COPPER_BULB, Material.WAXED_OXIDIZED_COPPER_BULB);
        protected final PathfinderMob mob;
        protected final float maxDist;
        protected final PathNavigation pathNav;
        private final double walkSpeedModifier;
        private final double sprintSpeedModifier;
        @Nullable
        protected Vec3 toAvoid;
        @Nullable
        protected Path path;

        public AvoidBlockGoal(PathfinderMob mob, float distance, double slowSpeed, double fastSpeed) {
            this.mob = mob;
            this.maxDist = distance;
            this.walkSpeedModifier = slowSpeed;
            this.sprintSpeedModifier = fastSpeed;
            this.pathNav = mob.getNavigation();
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        protected Vec3 nearestSoul() {
            int radius = (int) maxDist;
            final Location location = mob.getBukkitEntity().getLocation();
            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {
                    for (int z = -radius; z < radius; z++) {
                        final Location point = location.clone().add(x, y, z);
                        final Block block = point.getBlock();
                        if (!SCARY.contains(block.getType())) continue;
                        return new Vec3(point.getX(), point.getY(), point.getZ());
                    }
                }
            }
            return null;
        }

        @Override
        public boolean canUse() {
            this.toAvoid = this.nearestSoul();
            if (this.toAvoid == null) {
                return false;
            } else {
                Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, (int) (maxDist * 2), (int) maxDist, toAvoid);
                if (vec3 == null) {
                    return false;
                } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob.position())) {
                    return false;
                } else {
                    this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                    return this.path != null;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.pathNav.isDone();
        }

        @Override
        public void start() {
            this.pathNav.moveTo(this.path, this.walkSpeedModifier);
        }

        @Override
        public void stop() {
            this.toAvoid = null;
        }

        @Override
        public void tick() {
            if (toAvoid == null) return;
            if (this.mob.distanceToSqr(this.toAvoid) < 30) {
                this.mob.getNavigation().setSpeedModifier(this.sprintSpeedModifier);
            } else {
                this.mob.getNavigation().setSpeedModifier(this.walkSpeedModifier);
            }
        }
    }

}
