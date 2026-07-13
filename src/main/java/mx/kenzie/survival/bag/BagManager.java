package mx.kenzie.survival.bag;

import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static mx.kenzie.survival.bag.PickUpListener.mergeItem;

public class BagManager {
    public static final NamespacedKey BAG = NamespacedKey.minecraft("bag");

    public final ItemStack bag;

    public BagManager() {
        this.bag = this.createItem();
    }

    public ItemStack createItem() {
        final ItemStack stack = new ItemStack(Material.BLUE_BUNDLE);
        final ItemMeta meta = stack.getItemMeta();
        meta.customName(Component.text("Dokkaebi Bag", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.textOfChildren(Component.score("Coins", "coins").color(NamedTextColor.AQUA), Component.text(" Coins", NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false),
                Component.textOfChildren(Component.text("Action ", NamedTextColor.GRAY), Component.keybind("key.swapOffhand", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(BAG, PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public void setup() {
        final NamespacedKey key = new NamespacedKey(Survival.plugin, "dokkaebi_bag");
        final ShapedRecipe recipe = new ShapedRecipe(key, bag);
        recipe.shape(" T ", "LSL", " L ");
        recipe.setIngredient('T', Material.STRING);
        recipe.setIngredient('L', Material.RABBIT_HIDE);
        recipe.setIngredient('S', Material.NETHER_STAR);
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        Bukkit.addRecipe(recipe);
        Bukkit.getPluginManager().registerEvents(new PickUpListener(this), Survival.plugin);
    }

    public boolean isBag(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR) return false;
        if (stack.getType() != bag.getType()) return false;
        return stack.getItemMeta().getPersistentDataContainer().has(BAG);
    }

    public void addItems(ItemStack bag, ItemStack item) {
        if (item == null) return;
        assert this.isBag(bag);
        BundleMeta bundle = (BundleMeta) bag.getItemMeta();
        if (bundle.hasItems()) {
            List<ItemStack> items = new ArrayList<>(bundle.getItems());
            mergeItem(item, items);
            bundle.setItems(items);
        } else bundle.setItems(List.of(item));
    }

}
