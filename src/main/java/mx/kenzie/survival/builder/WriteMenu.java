package mx.kenzie.survival.builder;

import mx.kenzie.survival.Survival;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.function.Consumer;

public class WriteMenu implements Listener {

    protected final Player owner;
    protected final Consumer<Material> result;
    final List<Component> pages = new ArrayList<>();
    private Book book;

    public WriteMenu(Player owner, Consumer<Material> result) {
        this.owner = owner;
        this.result = result;
    }

    public void open() {
        Bukkit.getPluginManager().registerEvents(this, Survival.plugin);
        this.owner.openBook(book);
    }

    public void close() {
        this.unregister();
    }

    protected void finish(Material material) {
        this.owner.closeInventory();
        this.close();
        this.result.accept(material);
    }

    @EventHandler
    public void event(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        final String content = event.getMessage().substring(1);
        try {
            final ItemStack stack = Bukkit.getItemFactory().createItemStack(content.trim());
            final Material material = stack.getType();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Survival.plugin, () -> this.finish(material));
        } catch (Throwable ex) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Survival.plugin, this::close);
            return;
        }
        event.setCancelled(true);
    }

    protected void unregister() {
        PlayerCommandPreprocessEvent.getHandlerList().unregister(this);
    }

    public void findMaterials(BoundingBox box) {
        final Set<Inventory> inventories = this.inventories(box, owner.getWorld());
        final Set<Material> materials = new HashSet<>(80);
        for (Inventory inventory : inventories) {
            for (ItemStack stack : inventory) {
                if (stack == null || stack.getType() == Material.AIR) continue;
                materials.add(stack.getType());
            }
        }
        TextComponent.Builder builder = Component.text();
        int count = 0;
        final List<Material> list = new ArrayList<>(materials);
        list.sort(Comparator.comparing(Material::getKey));
        for (Material material : list) {
            if (count > 0) builder.append(Component.newline());
            builder.append(Component.translatable(material)
                    .hoverEvent(Component.text("Click to Search"))
                    .clickEvent(ClickEvent.runCommand("/" + material.getKey())));
            count++;
            if (count > 13) {
                this.pages.add(builder.asComponent());
                builder = Component.text();
                count = 0;
            }
        }
        if (count > 0) this.pages.add(builder.asComponent());
        this.book = Book.book(Component.text("Search"), Component.text("Click to Select"), pages.toArray(new Component[0]));
    }

    protected Set<Inventory> inventories(BoundingBox box, World world) {
        final Location start = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
        final Set<Inventory> inventories = new LinkedHashSet<>();
        final int height = (int) box.getHeight() + 1, width = (int) box.getWidthX() + 1, length = (int) box.getWidthZ() + 1;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                for (int z = 0; z < length; z++) {
                    final Block block = start.clone().add(x, y, z).getBlock();
                    if (block.isEmpty()) continue;
                    if (block.getState() instanceof InventoryHolder container)
                        inventories.add(container.getInventory());
                    else if (block.getState() instanceof EnderChest) inventories.add(owner.getEnderChest());
                }
        return inventories;
    }
}
