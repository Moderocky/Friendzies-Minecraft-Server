package mx.kenzie.survival.builder.command;

import mx.kenzie.centurion.*;
import mx.kenzie.clockwork.collection.ClockList;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.task.CancellableTask;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import static mx.kenzie.centurion.Arguments.INTEGER;
import static mx.kenzie.centurion.CommandResult.LAPSE;
import static mx.kenzie.centurion.CommandResult.PASSED;

public class GetCommand extends MinecraftCommand {
    static final ItemArgument ITEM = new ItemArgument();
    protected static Map<Player, int[][]> storagePositions = new WeakHashMap<>();

    public GetCommand() {
        super("Summon resources from your storage.");
        this.permission = null; // = "survival.command.get";
    }

    public static StorageArea getStorageArea(Player player, boolean near) {
        if (!near) return getStorageArea(player);
        return new StorageArea(player.getWorld(), BoundingBox.of(player.getLocation(), 7, 7, 7), false);
    }

    public static StorageArea getStorageArea(Player player) {
        final PersistentDataContainer container = player.getPersistentDataContainer();
        if (!container.has(NamespacedKey.minecraft("storage"), PersistentDataType.TAG_CONTAINER)) return null;
        final PersistentDataContainer inner = container.get(NamespacedKey.minecraft("storage"), PersistentDataType.TAG_CONTAINER);
        if (inner == null) return null;
        final String name = inner.get(NamespacedKey.minecraft("world"), PersistentDataType.STRING);
        if (name == null) return null;
        final World world = Bukkit.getWorld(name);
        if (world == null) return null;
        final int[] pos = inner.get(NamespacedKey.minecraft("position"), PersistentDataType.INTEGER_ARRAY);
        if (pos == null || pos.length < 6) return null;
        return new StorageArea(world, new BoundingBox(pos[0], pos[1], pos[2], pos[3], pos[4], pos[5]));
    }

    public static void setStorageArea(Player player, StorageArea area) {
        storagePositions.remove(player);
        final PersistentDataContainer container = player.getPersistentDataContainer();
        final NamespacedKey key = NamespacedKey.minecraft("storage");
        if (area == null) container.remove(key);
        else {
            final BoundingBox box = area.box;
            final PersistentDataContainer inner = container.getAdapterContext().newPersistentDataContainer();
            final int[] pos = new int[]{
                    (int) box.getMinX(), (int) box.getMinY(), (int) box.getMinZ(),
                    (int) box.getMaxX(), (int) box.getMaxY(), (int) box.getMaxZ()
            };
            inner.set(NamespacedKey.minecraft("position"), PersistentDataType.INTEGER_ARRAY, pos);
            final World world = area.world;
            inner.set(NamespacedKey.minecraft("world"), PersistentDataType.STRING, world.getName());
            container.set(key, PersistentDataType.TAG_CONTAINER, inner);
        }
    }

    public static void giveItemsSafely(Inventory player, Location drop, Collection<ItemStack> items) {
        giveItemsSafely(player, drop, items.toArray(new ItemStack[0]));
    }

    public static void giveItemsSafely(Inventory player, Location drop, ItemStack... items) {
        for (ItemStack value : player.addItem(items).values()) {
            drop.getWorld().dropItem(drop, value);
        }
    }

