package mx.kenzie.survival.potion;

import io.papermc.paper.datacomponent.DataComponentTypes;
import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedList;
import java.util.List;

public class WatchGlass implements Listener {

    public static final NamespacedKey KEY = Survival.key("watch_glass");
    public static final ItemStack ITEM;

    static {
        ITEM = new ItemStack(Material.BUNDLE);
        final ItemMeta meta = ITEM.getItemMeta();
        meta.customName(Component.text("Watch Glass").decoration(TextDecoration.ITALIC, false));
        meta.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, 1);
        ITEM.setItemMeta(meta);
        ITEM.setData(DataComponentTypes.ITEM_MODEL, KEY);
        ITEM.setData(DataComponentTypes.MAX_STACK_SIZE, 1);
    }

    public static boolean isWatchGlass(ItemStack item) {
        if (item == null) return false;
        return item.getPersistentDataContainer().has(KEY, PersistentDataType.INTEGER);
    }

    public static ItemStack distill(ItemStack ingredient, ItemStack glass, ItemStack result) {
        if (!isWatchGlass(glass)) return glass;
        ItemStack extracted = Survival.potionManager.takeIngredient(ingredient);
        if (extracted == null) return glass;
        transferInto(extracted, glass);
        return glass;
    }

    public static void transferInto(ItemStack input, ItemStack watchGlass) {
        BundleMeta meta = (BundleMeta) watchGlass.getItemMeta();
        if (!meta.hasItems() || meta.getItems().isEmpty()) {
            meta.setItems(List.of(input.asOne()));
            input.subtract();
            watchGlass.setItemMeta(meta);
        }

    }

    public static ItemStack takeItem(ItemStack watchGlass) {
        BundleMeta meta = (BundleMeta) watchGlass.getItemMeta();
        if (!meta.hasItems() || meta.getItems().isEmpty())
            return null;
        List<ItemStack> items = new LinkedList<>(meta.getItems());
        ItemStack stack = items.removeFirst();
        meta.setItems(items);
        watchGlass.setItemMeta(meta);
        return stack;
    }

    public void registerRecipe() {
        final ShapedRecipe recipe = new ShapedRecipe(KEY, ITEM);
        recipe.shape("   ", "G G", " A ");
        recipe.setIngredient('G', Material.GLASS);
        recipe.setIngredient('A', Material.AMETHYST_SHARD);
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler
    public void onInteract(InventoryClickEvent event) {
        ItemStack bundle, input;
        switch (event.getAction()) {
            case PICKUP_ALL_INTO_BUNDLE, PICKUP_SOME_INTO_BUNDLE:
                bundle = event.getCursor();
                input = event.getCurrentItem();
                break;
            case PLACE_ALL_INTO_BUNDLE, PLACE_SOME_INTO_BUNDLE:
                bundle = event.getCurrentItem();
                input = event.getCursor();
                break;
            default:
                return;
        }
        if (!isWatchGlass(bundle)) return;
        if (input == null) return;
        event.setCancelled(true);
        transferInto(input, bundle);
    }

}
