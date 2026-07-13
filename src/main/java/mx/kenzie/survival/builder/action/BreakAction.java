package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import mx.kenzie.survival.builder.task.InventoryBoxTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BreakAction implements BoxAction {

    @Override
    public BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box) {
        return new BreakTask(player, box, resources);
    }

    public static class BreakTask extends InventoryBoxTask {

        public Step step;

        public BreakTask(Player player, BoundingBox box, BuildingInventory inventory) {
            super(inventory, player, box);
            this.positions = this.reverse(positions); // we break from the top down
        }

        public static boolean modifyByEvent(Block thing, BlockState state, Player player, List<ItemStack> list) {
            List<Item> fakes = new ArrayList<>();
            Location location = thing.getLocation();
            for (ItemStack stack : list) {
                Item entity = thing.getWorld().createEntity(location, Item.class);
                entity.setItemStack(stack);
                fakes.add(entity);
            }
            boolean result = new BlockDropItemEvent(thing, state, player, fakes).callEvent();
            if (!result) return false;
            list.clear();
            for (Item fake : fakes) {
                list.add(fake.getItemStack());
            }
            return true;
        }

        @Override
        public Component name() {
            return Component.text("Mining");
        }

        protected int[][] reverse(int[][] a) {
            final int max = a.length;
            final int[][] b = new int[max][];
            for (int up = 0, down = max - 1; up < max; up++, down--) {
                b[down] = a[up];
            }
            return b;
        }

        @Override
        public Step peek() {
            if (cancelled || finished || count >= positions.length) return null;
            if (step != null) return step;
            final int[] pos = positions[count];
            final Location location = start.clone().add(pos[0], pos[1], pos[2]);
            return step = new Step(location.getBlock().getBlockData(), location);
        }

        @Override
        public boolean tick() {
            if (this.isFinished() || this.isCancelled()) return false;
            final Step step = this.peek();
            try {
                if (step == null) return false;
                final Block block = step.location().getBlock();
                if (block.isEmpty()) return false;
                if (!this.okay(block)) return false;
                final ItemStack tool = this.prepareToolFor(block, player);
                if (tool == null) return false;
                this.doBreak(block, tool);
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

        protected boolean okay(Block block) {
            if (block.isEmpty()) return false;
            if (block.getType() == Material.WATER) return false;
            return block.getType() != Material.LAVA;
        }

        protected boolean doBreak(Block block, ItemStack tool) {
            final List<ItemStack> result = this.destroy(block, tool);
            if (result == null) return false;
            if (!this.store(result)) this.cancel();
            return true;
        }

        protected ItemStack prepareToolFor(Block thing, Player player) {
            if (thing == null) return null;
            for (ItemStack content : contents) {
                switch (this.isCorrect(thing, content)) {
                    case 0 -> {
                        return content;
                    }
                    case 1 -> {
                        if (this.remainingDurability(content) > 1) {
                            content.damage(1, player);
                            return content;
                        }
                    }
                }
            }
            return null;
        }

        protected int remainingDurability(ItemStack stack) {
            if (stack == null) return 0;
            final int max = stack.getType().getMaxDurability();
            return max - (stack.getItemMeta() instanceof Damageable damageable ? damageable.getDamage() : 0);
        }

        protected int isCorrect(Block thing, ItemStack tool) {
            if (tool == null || thing == null) return -1;
            BlockData data = thing.getBlockData();
            if (data.getMaterial().isAir()) return -1;
            if (!thing.isValidTool(tool)) return -1;
            final float speed = data.getDestroySpeed(tool);
            if (speed < 0 || speed > 90) return -1;
            if (!data.requiresCorrectToolForDrops()) return 0;
            if (data.isPreferredTool(tool)) return 1;
            return -1;
        }

        protected @Nullable List<ItemStack> destroy(Block thing, ItemStack tool) {
            if (thing == null) return new ArrayList<>();
            org.bukkit.block.BlockState state = thing.getState();
            BlockData data = state.getBlockData();
            final List<ItemStack> list = new ArrayList<>(8);
            final boolean result;
            if (tool == null || !data.requiresCorrectToolForDrops() || data.isPreferredTool(tool)) {
                list.addAll(state.getDrops(tool, player));
                result = modifyByEvent(thing, state, player, list);
            } else result = false;
            thing.setType(Material.AIR, true);
            if (!result) return null;
            return list;
        }

//        protected @Nullable List<ItemStack> destroy1(Block thing, ItemStack tool) {
//            if (thing == null) return new ArrayList<>();
//            final CraftBlock block = (CraftBlock) thing;
//            final BlockState state = block.getNMS();
//            final net.minecraft.world.level.block.Block type = state.getBlock();
//            final net.minecraft.world.item.ItemStack item = CraftItemStack.asNMSCopy(tool);
//            final BlockPos position = block.getPosition();
//            final List<ItemStack> list = new ArrayList<>(8);
//            final boolean result;
//            final ServerLevel level = ((CraftWorld) thing.getWorld()).getHandle();
//            if (item == null || !state.requiresCorrectToolForDrops() || item.isCorrectToolForDrops(state)) {
//                for (net.minecraft.world.item.ItemStack drop
//                    : net.minecraft.world.level.block.Block.getDrops(state, level, position, level.getBlockEntity(position), null, item))
//                    list.add(CraftItemStack.asCraftMirror(drop));
//                if (state.getBlock() instanceof BaseFireBlock) level.levelEvent(1009, position, 0);
//                else level.levelEvent(2001, position, net.minecraft.world.level.block.Block.getId(state));
//                type.popExperience(level.getMinecraftWorld(), position, type.getExpDrop(state, level.getMinecraftWorld(), position, item, true));
//                result = true;
//            } else result = false;
//            final boolean destroyed = level.removeBlock(position, false);
//            if (destroyed) type.destroy(level, position, state);
//            if (result) {
//                if (type instanceof IceBlock ice) ice.afterDestroy(level.getMinecraftWorld(), position, item);
//                else if (type instanceof TurtleEggBlock eggs)
//                    eggs.decreaseEggs(level.getMinecraftWorld(), position, state);
//            }
//            if (!destroyed || !result) return null;
//            return list;
//        }

        protected boolean store(List<ItemStack> stacks) {
            if (stacks == null || stacks.isEmpty()) return true;
            return this.inventory.store(stacks);
        }

    }
}
