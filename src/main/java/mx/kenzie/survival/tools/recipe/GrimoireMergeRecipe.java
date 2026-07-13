package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.EnchantingManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class GrimoireMergeRecipe extends UnchantingRecipe {

    private final EnchantingManager manager = Survival.enchantingManager;

    @Override
    public boolean matches(ItemStack s1, ItemStack s2) {
        if (!manager.isGrimoire(s1)) return false;
        if (s2 == null || !(s2.getItemMeta() instanceof EnchantmentStorageMeta meta)) return false;
        return !meta.getStoredEnchants().isEmpty();
    }

    @NotNull
    @Override
    public ItemStack[] getResult(ItemStack s1, ItemStack s2) {
        if (s2 == null || !manager.isGrimoire(s1) || !(s2.getItemMeta() instanceof EnchantmentStorageMeta meta))
            return new ItemStack[]{s1, s2, ItemStack.empty()};
        final ItemStack result = s1.clone();
        final Map<Enchantment, Integer> enchantments = meta.getStoredEnchants();
        this.manager.mergeStorage(result, enchantments);
        this.manager.updateGrimoire(result);
        return new ItemStack[]{ItemStack.empty(), s2.subtract(), result};
    }

    @Override
    public void postCompletion(Player crafter, ItemStack s1, ItemStack s2, ItemStack... results) {
        crafter.give(new ItemStack(Material.BOOK));
        super.postCompletion(crafter, s1, s2, results);
    }

    @Override
    public int getRepairCost(ItemStack s1, ItemStack s2) {
        return 0;
    }
}
