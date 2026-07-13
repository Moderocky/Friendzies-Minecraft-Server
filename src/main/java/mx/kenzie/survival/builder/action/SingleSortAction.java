package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SingleSortAction implements Action {

    @Override
    public void run(Player player, BuildingInventory resources, int[][] position) {
        final SingleSortTask task = new SingleSortTask(this.box(position), player);
        Survival.builder.runTask(task);
    }


    static class SingleSortTask extends SortAction.SortTask {

        protected int count;

        public SingleSortTask(BoundingBox box, Player player) {
            super(box, player);
        }

        @Override
        public boolean tick() {
            if (finished || cancelled) return false;
            if (count >= positions.length) {
                this.finished = true;
                return false;
            }
            final int[] pos = positions[count++];
            final Location location = start.clone().add(pos[0], pos[1], pos[2]);
            final Block block = location.getBlock();
            if (block.isEmpty()) return false;
            final Set<Inventory> inventories = new LinkedHashSet<>(1);
            if (block.getState() instanceof InventoryHolder container) inventories.add(container.getInventory());
            else if (block.getState() instanceof EnderChest) inventories.add(player.getEnderChest());
            final List<ItemStack> stacks = this.sort(inventories);
            if (stacks.isEmpty()) return true;
            final Location drop = start.toCenterLocation();
            for (ItemStack stack : stacks) start.getWorld().dropItem(drop, stack);
            return true;
        }

    }

}
