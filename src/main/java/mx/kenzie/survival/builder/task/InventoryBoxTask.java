package mx.kenzie.survival.builder.task;

import mx.kenzie.survival.builder.BuildingInventory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class InventoryBoxTask extends InventoryBuildTask {
    public final Player player;
    public final BoundingBox box;
    public int[][] positions;
    protected ItemStack[] contents;

    public InventoryBoxTask(BuildingInventory inventory, Player player, BoundingBox box) {
        super(player.getWorld(), new Location(player.getWorld(), box.getMinX(), box.getMinY(), box.getMinZ()), inventory);
        this.player = player;
        this.box = box;
        this.positions = this.assemblePositions();
        this.updateContents();
    }

    protected int[][] assemblePositions() {
        int index = 0;
        final int height = (int) box.getHeight() + 1, width = (int) box.getWidthX() + 1, length = (int) box.getWidthZ() + 1;
        final int[][] positions = new int[total = (height * width * length)][3];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                for (int z = 0; z < length; z++) positions[index++] = new int[]{x, y, z};
        return positions;
    }

    protected void updateContents() {
        final List<ItemStack> list = new ArrayList<>(18);
        for (ItemStack stack : inventory.getInventory()) {
            if (stack == null) continue;
            if (stack.getType() == Material.AIR) continue;
            if (stack.getAmount() < 1) continue;
            list.add(stack);
        }
        this.contents = list.toArray(new ItemStack[0]);
    }

    @Override
    public boolean isFinished() {
        return finished || count >= positions.length;
    }

    @Override
    public @Nullable Player owner() {
        return player;
    }

}
