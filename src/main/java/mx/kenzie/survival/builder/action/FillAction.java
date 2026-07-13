package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import mx.kenzie.survival.builder.task.InventoryBoxTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FillAction implements BoxAction {

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new FillTask(player, box, resources);
    }

    static class FillTask extends InventoryBoxTask {

        public Step step;

        public FillTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(inventory, player, box);
            for (ItemStack stack : inventory.inventory.getContents()) {
                if (stack == null) continue;
                if (!stack.getType().isBlock()) {
                    inventory.giveBack(player, stack);
                    inventory.inventory.remove(stack);
                }
            }
            this.updateContents();
        }

        @Override
        public Component name() {
            return Component.text("Filling");
        }

        @Override
        public Step peek() {
            if (cancelled || finished || count >= positions.length) return null;
            if (step != null) return step;
            final int[] pos = positions[count];
            final Location location = start.clone().add(pos[0], pos[1], pos[2]);
            final Material material = this.getMaterial();
            if (material == null) return null;
            return step = new Step(material.createBlockData(), location);
        }

        public Material getMaterial() {
            final Random random = ThreadLocalRandom.current();
            int attempts = 0;
            while (++attempts < 32) {
                final Material material = contents[random.nextInt(contents.length)].getType();
                if (inventory.getInventory().contains(material)) return material;
            }
            this.updateContents();
            while (++attempts < 64) {
                final Material material = contents[random.nextInt(contents.length)].getType();
                if (inventory.getInventory().contains(material)) return material;
            }
            return Material.AIR;
        }

        @Override
        public boolean tick() {
            if (this.isFinished() || this.isCancelled()) return false;
            final Step step = this.peek();
            try {
                if (step == null) return false;
                final Block block = step.location().getBlock();
                if (!this.okay(block)) return false;
                block.setBlockData(step.data());
            } catch (Throwable ex) {
                this.cancel();
                ex.printStackTrace();
                return false;
            } finally {
                this.count++;
                this.step = null;
            }
            if (!this.take(step.data().getMaterial())) this.cancel();
            if (inventory.getInventory().isEmpty()) this.cancel();
            return true;
        }

        protected boolean okay(Block block) {
            if (block.isEmpty()) return true;
            return block.getType() == Material.WATER || block.getType() == Material.LAVA;
        }

        protected boolean take(Material material) {
            for (ItemStack stack : inventory.inventory) {
                if (stack == null) continue;
                if (stack.getType() != material) continue;
                if (stack.getAmount() < 1) continue;
                stack.setAmount(stack.getAmount() - 1);
                return true;
            }
            return false;
        }

    }

}
