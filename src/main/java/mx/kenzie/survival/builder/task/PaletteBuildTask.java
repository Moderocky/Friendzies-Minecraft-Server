package mx.kenzie.survival.builder.task;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class PaletteBuildTask implements BuildTask {
    @SuppressWarnings("all")
    public static final PaletteBuildTask EMPTY = new PaletteBuildTask() {
        @Override
        public boolean tick() {
            return true;
        }

        @Override
        public boolean isFinished() {
            return true;
        }

        @Override
        public void finish() {
        }

        @Override
        public PaletteBuildTask clone() {
            return this;
        }
    };

    protected final Palette palette;
    protected final Iterator<BlockState> iterator;
    protected final Location start;
    protected final int id = BuildManager.createTaskId();
    protected boolean finished, cancelled;
    protected int total, count;
    protected BlockState next;

    protected PaletteBuildTask() {
        this.palette = null;
        this.iterator = Collections.emptyIterator();
        this.start = null;
    }

    public PaletteBuildTask(Location location, Palette palette) {
        this.palette = palette;
        this.iterator = palette.getBlocks().iterator();
        this.start = location;
        this.total = palette.getBlockCount();
        Bukkit.getScheduler().runTaskAsynchronously(Survival.plugin, this::fixBlocks);
    }

    public PaletteBuildTask(Location location, Structure structure) {
        final List<Palette> palettes = structure.getPalettes();
        this.palette = palettes.get(ThreadLocalRandom.current().nextInt(palettes.size()));
        this.iterator = palette.getBlocks().iterator();
        this.start = location;
        this.total = palette.getBlockCount();
        Bukkit.getScheduler().runTaskAsynchronously(Survival.plugin, this::fixBlocks);
    }

    @Override
    public int taskId() {
        return id;
    }

    protected void fixBlocks() { // we do the reflection off main, the `fix` method will then use the simple instanceof.
        for (BlockState state : palette.getBlocks()) {
            final Location point = state.getLocation(start.clone());
            point.setWorld(start.getWorld());
            point.add(start);
        }
    }

    @Override
    public boolean tick() {
        return this.tick(null);
    }

    @Override
    public boolean tick(Consumer<Step> consumer) {
        if (this.isFinished() || this.cancelled) return false;
        final BlockState state;
        if (next != null) {
            state = next;
            next = null;
        } else state = iterator.next();
        final BlockData data = state.getBlockData();
        final Location point = state.getLocation(start.clone());
        point.setWorld(start.getWorld());
        point.add(start);
        if (consumer != null) {
            final Step step = new Step(data, start);
            consumer.accept(step);
        }
        if (point.getBlock().getBlockData().matches(data)) return false;
//        if (state instanceof CraftBlockEntityState<?> entity && this.fixState(entity, point)) state.update(true, false);
        point.getBlock().setBlockData(data, false);
        this.count++;
        return true;
    }

    public void doPlace() {

    }

//    protected boolean fixState(CraftBlockState state, Location location) {
//        if (position == null) return false;
//        state.setWorldHandle(((CraftWorld) location.getWorld()).getHandle());
//        if (state.getPosition() instanceof BlockPos.MutableBlockPos pos) {
//            pos.set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
//        } else {
//            final BlockPos pos = new BlockPos.MutableBlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
//            try {
//                position.set(state, pos);
//            } catch (IllegalAccessException e) {
//                return false;
//            }
//        }
//        return true;
//    }

    @Override
    public Step peek() {
        if (this.isFinished() || this.cancelled) return null;
        if (next == null) next = iterator.next();
        final BlockData data = next.getBlockData();
        final Location point = next.getLocation(start.clone());
        point.setWorld(start.getWorld());
        point.add(start);
        return new Step(data, point);
    }

    @Override
    public void finish() {
        if (cancelled) return;
        while (!this.isFinished()) this.tick();
        this.count = total;
        this.finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished || (!iterator.hasNext() && next == null);
    }

    @Override
    public int getTotalStages() {
        return total;
    }

    @Override
    public int getStage() {
        return count;
    }

    @Override
    public int remaining() {
        return total - count;
    }

    @Override
    public void cancel() {
        this.cancelled = true;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public @Nullable Player owner() {
        return null;
    }

    @Override
    public PaletteBuildTask clone() {
        return new PaletteBuildTask(start, palette);
    }

    @Override
    public String toString() {
        if (palette == null) return "BuildTask[Empty]";
        return "BuildTask[" + palette.getBlockCount() + "]";
    }

}
