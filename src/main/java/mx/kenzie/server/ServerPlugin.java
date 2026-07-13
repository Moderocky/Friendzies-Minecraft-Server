package mx.kenzie.server;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerPlugin extends JavaPlugin {

    public static ItemStack CONTROLLER;
    static NamespacedKey controller = NamespacedKey.minecraft("controller");

    public static boolean isController(ItemStack itemStack) {
        if (itemStack == CONTROLLER)
            return true;
        ItemMeta meta = itemStack.getItemMeta();
        return meta.getPersistentDataContainer().has(controller, PersistentDataType.BOOLEAN)
                && Boolean.TRUE.equals(meta.getPersistentDataContainer().get(controller, PersistentDataType.BOOLEAN));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        ItemStack item = ItemStack.of(Material.DEBUG_STICK, 1);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Controller"));
        meta.getPersistentDataContainer().set(controller, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        CONTROLLER = item;
    }


    @Override
    public void onDisable() {
        super.onDisable();
    }
}
