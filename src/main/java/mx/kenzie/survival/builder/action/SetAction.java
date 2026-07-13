package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import mx.kenzie.survival.utility.DefaultMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class SetAction extends BreakAction {

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new SetTask(player, box, resources);
    }

    static class SetTask extends BreakTask {

        final Random random = ThreadLocalRandom.current();
        public Step step;
        protected Material[] materials;
        protected Map<Material, Float> chances;


        public SetTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(player, box, inventory);
            this.updateContents();
        }

        @Override
        public Component name() {
            return Component.text("Setting Area");
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

        protected boolean okay(Block block) {
            return true;
        }

        @Override
        public boolean tick() {
            if (this.isFinished() || this.isCancelled()) return false;
            final Step step = this.peek();
            try {
                if (step == null) return false;
                final Block block = step.location().getBlock();
                if (!this.okay(block)) return false;
                if (block.getBlockData().equals(step.data())) return false;
                if (!this.canTake(step.data().getMaterial())) {
                    this.cancel();
                    return false;
                }
                if (!block.isEmpty()) {
                    final ItemStack tool = this.prepareToolFor(block, player);
                    if (tool == null) return false;
                    BlockData blockData = this.prepareData(step, block);
                    if (this.doBreak(block, tool)) this.doPlace(block, blockData);
                } else this.doPlace(block, step.data());
            } catch (Throwable ex) {
                this.cancel();
                ex.printStackTrace();
                return false;
            } finally {
                this.count++;
                this.step = null;
            }
            return true;
        }

        protected BlockData prepareData(Step step, Block existing) {
            return step.data();
        }

        protected void doPlace(Block block, BlockData data) {
            block.setBlockData(data);
            if (!this.take(data.getMaterial())) this.cancel();
        }

        public Material getMaterial() {
            if (materials == null || materials.length < 1) return Material.AIR;
            int attempts = 0;
            while (++attempts < 32) {
                float f = random.nextFloat();
                float chance = 0;
                for (Map.Entry<Material, Float> entry : chances.entrySet()) {
                    Material material = entry.getKey();
                    chance += entry.getValue();
                    if (f < chance && inventory.getInventory().contains(material)) return material;
                }
            }
            this.updateContents();
            while (++attempts < 64) {
                final Material material = materials[random.nextInt(materials.length)];
                if (inventory.getInventory().contains(material)) return material;
            }
            return Material.AIR;
        }

        @Override
        protected void updateContents() {
            super.updateContents();
            final Set<Material> materials = new HashSet<>(18);
            int total = 0;
            DefaultMap<Material, Integer> counts = new DefaultMap<>(0);
            for (ItemStack stack : inventory.getInventory()) {
                if (stack == null) continue;
                if (stack.getAmount() < 1) continue;
                final Material material = stack.getType();
                if (material == Material.AIR) continue;
                if (!material.isBlock()) continue;
                materials.add(material);
                total += stack.getAmount();
                counts.put(material, counts.get(material) + stack.getAmount());
            }
            this.chances = new DefaultMap<>(0.0F);
            float finalTotal = total;
            counts.forEach((m, c) -> chances.put(m, c / finalTotal));
            this.materials = materials.toArray(new Material[0]);
        }

        protected boolean canTake(Material material) {
            return (inventory.inventory.contains(material));
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
