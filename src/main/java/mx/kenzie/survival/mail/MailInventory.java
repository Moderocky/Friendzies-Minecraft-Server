package mx.kenzie.survival.mail;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.bag.PickUpListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class MailInventory implements Listener {
    public final Inventory inventory;
    final HumanEntity sender;
    final MailTarget target;

    public MailInventory(HumanEntity sender, MailTarget target) {
        this.sender = sender;
        this.target = target;
        this.inventory = Bukkit.createInventory(null, 9, Component.text("Mail to Send"));
        Bukkit.getPluginManager().registerEvents(this, Survival.plugin);
    }

    @EventHandler
    public void event(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        this.close();
    }

    public void close() {
        this.unregister();
        ItemStack bag = new ItemStack(Material.RED_BUNDLE);
        BundleMeta meta = (BundleMeta) bag.getItemMeta();
        meta.displayName(Component.text("Delivery from ").append(sender.name()).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : inventory) {
            if (stack == null) continue;
            if (stack.getType() == Material.AIR) continue;
            if (stack.getAmount() == 0) continue;
            PickUpListener.mergeItem(stack, items);
        }
        if (items.isEmpty()) return;
        meta.setItems(items);
        meta.getPersistentDataContainer().set(MailManager.MAILBOX, PersistentDataType.INTEGER, 1);
        bag.setItemMeta(meta);
        Survival.mailManager.sendMail(sender, target, bag);
        this.inventory.clear();
    }

    public void unregister() {
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

}
