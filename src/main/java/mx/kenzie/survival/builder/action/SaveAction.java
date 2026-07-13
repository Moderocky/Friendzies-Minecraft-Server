package mx.kenzie.survival.builder.action;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.BuildingInventory;
import mx.kenzie.survival.utility.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveAction implements Action {

    public static ItemStack createBook(Structure structure, CommandSender maker, NamespacedKey id) {
        final String author;
        if (maker instanceof Player player) author = player.getName();
        else author = "Server";
        final Palette palette = structure.getPalettes().getFirst();
        final BlockVector vector = structure.getSize();
        return new ItemBuilder(Material.WRITTEN_BOOK)
                .setMeta(BookMeta.class, meta -> {
                    meta.setTitle("Blueprint");
                    meta.setAuthor(author);
                    meta.setGeneration(BookMeta.Generation.COPY_OF_ORIGINAL);
                    meta.addPages(pages(palette));
                })
                .setMeta(meta -> meta.getPersistentDataContainer().set(NamespacedKey.minecraft("structure"), PersistentDataType.STRING, id.toString()))
                .setName(Component.textOfChildren(
                        Component.text(vector.getBlockX() + "×" + vector.getBlockY() + "×" + vector.getBlockZ(), NamedTextColor.AQUA),
                        Component.space(),
                        Component.text("Blueprint", NamedTextColor.AQUA)
                ).decoration(TextDecoration.ITALIC, false))
                .getItem();
    }

    protected static Component[] pages(Palette palette) {
        final List<Component> pages = new LinkedList<>();
        final Map<Material, AtomicInteger> map = new LinkedHashMap<>();
        for (BlockState block : palette.getBlocks()) {
            map.putIfAbsent(block.getType(), new AtomicInteger(0));
            map.get(block.getType()).incrementAndGet();
        }
        TextComponent.Builder builder = Component.text();
        int count = 0;
        final List<Material> list = new ArrayList<>(map.keySet());
        list.sort(Comparator.comparing(Material::getKey));
        for (Material material : list) {
            if (count > 0) builder.append(Component.newline());
            final int amount = map.get(material).get();
            builder.append(Component.textOfChildren(
                    Component.translatable(material),
                    Component.space(),
                    Component.text("×" + amount)
            ));
            count++;
            if (count > 13) {
                pages.add(builder.asComponent());
                builder = Component.text();
                count = 0;
            }
        }
        if (count > 0) pages.add(builder.asComponent());
        return pages.toArray(new Component[0]);
    }

    @Override
    public void run(Player player, BuildingInventory resources, int[][] position) {
        final BoundingBox box = this.box(position);
        final Location from = box.getMin().toLocation(player.getWorld()).toCenterLocation();
        final Location to = box.getMax().toLocation(player.getWorld()).add(1, 1, 1).toCenterLocation();
        final Structure structure = Bukkit.getStructureManager().createStructure();
        structure.fill(from, to, false);
        final UUID uuid = UUID.randomUUID();
        final String id = "gen_" + Math.abs(uuid.getMostSignificantBits()) + '_' + Math.abs(player.getUniqueId().getMostSignificantBits() ^ uuid.getLeastSignificantBits());
        Bukkit.getScheduler().runTaskAsynchronously(Survival.plugin, () -> {
            final NamespacedKey key = NamespacedKey.minecraft("schematic/" + id);
            try {
                Bukkit.getStructureManager().registerStructure(key, structure);
                Bukkit.getStructureManager().saveStructure(key, structure);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            final ItemStack stack = createBook(structure, player, key);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Survival.plugin, () -> {
                player.getInventory().addItem(stack);
                player.sendActionBar(Component.text("Saved Blueprint"));
            });
        });
    }

}
