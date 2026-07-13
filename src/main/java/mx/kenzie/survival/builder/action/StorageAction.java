package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.builder.command.GetCommand;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;

public class StorageAction implements Action {

    @Override
    public void run(Player player, BuildingInventory resources, int[][] positions) {
        final BoundingBox box = new BoundingBox(
                positions[0][0], positions[0][1], positions[0][2],
                positions[1][0], positions[1][1], positions[1][2]
        );
        final World world = player.getWorld();
        final GetCommand.StorageArea area = new GetCommand.StorageArea(world, box);
        GetCommand.setStorageArea(player, area);
        if (GetCommand.getStorageArea(player) != null) player.sendActionBar(Component.text("Marked storage area."));
        else player.sendActionBar(Component.text("Failed to mark storage area."));
    }

}
