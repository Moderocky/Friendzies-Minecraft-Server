package mx.kenzie.survival.listener;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.entity.CraftAnimals;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;

public class AnimalListener implements Listener {

    @EventHandler
    public void event(EntityAddToWorldEvent event) {
        if (!(event.getEntity() instanceof Animals animals)) return;
        this.registerGoal(animals);
    }

    protected void registerGoal(Animals animals) {
        final double speed = 0.7;
        final float distance = 4;
        final Animal handle = ((CraftAnimals) animals).getHandle();
        GoalSelector selector = handle.getGoalSelector();
        switch (handle) {
            case AbstractCow _, Sheep _, Llama _, Donkey _, Horse _ ->
                    selector.addGoal(5, new EatFarmGoal(handle, Material.WHEAT, distance, speed));
            case Hoglin hoglin -> selector.addGoal(5, new EatFarmGoal(hoglin, Material.NETHER_WART, distance, speed));
            case Pig pig -> {
                selector.addGoal(5, new EatFarmGoal(pig, Material.CARROTS, distance, speed));
                selector.addGoal(5, new EatFarmGoal(pig, Material.POTATOES, distance, speed));
                selector.addGoal(5, new EatFarmGoal(pig, Material.BEETROOTS, distance, speed));
                selector.addGoal(5, new EatFarmGoal(pig, Material.SWEET_BERRIES, distance, speed));
                selector.addGoal(5, new EatFarmGoal(pig, Material.NETHER_WART, distance, speed));
            }
            case Rabbit rabbit -> selector.addGoal(5, new EatFarmGoal(rabbit, Material.CARROTS, distance, speed));
            case Parrot _, Chicken _ -> {
                selector.addGoal(5, new EatFarmGoal(handle, Material.WHEAT, distance, speed));
                selector.addGoal(5, new EatFarmGoal(handle, Material.BEETROOTS, distance, speed));
                selector.addGoal(5, new EatFarmGoal(handle, Material.COCOA, distance, speed));
            }
            case Fox fox -> selector.addGoal(5, new EatFarmGoal(fox, Material.SWEET_BERRIES, distance, speed));
            default -> {
            }
        }
    }

    static class EatFarmGoal extends Goal {
        private static final long TIME = 16000;
        protected final PathfinderMob mob;
        protected final float maxDist;
        protected final PathNavigation pathNav;
        private final double walkSpeedModifier;
        private final Material material;
        protected long nextEat;
        @Nullable
        protected Vec3 toEat;
        @Nullable
        protected Path path;

        public EatFarmGoal(PathfinderMob mob, Material material, float distance, double speed) {
            this.mob = mob;
            this.maxDist = distance;
            this.walkSpeedModifier = speed;
            this.pathNav = mob.getNavigation();
            this.material = material;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        protected Vec3 nearestFarm() {
            int radius = (int) maxDist;
            final Location location = mob.getBukkitEntity().getLocation();
            for (int x = -radius; x < radius; x++) {
                for (int y = -radius; y < radius; y++) {
                    for (int z = -radius; z < radius; z++) {
                        final Location point = location.clone().add(x, y, z).toCenterLocation();
                        final Block block = point.getBlock();
                        if (block.getType() != material) continue;
                        if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge())
                            continue;
                        return new Vec3(point.getX(), point.getY(), point.getZ());
                    }
                }
            }
            return null;
        }

        @Override
        public boolean canUse() {
            if (nextEat > System.currentTimeMillis()) return false;
            this.toEat = this.nearestFarm();
            if (this.toEat == null) return false;
            this.path = this.pathNav.createPath(toEat.x, toEat.y, toEat.z, 0);
            return this.path != null;
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
            check:
            if (toEat != null && mob.distanceToSqr(toEat) < 2) {
                final Location point = new Location(mob.getBukkitEntity().getWorld(), toEat.x, toEat.y, toEat.z);
                final Block block = point.getBlock();
                if (block.getType() != material) break check;
                if (!(block.getBlockData() instanceof Ageable ageable)) break check;
                if (ageable.getAge() < ageable.getMaximumAge())
                    break check;
                this.nextEat = System.currentTimeMillis() + TIME + (int) (ThreadLocalRandom.current().nextDouble() * TIME);
                final BlockState state = ((CraftBlock) block).getBlockState();
                final BlockPos pos = BlockPos.containing(toEat);
                final ItemStack item = new ItemStack(Blocks.AIR);
                net.minecraft.world.level.block.Block.dropResources(state, mob.level().getMinecraftWorld(), pos, mob.level().getBlockEntity(pos), null, item);
                mob.level().levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(state));
                ageable.setAge(1);
                block.setBlockData(ageable, true);
            }
            this.toEat = null;
        }

    }
}
