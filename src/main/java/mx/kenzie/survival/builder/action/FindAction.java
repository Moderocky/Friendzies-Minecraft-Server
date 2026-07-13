package mx.kenzie.survival.builder.action;

import mx.kenzie.clockwork.collection.ClockList;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

public class FindAction implements Action {

    protected final Material material;

    public FindAction(Material material) {
        this.material = material;
    }

    @Override
    public void run(Player player, BuildingInventory resources, int[][] position) {
        final FindTask task = new FindTask(this.box(position), player);
        Survival.builder.runTask(task);
    }

    public class FindTask extends SortAction.SortTask {

        protected int count = 0;

        public FindTask(BoundingBox box, Player player) {
            super(box, player);
        }

        @Override
        protected int[][] getPositions(int height, int width, int length) {
            final ClockList<int[]> list = new ClockList<>();
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    for (int z = 0; z < length; z++) {
                        final Location location = start.clone().add(x, y, z);
                        final Block block = location.getBlock();
                        if (block.isEmpty()) continue;
                        if (block.getState() instanceof InventoryHolder) list.add(new int[]{x, y, z});
                        else if (block.getState() instanceof EnderChest) list.add(new int[]{x, y, z});
                    }
            return list.toArray();
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
            final Inventory inventory;
            if (block.getState() instanceof InventoryHolder holder) inventory = holder.getInventory();
            else if (block.getState() instanceof EnderChest) inventory = player.getEnderChest();
            else return false;
            for (ItemStack stack : inventory) {
                if (stack == null) continue;
                if (stack.getType() != material) continue;
                this.alert(block);
                return true;
            }
            return false;
        }

        public void alert(Block block) {
            final Location centre = block.getLocation().toCenterLocation();
            block.getWorld().playSound(centre.toCenterLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 5.0F, 1.0F);
            for (BlockFace value : BlockFace.values()) {
                block.getWorld().spawnParticle(Particle.END_ROD, centre.clone().add(value.getDirection()), 4, 0.2, 0.2, 0.2, 0);
            }
        }

        @Override
        public Component name() {
            return Component.text("Finding Task");
        }

    }

}
