package mx.kenzie.survival.builder.task;

import mx.kenzie.survival.builder.BuildManager;
import mx.kenzie.survival.builder.BuildingInventory;
import org.bukkit.Location;
import org.bukkit.World;

public abstract class InventoryBuildTask extends CancellableTask implements BuildTask {

    protected final World world;
    protected final Location start;
    protected final BuildingInventory inventory;
    protected final int id = BuildManager.createTaskId();
    protected int total;
    protected int count = 0;

    public InventoryBuildTask(World world, Location location, BuildingInventory inventory) {
        this.world = world;
        this.start = location;
        this.inventory = inventory;
        if (inventory.getInventory().isEmpty()) this.cancel();
    }

    @Override
    public void finish() {
        if (cancelled) return;
        while (!this.isFinished()) this.tick();
        this.count = total;
        this.finished = true;
    }

    @Override
    public abstract boolean isFinished();

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
    public void onEnd() {
        this.inventory.close();
    }

    @Override
    public int taskId() {
        return id;
    }
}
