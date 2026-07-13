package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.CancellableTask;
import mx.kenzie.survival.builder.task.Task;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SortAction implements Action {

    @Override
    public void run(Player player, BuildingInventory resources, int[][] position) {
        final SortTask task = new SortTask(this.box(position), player);
        Survival.builder.runTask(task);
    }


    static class SortTask extends CancellableTask implements Task {

        protected final BoundingBox box;
        protected final Player player;
        protected final Location start;
        protected int[][] positions;
        protected int total;

        public SortTask(BoundingBox box, Player player) {
            this.player = player;
            this.box = box;
            this.start = new Location(player.getWorld(), box.getMinX(), box.getMinY(), box.getMinZ());
            final int height = (int) box.getHeight() + 1, width = (int) box.getWidthX() + 1, length = (int) box.getWidthZ() + 1;
            this.positions = this.getPositions(height, width, length);
        }

        protected int[][] getPositions(int height, int width, int length) {
            int index = 0;
            this.positions = new int[total = (height * width * length)][3];
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    for (int z = 0; z < length; z++) this.positions[index++] = new int[]{x, y, z};
            return positions;
        }

        protected List<ItemStack> sort(Set<Inventory> inventories) {
            final LinkedList<ItemStack> stacks = new LinkedList<>();
            for (Inventory inventory : inventories) {
                try {
                    for (ItemStack stack : inventory) {
                        if (stack == null) continue;
                        if (stack.getType() == Material.AIR) continue;
                        if (stack.getAmount() < 1) continue;
                        final ItemStack copy = stack.clone();
                        stack.setAmount(0);
                        for (ItemStack item : stacks) this.squash(copy, item);
                        if (copy.getAmount() > 0) stacks.add(copy);
                    }
                    inventory.clear();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    this.cancel();
                }
            }
            stacks
                    .sort(Comparator.<ItemStack, CreativeCategory>comparing(stack -> stack.getType().getCreativeCategory())
                            .thenComparing(stack -> stack.getType().isBlock()).thenComparing(ItemStack::getType).thenComparing(ItemStack::getAmount));
            for (Inventory inventory : inventories)
                while (!stacks.isEmpty()) {
                    final Map<Integer, ItemStack> map = inventory.addItem(stacks.poll());
                    if (map.isEmpty()) continue;
                    for (ItemStack value : map.values()) stacks.addFirst(value);
                    break;
                }
            return stacks;
        }

        @Override
        public boolean tick() {
            if (finished || cancelled) return false;
            final Set<Inventory> inventories = new LinkedHashSet<>(16);
            for (int[] pos : positions) {
                final Location location = start.clone().add(pos[0], pos[1], pos[2]);
                final Block block = location.getBlock();
                if (block.isEmpty()) continue;
                if (block.getState() instanceof InventoryHolder container) inventories.add(container.getInventory());
                else if (block.getState() instanceof EnderChest) inventories.add(player.getEnderChest());
            }
            final List<ItemStack> stacks = this.sort(inventories);
            this.finished = true;
            if (stacks.isEmpty()) return true;
            final Location drop = start.toCenterLocation();
            for (ItemStack stack : stacks) start.getWorld().dropItem(drop, stack);
            return true;
        }

        protected void squash(ItemStack from, ItemStack to) {
            if (from == null || to == null) return;
            final Material first = from.getType(), second = to.getType();
            if (first != second) return;
            final int max = first.getMaxStackSize();
            if (first.getMaxStackSize() < 2) return;
            if (to.getAmount() >= max) return;
            final int count = from.getAmount(), space = max - to.getAmount(), take = Math.min(count, space);
            from.setAmount(take);
            to.setAmount(to.getAmount() + take);
        }

        @Override
        public void finish() {
            while (!this.isCancelled() && !this.isFinished()) this.tick();
            this.finished = true;
        }

        @Override
        public int getTotalStages() {
            return total;
        }

        @Override
        public int getStage() {
            return finished ? total : 0;
        }

        @Override
        public int remaining() {
            return finished ? 0 : total;
        }

        @Override
        public @Nullable Player owner() {
            return player;
        }

        @Override
        public Component name() {
            return Component.text("Sorting");
        }

    }

}
