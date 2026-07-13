package mx.kenzie.survival.mail;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.utility.ItemFetcher;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class MailMenu implements Listener, Closeable {

    private final Inventory inventory;
    private transient final Player player;
    MailTarget[] targets;

    public MailMenu(Player player) {
        this.player = player;
        this.inventory = Bukkit.createInventory(player, InventoryType.SHULKER_BOX, Component.text("Select Recipient"));
    }

    public Inventory createFor(OfflinePlayer[] playerList) {
        List<MailTarget> targets = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta itemMeta = (SkullMeta) head.getItemMeta();
            itemMeta.setPlayerProfile(player.getPlayerProfile());
            itemMeta.displayName(player.displayName().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            head.setItemMeta(itemMeta);
            targets.add(new MailTarget(player.getLocation(), player, false));
            inventory.addItem(head);
        }
        for (OfflinePlayer player : playerList) {
            Location location = MailManager.getMailboxLocation(player);
            if (location == null) continue;
            ItemStack chest = new ItemStack(Material.CHEST);
            String name = player.getName();
            if (name == null) continue;
            ItemMeta itemMeta = chest.getItemMeta();
            itemMeta.displayName((Component.text(name).append(Component.text("'s Mailbox"))).color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
            chest.setItemMeta(itemMeta);
            targets.add(new MailTarget(location, player, true));
            inventory.addItem(chest);
        }
        this.targets = targets.toArray(new MailTarget[0]);
        Bukkit.getPluginManager().registerEvents(this, Survival.plugin);
        player.openInventory(this.inventory);
        return inventory;
    }

    public void unregister() {
        InventoryDragEvent.getHandlerList().unregister(this);
        InventoryClickEvent.getHandlerList().unregister(this);
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void event(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        if (!inventory.equals(event.getClickedInventory())) return;
        event.setCancelled(true);
        final ItemStack icon = event.getCurrentItem();
        if (icon == null) return;
        final int slot = event.getSlot();
        if (slot >= targets.length || slot < 0) return;
        HumanEntity human = event.getWhoClicked();
        human.closeInventory();
        MailTarget target = targets[slot];
        MailInventory mailInventory = new MailInventory(human, target);
        human.openInventory(mailInventory.inventory);
    }

    @EventHandler
    public void event(InventoryDragEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void event(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        this.close();
    }

    public void close() {
        this.unregister();
        this.inventory.clear();
    }
}

record MailTarget(Location location, OfflinePlayer player, boolean box) {

    public <Q extends InventoryHolder & BlockState> ItemFetcher.Target toTarget() {
        if (box)
            return ItemFetcher.Target.block(((Q) location.getBlock().getState()));
        else return new ItemFetcher.Target(player.getPlayer()::getInventory, player.getPlayer()::getEyeLocation);
    }
}
