package mx.kenzie.survival.builder;

import io.papermc.paper.datacomponent.DataComponentTypes;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.command.DepositCommand;
import mx.kenzie.survival.builder.command.GetCommand;
import mx.kenzie.survival.builder.command.WandCommand;
import mx.kenzie.survival.builder.task.Task;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;

import java.util.*;

public class BuildManager {
    public static final NamespacedKey WAND = NamespacedKey.minecraft("wand"),
            POS_1 = NamespacedKey.minecraft("pos_1"), POS_2 = NamespacedKey.minecraft("pos_2");
    public static final int MAX_SELECTION_SIZE = 100;
    private static int counter = 0;
    public final ItemStack wand;
    final Set<Task> tasks = Collections.synchronizedSet(new LinkedHashSet<>());
    final Map<Task, BossBar> bars = new HashMap<>();

    public BuildManager() {
        this.wand = this.createItem();
    }

    public static int createTaskId() {
        return ++counter;
    }

    protected ItemStack createItem() {
        final ItemStack stack = new ItemStack(Material.DEBUG_STICK);
        final ItemMeta meta = stack.getItemMeta();
        meta.customName(Component.text("Fairy Wand", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.textOfChildren(Component.text("Position #1 ", NamedTextColor.GRAY), Component.keybind("key.attack", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false),
                Component.textOfChildren(Component.text("Position #2 ", NamedTextColor.GRAY), Component.keybind("key.use", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false),
                Component.textOfChildren(Component.text("Action ", NamedTextColor.GRAY), Component.keybind("key.swapOffhand", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(NamespacedKey.minecraft("wand"), PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        stack.setData(DataComponentTypes.ITEM_MODEL, Survival.key("fairy_wand"));
        return stack;
    }

    public void runTask(Task task) {
        this.tasks.add(task);
        Player owner = task.owner();
        if (owner != null) {
            BossBar bar = BossBar.bossBar(task.name(), 0, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS);
            bar.addViewer(owner);
            bars.put(task, bar);
        }
    }

    public void setup() {
        final NamespacedKey key = new NamespacedKey(Survival.plugin, "builder_wand");
        final ShapedRecipe recipe = new ShapedRecipe(key, wand);
        recipe.shape(" N ", " S ", " S ");
        recipe.setIngredient('N', Material.NETHER_STAR);
        recipe.setIngredient('S', Material.STICK);
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        Bukkit.addRecipe(recipe);
        Bukkit.getPluginManager().registerEvents(new WandUseListener(), Survival.plugin);
        Bukkit.getPluginManager().registerEvents(new TickListener(), Survival.plugin);
        new WandCommand().register(Survival.plugin);
        new GetCommand().register(Survival.plugin);
        new DepositCommand().register(Survival.plugin);
    }

    public boolean isWand(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false;
        if (stack.getType() != wand.getType()) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(WAND);
    }

    public boolean wandAction(ItemStack stack, Block block, Action action) {
        if (block == null) return false;
        return this.wandAction(stack, block.getLocation().toVector().toBlockVector(), action);
    }

    public boolean wandAction(ItemStack stack, BlockVector block, Action action) {
        if (block == null) return false;
        final int[] ints = new int[]{block.getBlockX(), block.getBlockY(), block.getBlockZ()};
        return switch (action) {
            case RIGHT_CLICK_BLOCK -> {
                stack.editMeta(meta -> meta.getPersistentDataContainer().set(POS_2, PersistentDataType.INTEGER_ARRAY, ints));
                yield true;
            }
            case LEFT_CLICK_BLOCK -> {
                stack.editMeta(meta -> meta.getPersistentDataContainer().set(POS_1, PersistentDataType.INTEGER_ARRAY, ints));
                yield true;
            }
            default -> false;
        };
    }

    public int[][] wandPositions(ItemStack stack) {
        final int[][] positions = new int[2][];
        final PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
        if (!container.has(POS_1) || !container.has(POS_2)) return null;
        positions[0] = container.get(POS_1, PersistentDataType.INTEGER_ARRAY);
        positions[1] = container.get(POS_2, PersistentDataType.INTEGER_ARRAY);
        assert positions[0] != null && positions[0].length == 3;
        assert positions[1] != null && positions[1].length == 3;
        return positions;
    }

    private Location getLocation(Entity entity, int[] position) {
        assert position != null && position.length == 3;
        return new Location(entity.getWorld(), position[0], position[1], position[2]);
    }

    public boolean isValid(Player player, int[][] positions) {
        if (positions == null || player == null) return false;
        final Location a = new Location(player.getWorld(), positions[0][0], positions[0][1], positions[0][2]),
                b = new Location(player.getWorld(), positions[1][0], positions[1][1], positions[1][2]);
        final int max = MAX_SELECTION_SIZE * MAX_SELECTION_SIZE;
        if (a.distanceSquared(b) > max) return false;
        final Location start = player.getLocation();
        return !(start.distanceSquared(a) > max) || !(start.distanceSquared(b) > max);
    }

}
