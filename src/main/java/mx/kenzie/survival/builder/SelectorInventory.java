package mx.kenzie.survival.builder;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.utility.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class SelectorInventory implements Listener, Closeable {

    public final Inventory inventory;
    public final Player owner;
    protected final ItemStack[] icons;
    protected final BlockData[] data;
    protected final Set<BlockData> chosen;
    protected final Consumer<Set<BlockData>> result;

    public SelectorInventory(Player owner, BlockData[] data, Consumer<Set<BlockData>> chosen) {
        this.owner = owner;
        this.data = data;
        this.icons = this.createIcons(data);
        final int size = this.inventorySize(data);
        this.inventory = Bukkit.createInventory(owner, size, Component.text("Select Materials"));
        for (int i = 0; i < Math.min(icons.length, size); i++) inventory.setItem(i, icons[i]);
        this.chosen = new HashSet<>();
        this.result = chosen;
        Bukkit.getPluginManager().registerEvents(this, Survival.plugin);
    }

    protected int inventorySize(BlockData[] data) {
        final int length = data.length;
        if (length > 45) return 54;
        if (length > 36) return 45;
        if (length > 27) return 36;
        if (length > 18) return 27;
        if (length > 9) return 18;
        return 9;
    }

    protected ItemStack[] createIcons(BlockData[] data) {
        final List<ItemStack> list = new ArrayList<>(data.length);
        for (BlockData datum : data) {
            final Material material = datum.getMaterial();
            list.add(new ItemBuilder(material.isAir() ? Material.STRUCTURE_VOID : material.isItem() ? material : Material.BARRIER)
                    .setName(Component.translatable(datum.getMaterial(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
//                .setLore(this.states(datum))
                    .hideFlags()
                    .getItem());
        }
        return list.toArray(new ItemStack[0]);
    }

//    private Component[] states(BlockData data) {
//        final CompoundTag tag = ((CraftBlockData) data).toStates();
//        final List<Component> list = new ArrayList<>(tag.size());
//        for (String key : tag.getAllKeys()) {
//            list.add(Component.text(key + " = " + tag.get(key), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
//        }
//        return list.toArray(new Component[0]);
//    }

    public Inventory getInventory() {
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
        if (slot >= data.length) return;
        if (chosen.remove(data[slot])) icon.removeEnchantment(Enchantment.INFINITY);
        else {
            chosen.add(data[slot]);
            icon.addUnsafeEnchantment(Enchantment.INFINITY, 1);
        }
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(Survival.plugin, () -> result.accept(chosen));
    }

    public void close() {
        this.unregister();
        this.inventory.clear();
    }

}
