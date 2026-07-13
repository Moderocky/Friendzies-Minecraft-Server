package mx.kenzie.survival.mail;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class MailListener implements Listener {
    final MailManager mailManager;

    public MailListener(MailManager mailManager) {
        this.mailManager = mailManager;
    }

    @EventHandler
    public void event(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getEquipment().getItem(event.getHand());
        if (mailManager.isMailBag(item) || mailManager.isMailTag(item))
            event.setCancelled(true);
    }

    @EventHandler
    public void event(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && block.getState() instanceof Container container) {
            if (!mailManager.isMailTag(event.getItem())) return;
            MailManager.setMailboxLocation(player, block.getLocation());
            event.setCancelled(true);
            Location interactionPoint = event.getInteractionPoint();
            if (interactionPoint == null) interactionPoint = block.getLocation().add(0.5, 1, 0.5);
            block.getWorld().spawnParticle(Particle.FIREWORK, interactionPoint, 7, 0.5, 0.5, 0.5, 0);
            container.getPersistentDataContainer().set(MailManager.MAILBOX, PersistentDataType.STRING, player.getUniqueId().toString());
            //<editor-fold desc="Message" defaultstate="collapsed">
            player.sendMessage(Component.textOfChildren(
                    text("Your mailbox has been set to this ", NamedTextColor.DARK_GREEN),
                    translatable(block.getType(), NamedTextColor.GREEN),
                    text(".", NamedTextColor.DARK_GREEN)
            ));
            //</editor-fold>
        } else {
            ItemStack item = event.getItem();
            if (!mailManager.isMailTag(item)) return;
            event.setCancelled(true);
            mailManager.selectMail(player);
        }
    }

    @EventHandler
    public void event1(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (item == null) return;
        if (!mailManager.isMailBag(item)) return;
        event.setCancelled(true);
        BundleMeta meta = (BundleMeta) item.getItemMeta();
        if (meta.hasItems()) {
            List<ItemStack> items = meta.getItems();
            for (ItemStack itemStack : items) {
                Item dropped = player.getWorld().dropItem(player.getLocation(), itemStack);
                dropped.setPickupDelay(0);
                dropped.setOwner(player.getUniqueId());
            }
        }
        item.setAmount(0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void event(BlockBreakEvent event) {
        if (event.getBlock().getState() instanceof Container container) {
            PersistentDataContainer dataContainer = container.getPersistentDataContainer();
            if (!dataContainer.has(MailManager.MAILBOX, PersistentDataType.STRING)) return;
            String string = dataContainer.get(MailManager.MAILBOX, PersistentDataType.STRING);
            assert string != null;
            UUID uuid = UUID.fromString(string);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            MailManager.setMailboxLocation(player, null);
        }
    }

    @EventHandler
    public void event(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MailManager.checkMailboxLocation(player);
    }

    @EventHandler
    public void event(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        if (event.isCancelled()) return;
        if (!mailManager.isMailBag(item)) return;
        HumanEntity player = event.getWhoClicked();
        event.setCancelled(true);
        assert item != null;
        event.setCurrentItem(null);
        BundleMeta meta = (BundleMeta) item.getItemMeta();
        if (meta.hasItems()) {
            List<ItemStack> items = meta.getItems();
            for (ItemStack itemStack : items) {
                this.giveBack(player, itemStack);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void event(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        if (!mailManager.isMailBag(item)) return;
        BundleMeta meta = (BundleMeta) item.getItemMeta();
        if (!meta.hasItems()) return;
        List<ItemStack> items = new ArrayList<>(meta.getItems());
        if (items.isEmpty()) return;
        event.setCancelled(true);
        Inventory target = event.getDestination();
        ItemStack itemStack = items.removeLast();
        HashMap<Integer, ItemStack> failure = target.addItem(itemStack);
        if (!failure.isEmpty()) items.addAll(failure.values());
        meta.setItems(items);
        item.setItemMeta(meta);
    }

    protected void giveBack(HumanEntity player, ItemStack stack) {
        final Map<Integer, ItemStack> result = player.getInventory().addItem(stack);
        for (ItemStack value : result.values()) player.getWorld().dropItem(player.getLocation(), value);
    }
}