    @Override
    public MinecraftBehaviour create() {
        final ColorProfile profile = this.getProfile();
        TypedArgument<Material> argItem = ITEM.described("The type of item to retrieve.");
        TypedArgument<Integer> argAmount = INTEGER.withLapse(1).asOptional().described("The number of items to retrieve.").labelled("amount");
        return command("get")
                .arg(argItem, argAmount, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final StorageArea area = getStorageArea(player);
                    if (area == null) {
                        player.sendMessage(Component.text("You have no storage area.", profile.light()));
                        return PASSED;
                    }
                    final Material material = arguments.get(0);
                    final int amount = arguments.get(1);
                    if (amount < 1) return PASSED;
                    final StorageTask task = new StorageTask(area, player, amount, material);
                    Survival.builder.runTask(task);
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Retrieving ", profile.dark()),
                            Component.text(amount, profile.highlight()),
                            Component.text("× ", profile.pop()),
                            Component.translatable(material, profile.highlight()),
                            Component.text(" from storage.", profile.dark())
                    ));
                    //</editor-fold>
                    return PASSED;
                });
    }

    private CommandResult fetch(CommandSender sender, Arguments arguments) {
        final ColorProfile profile = this.getProfile();
        if (!(sender instanceof Player player)) return LAPSE;
        final StorageArea area = getStorageArea(player);
        if (area == null) {
            player.sendMessage(Component.text("You have no storage area.", profile.light()));
            return PASSED;
        }
        final Material material = arguments.get(0);
        final int amount = arguments.get(1);
        if (amount < 1) return PASSED;
        final StorageTask task = new StorageTask(area, player, amount, material);
        Survival.builder.runTask(task);
        //<editor-fold desc="Message" defaultstate="collapsed">
        player.sendMessage(Component.textOfChildren(
                Component.text("Retrieving ", profile.dark()),
                Component.text(amount, profile.highlight()),
                Component.text("× ", profile.pop()),
                Component.translatable(material, profile.highlight()),
                Component.text(" from storage.", profile.dark())
        ));
        //</editor-fold>
        return PASSED;
    }

    static class StorageTask extends CancellableTask {

        final Player player;
        private final Material material;
        private final Location start;
        private final ClockList<Chunk> chunks = new ClockList<>();
        protected int[][] positions;
        protected int total, count;
        protected ClockList<ItemStack> items = new ClockList<>();
        private int amount;

        public StorageTask(StorageArea area, Player player, int amount, Material material) {
            this.start = area.box.getMin().toLocation(area.world);
            this.player = player;
            this.amount = amount;
            this.material = material;
            this.prepare(start);
            final BoundingBox box = area.box;
            final int height = (int) box.getHeight() + 1, width = (int) box.getWidthX() + 1, length = (int) box.getWidthZ() + 1;
            if (area.real) positions = storagePositions.get(player);
            if (positions != null) return;
            this.positions = this.findStorage(height, width, length);
            if (area.real) storagePositions.put(player, positions);
        }

        @Override
        public Component name() {
            return Component.text("Accessing Storage");
        }

        protected int[][] findStorage(int height, int width, int length) {
            final ClockList<int[]> list = new ClockList<>();
            for (int y = 0; y < height; y++)
                for (int x = 0; x < width; x++)
                    for (int z = 0; z < length; z++) {
                        final Location location = start.clone().add(x, y, z);
                        this.prepare(location);
                        final Block block = location.getBlock();
                        if (block.isEmpty()) continue;
                        if (block.getState() instanceof InventoryHolder) list.add(new int[]{x, y, z});
                        else if (block.getState() instanceof EnderChest) list.add(new int[]{x, y, z});
                    }
            return list.toArray();
        }

        protected void prepare(Location location) {
            final Chunk chunk = location.getChunk();
            if (chunks.contains(chunk) || chunk.isLoaded()) return;
            this.chunks.add(chunk);
            chunk.addPluginChunkTicket(Survival.plugin);
            chunk.load();
        }

        @Override
        public boolean tick() {
            if (finished || cancelled) return false;
            if (count >= positions.length || amount < 1) {
                this.finish();
                return false;
            }
            final int[] pos = positions[count++];
            final Location location = start.clone().add(pos[0], pos[1], pos[2]);
            this.prepare(location);
            final Block block = location.getBlock();
            if (block.isEmpty()) return false;
            if (block.getState() instanceof InventoryHolder container) return this.check(container.getInventory());
            else if (block.getState() instanceof EnderChest) return this.check(player.getEnderChest());
            return false;
        }

        protected boolean check(Inventory inventory) {
            for (ItemStack content : inventory.getContents()) {
                if (content == null) continue;
                if (content.getType() == Material.AIR) continue;
                if (content.getAmount() < 1) continue;
                if (content.getType() != material) continue;
                final ItemStack copy = content.clone();
                final int take = Math.min(amount, content.getAmount());
                content.setAmount(content.getAmount() - take);
                copy.setAmount(take);
                this.amount -= take;
                this.items.add(copy);
                if (amount == 0) break;
            }
            return false;
        }

        @Override
        public int getTotalStages() {
            return total;
        }

        @Override
        public int getStage() {
            return count;
        }

        @Override
        public int remaining() {
            return 1;
        }

        @Override
        public void onEnd() {
            super.onEnd();
            for (Chunk chunk : chunks) chunk.removePluginChunkTicket(Survival.plugin);
            int count = 0;
            for (ItemStack item : items) count += item.getAmount();
            giveItemsSafely(player.getInventory(), player.getLocation(), items);
            this.sendMessage(count);
            this.items.clear();
        }

        protected void sendMessage(int count) {
            final ColorProfile profile = DEFAULT_PROFILE;
            //<editor-fold desc="Message" defaultstate="collapsed">
            this.player.sendMessage(Component.textOfChildren(
                    Component.text("Retrieved ", profile.dark()),
                    Component.text(count, profile.highlight()),
                    Component.text("× ", profile.pop()),
                    Component.translatable(material, profile.highlight()),
                    Component.text(" from storage.", profile.dark())
            ));
            //</editor-fold>
        }

        @Override
        public @Nullable Player owner() {
            return player;
        }

    }

    public record StorageArea(World world, BoundingBox box, boolean real) {

        public StorageArea(World world, BoundingBox box) {
            this(world, box, true);
        }

    }

}

