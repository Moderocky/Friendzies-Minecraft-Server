package mx.kenzie.survival.robot;

import mx.kenzie.centurion.MinecraftCommand;
import org.bukkit.Location;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;

import static mx.kenzie.centurion.CommandResult.PASSED;
import static org.bukkit.inventory.EquipmentSlot.SADDLE;

public class TestingCommand extends MinecraftCommand {

    public TestingCommand() {
        super("testing");
    }

    @Override
    public MinecraftBehaviour create() {
        return command("testing").lapse(sender -> {
            Player player = (Player) sender;
            Location location = player.getLocation();
            location.getWorld().spawn(location, CopperGolem.class, golem -> {
                EntityEquipment equipment = golem.getEquipment();
                equipment.setItemInMainHand(player.getInventory().getItemInMainHand());
                equipment.setItemInOffHand(player.getInventory().getItemInOffHand());
                equipment.setHelmet(player.getInventory().getHelmet());
                equipment.setChestplate(player.getInventory().getChestplate());
                equipment.setLeggings(player.getInventory().getLeggings());
                equipment.setBoots(player.getInventory().getBoots());
                equipment.setItem(SADDLE, player.getInventory().getItemInOffHand());
            });
            return PASSED;
        });
    }
}
