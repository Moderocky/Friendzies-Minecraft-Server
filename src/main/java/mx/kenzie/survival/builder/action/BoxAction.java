package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.task.BuildTask;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

@FunctionalInterface
public interface BoxAction extends Action {

    @Override
    default void run(Player player, BuildingInventory resources, int[][] position) {
        final BuildTask task = this.makeTask(player, resources, this.box(position));
        Survival.builder.runTask(task);
    }

    BuildTask makeTask(Player player, BuildingInventory resources, BoundingBox box);

}
