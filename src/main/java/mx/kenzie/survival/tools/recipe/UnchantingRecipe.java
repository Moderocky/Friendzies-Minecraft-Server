package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.utility.DefaultMap;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class UnchantingRecipe implements AnvilRecipe {
    protected StorageFunction<EnchantmentStorageMeta> function = (a, b, c) -> a.addStoredEnchant(b, c, true);

    @Override
    public boolean matches(ItemStack s1, ItemStack s2) {
        if (s1 == null) return false;
        if (s1.getType() != Material.BOOK) return false;
        return !this.getEnchantments(s2).isEmpty();
    }

    protected void removeEnchantment(ItemStack item, Enchantment enchantment) {
        if (item == null || !item.hasItemMeta()) return;
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.removeStoredEnchant(enchantment);
            item.setItemMeta(meta);
        } else item.removeEnchantment(enchantment);
    }

    protected Map<Enchantment, Integer> getEnchantments(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return new DefaultMap<>(0);
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta)
            return meta.getStoredEnchants();
        else return item.getEnchantments();
    }

    @NotNull
    @Override
    public ItemStack[] getResult(ItemStack s1, ItemStack s2) {
        final ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta book = (EnchantmentStorageMeta) result.getItemMeta();
        book.customName(s1.getItemMeta().customName());
        final Map<Enchantment, Integer> enchantments = this.getEnchantments(s2);
        for (Enchantment enchantment : enchantments.keySet()) {
            this.storeEnchantment(book, function, enchantment, enchantments.get(enchantment));
            this.removeEnchantment(s2, enchantment);
        }
        result.setItemMeta(book);
        s1.setAmount(s1.getAmount() - 1);
        return new ItemStack[]{s1, s2, result};
    }

    @Override
    public int getRepairCost(ItemStack s1, ItemStack s2) {
        int i = 0;
        for (Integer integer : this.getEnchantments(s2).values()) i = i + integer;
        i = (int) Math.round(Math.pow(i, 1.05));
        return i + 1;
    }

    protected <Storage> void storeEnchantment(Storage storage, StorageFunction<Storage> function, Enchantment enchantment, int level) {
        function.store(storage, enchantment, level);
    }

    protected interface StorageFunction<Storage> {
        void store(Storage storage, Enchantment enchantment, int level);
    }

}
