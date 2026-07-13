package mx.kenzie.survival.bag;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;

import java.util.ArrayList;
import java.util.List;

public class PickUpListener implements Listener {

    BagManager bagManager;

    public PickUpListener(BagManager bagManager) {
        this.bagManager = bagManager;
    }

    public static void mergeItem(ItemStack item, List<ItemStack> items) {
        if (item == null) return;
        for (ItemStack stack : items) {
            if (stack == null) continue;
            int amount = stack.getAmount();
            int max = stack.getMaxStackSize();
            if (amount >= max) continue;
            if (!stack.isSimilar(item)) continue;
            int remaining = max - amount;
            int available = item.getAmount();
            int taken = Math.min(remaining, available);
            stack.add(taken);
            int total = available - taken;
            item.setAmount(total);
            if (total < 1) break;
        }
        if (item.getAmount() > 0) items.add(item);
    }

    private void addItem0(BundleMeta bundle, ItemStack item) {
        if (item == null) return;
        if (bundle.hasItems()) bundle.addItem(item);
        else bundle.setItems(List.of(item));
    }

    private void addItem(BundleMeta bundle, ItemStack item) {
        if (item == null) return;
        if (bundle.hasItems()) {
            List<ItemStack> items = new ArrayList<>(bundle.getItems());
            mergeItem(item, items);
            bundle.setItems(items);
        } else bundle.setItems(List.of(item));
    }

    @EventHandler
    public void event(EntityPickupItemEvent event) {
        if (event.isCancelled()) return;
        LivingEntity entity = event.getEntity();
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return;
        ItemStack item;
        if (bagManager.isBag(equipment.getItem(EquipmentSlot.HAND))) {
            item = equipment.getItem(EquipmentSlot.HAND);
        } else if (bagManager.isBag(equipment.getItem(EquipmentSlot.OFF_HAND))) {
            item = equipment.getItem(EquipmentSlot.OFF_HAND);
        } else {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof BundleMeta bundle)) return;
        event.setCancelled(true);
        Item source = event.getItem();
        ItemStack taker = source.getItemStack();
        source.setPickupDelay(1000000);
        this.addItem(bundle, taker);
        item.setItemMeta(bundle);
        entity.playPickupItemAnimation(source);
        source.remove();
    }

    @EventHandler
    public void event(PlayerSwapHandItemsEvent event) {
        if (true) return; // fix later
        if (event.isCancelled()) return;
        final ItemStack stack = event.getOffHandItem();
        if (!bagManager.isBag(stack)) return;
        ItemMeta itemMeta = stack.getItemMeta();
        if (!(itemMeta instanceof BundleMeta bundle)) return;
        event.setCancelled(true);
        final Player player = event.getPlayer();
        List<Entity> nearbyEntities = player.getNearbyEntities(12, 6, 12);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Item item)) continue;
            if (!player.hasLineOfSight(item)) continue;
            item.setPickupDelay(1000000);
            player.playPickupItemAnimation(item);
            this.addItem(bundle, item.getItemStack().clone());
            item.remove();
        }
        stack.setItemMeta(bundle);
    }

    @EventHandler
    public void event(PlayerInteractAtEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        if (target.isDead()) return;
        ItemStack item = player.getEquipment().getItemInMainHand();
        if (!bagManager.isBag(item)) return;
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof BundleMeta bundle)) return;
        EntityType type = target.getType();
        if (!type.isSpawnable()) return;
        EntitySnapshot snapshot = target.createSnapshot();
        if (snapshot == null) return;
        Material material;
        try {
            material = Material.valueOf(type.name() + "_SPAWN_EGG");
        } catch (IllegalArgumentException ignored) {
            return;
        }
        event.setCancelled(true);
        ItemStack egg = new ItemStack(material);
        SpawnEggMeta meta = (SpawnEggMeta) egg.getItemMeta();
        meta.setSpawnedEntity(snapshot);
        egg.setItemMeta(meta);
        target.remove();
        this.addItem0(bundle, egg);
        item.setItemMeta(bundle);
    }

    @EventHandler
    public void event(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) return;
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (!(block.getState() instanceof Container container)) return;
        ItemStack item;
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        if (bagManager.isBag(equipment.getItemInMainHand())) {
            item = equipment.getItem(EquipmentSlot.HAND);
        } else if (bagManager.isBag(equipment.getItem(EquipmentSlot.OFF_HAND))) {
            item = equipment.getItem(EquipmentSlot.OFF_HAND);
        } else {
            return;
        }
        ItemMeta itemMeta = item.getItemMeta();
        if (!(itemMeta instanceof BundleMeta bundle)) return;
        Inventory inventory = container.getInventory();
        if (player.isSneaking()) {
            // take contents
            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            List<ItemStack> items;
            if (bundle.hasItems()) items = new ArrayList<>(bundle.getItems());
            else items = new ArrayList<>(16);
            for (ItemStack stack : inventory) {
                mergeItem(stack, items);
            }
            bundle.setItems(items);
            inventory.clear();
        } else {
            // deposit (matching) contents in storage
            if (!bundle.hasItems()) return;
            event.setCancelled(true);
            List<ItemStack> items = bundle.getItems();
            List<ItemStack> result = new ArrayList<>(items.size());
            if (inventory.isEmpty()) {
                for (ItemStack stack : items) {
                    result.addAll(inventory.addItem(stack).values());
                }
            } else {
                for (ItemStack stack : items) {
                    if (inventory.containsAtLeast(stack, 1))
                        result.addAll(inventory.addItem(stack).values());
                    else result.add(stack);
                }
            }
            bundle.setItems(result);
        }
        item.setItemMeta(bundle);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void dropContents(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY) return;
        Block block = event.getClickedBlock();
        if (event.getAction() != Action.PHYSICAL) return;
        if (block == null) return;
        Player player = event.getPlayer();
        EntityEquipment equipment = player.getEquipment();
        if (bagManager.isBag(equipment.getItemInMainHand())
                || bagManager.isBag(equipment.getItem(EquipmentSlot.OFF_HAND)))
            event.setUseItemInHand(Event.Result.DENY);
    }

}
