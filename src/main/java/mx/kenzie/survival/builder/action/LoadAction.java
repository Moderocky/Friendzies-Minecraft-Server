package mx.kenzie.survival.builder.action;

import mx.kenzie.clockwork.io.DataTask;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import mx.kenzie.survival.builder.task.PaletteBuildTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BoundingBox;

import java.io.IOException;

public class LoadAction implements BoxAction {

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new LoadTask(player, box, resources);
    }

    static class LoadTask extends SetAction.SetTask {

        protected final DataTask task;
        protected final Location corner;
        protected StatePaletteBuildTask subtask;
        protected volatile Structure structure;

        public LoadTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(player, box, inventory);
            this.corner = box.getMin().toLocation(player.getWorld());
            for (ItemStack stack : inventory.getInventory()) {
                if (stack == null) continue;
                if (stack.getType() != Material.WRITTEN_BOOK) continue;
                final PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
                if (!container.has(NamespacedKey.minecraft("structure"))) continue;
                final String id = container.get(NamespacedKey.minecraft("structure"), PersistentDataType.STRING);
                if (id == null) continue;
                final NamespacedKey key = NamespacedKey.fromString(id);
                if (key == null) continue;
                this.task = Survival.IO_QUEUE.queue(new DataTask() {
                    @Override
                    public void execute() throws IOException, InterruptedException {
                        final StructureManager manager = Bukkit.getStructureManager();
                        if (manager.getStructures().containsKey(key)) structure = manager.getStructure(key);
                        else structure = Bukkit.getStructureManager().loadStructure(key);
                    }
                });
                return;
            }
            this.cancelled = true;
            this.task = null;
        }

        @Override
        public Component name() {
            return Component.text("Loading");
        }

        @Override
        public boolean tick() {
            if (this.isFinished() || this.isCancelled()) return false;
            if (subtask != null) {
                this.subtask.tick();
                return super.tick();
            }
            if (!task.isReady()) return true;
            if (structure == null) {
                this.cancelled = true;
                return false;
            }
            this.subtask = new StatePaletteBuildTask(corner, structure);
            return false;
        }

        @Override
        public boolean isFinished() {
            return finished || (subtask != null && subtask.isFinished());
        }

        @Override
        public Step peek() {
            if (cancelled || finished) return null;
            if (step != null) return step;
            return step = subtask.peek();
        }

        @Override
        protected void doPlace(Block block, BlockData data) {
            final BlockState state = subtask.state;
//            if (state instanceof CraftBlockEntityState<?> entity
//                && !(state instanceof InventoryHolder)
//                && subtask.fixState(entity, block.getLocation()))
//                state.update(true, false);
//            else
            block.setBlockData(data, false);
            if (!this.take(data.getMaterial())) this.cancel();
        }

    }

    private static class StatePaletteBuildTask extends PaletteBuildTask {

        public BlockState state;

        public StatePaletteBuildTask(Location location, Structure structure) {
            super(location, structure);
        }

        @Override
        public boolean tick() {
            if (this.isFinished() || this.cancelled) return false;
            if (next != null) {
                state = next;
                next = null;
            } else state = iterator.next();
            final BlockData data = state.getBlockData();
            final Location point = state.getLocation(start.clone());
            point.setWorld(start.getWorld());
            point.add(start);
            this.count++;
            return !point.getBlock().getBlockData().matches(data);
        }

//        @Override
//        public boolean fixState(CraftBlockState state, Location location) {
//            return super.fixState(state, location);
//        }

    }

}
