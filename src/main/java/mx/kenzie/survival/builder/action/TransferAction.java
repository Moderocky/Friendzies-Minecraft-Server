package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.CancellableTask;
import mx.kenzie.survival.builder.task.Task;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TransferAction implements Action {

    @Override
    public void run(Player player, BuildingInventory resources, int[][] position) {
        final Block from = player.getWorld().getBlockAt(position[0][0], position[0][1], position[0][2]);
        final Block to = player.getWorld().getBlockAt(position[1][0], position[1][1], position[1][2]);
        final TransferTask task = new TransferTask(from, to, player);
        Survival.builder.runTask(task);
    }

    static class TransferTask extends CancellableTask implements Task {

        protected final Player player;
        protected Block from, to;
        protected Inventory first, second;
        protected int total, count;

        TransferTask(Block from, Block to, Player player) {
            this.player = player;
            this.from = from;
            this.first = this.getInventory(from.getState());
            this.to = to;
            this.second = this.getInventory(to.getState());
            if (first.equals(second)) this.cancel();
            this.total = this.sum(first);
        }

        @Override
        public Component name() {
            return Component.text("Transferring");
        }

        private int sum(Inventory inventory) {
            int count = 0;
            for (ItemStack stack : inventory) {
                if (stack == null) continue;
                count += stack.getAmount();
            }
            return count;
        }

        protected Inventory getInventory(BlockState state) {
            if (state instanceof InventoryHolder container) return container.getInventory();
            else if (state instanceof EnderChest) return player.getEnderChest();
            else return player.getInventory();
        }

        @Override
        public boolean tick() {
            if (finished || cancelled) return false;
            if (!this.getInventory(from.getState()).equals(first)) {
                this.cancel();
                return false;
            }
            if (!this.getInventory(to.getState()).equals(second)) {
                this.cancel();
                return false;
            }
            final ItemStack item = this.take(first);
            if (item == null) {
                this.finished = true;
                return false;
            }
            this.count++;
            for (ItemStack value : second.addItem(item).values()) {
                for (ItemStack stack : first.addItem(value).values())
                    to.getWorld().dropItem(to.getLocation().toCenterLocation(), stack);
                this.cancelled = true;
            }
            return true;
        }

        protected ItemStack take(Inventory inventory) {
            for (ItemStack stack : inventory) {
                if (stack == null) continue;
                if (stack.getType() == Material.AIR) continue;
                if (stack.getAmount() < 1) continue;
                final ItemStack item = stack.clone();
                item.setAmount(1);
                stack.setAmount(stack.getAmount() - 1);
                return item;
            }
            return null;
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
            return count;
        }

        @Override
        public int remaining() {
            return total - count;
        }

        @Override
        public @Nullable Player owner() {
            return player;
        }

    }

}
