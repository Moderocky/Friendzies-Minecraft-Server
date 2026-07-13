package mx.kenzie.survival.tools.recipe;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.potion.PotionManager;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PotionEnchantingRecipe implements AnvilRecipe {
    protected List<PotionEnchantment> enchantments = new ArrayList<>();

    {
        this.enchantments.add(tool -> Tag.ITEMS_SWORDS.isTagged(tool.getType()));
        this.enchantments.add(tool -> Tag.ITEMS_SPEARS.isTagged(tool.getType()));
        this.enchantments.add(tool -> tool.hasItemMeta() && tool.getItemMeta().hasEnchant(Enchantment.THORNS));
    }

    @Override
    public void postCompletion(Player crafter, ItemStack s1, ItemStack s2, ItemStack... results) {
        if (s2.getAmount() < 2) return;
        crafter.give(new ItemStack(Material.GLASS_BOTTLE));
    }

    @Override
    public boolean matches(ItemStack tool, ItemStack potion) {
        if (tool == null || potion == null) return false;
        if (potion.getType() != Material.POTION || !(potion.getItemMeta() instanceof PotionMeta meta)) return false;
        if (meta.getAllEffects().isEmpty()) return false;
        for (PotionEnchantment enchantment : enchantments) {
            if (enchantment.test(tool)) return getRepairCost(tool, potion) > 0;
        }
        return false;
    }

    @Override
    public @NotNull ItemStack[] getResult(ItemStack tool, ItemStack potion) {
        Collection<PotionEffect> current = this.getEffectsOn(tool);
        Collection<PotionEffect> adding = this.getEffectsOn(potion);
        Collection<PotionEffect> effects = this.effectsToAdd(current, adding);
        List<PotionEffect> result = new ArrayList<>(current);
        result.addAll(effects);
        PotionManager.setPotionEnchantmentEffects(tool, result, potion);
        Survival.potionManager.copyCompanionData(potion, tool);
        if (potion.getAmount() > 1)
            potion.setAmount(potion.getAmount() - 1);
        else potion = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = tool.getItemMeta();
        Survival.potionManager.fixListDetails(meta, this.getEffectsOn(tool));
        tool.setItemMeta(meta);
        return new ItemStack[]{ItemStack.empty(), potion, tool};
    }

    @Override
    public int getRepairCost(ItemStack tool, ItemStack potion) {
        if (tool == null || potion == null || !(potion.getItemMeta() instanceof PotionMeta))
            return 0;
        Collection<PotionEffect> current = this.getEffectsOn(tool);
        Collection<PotionEffect> adding = this.getEffectsOn(potion);
        Collection<PotionEffect> effects = this.effectsToAdd(current, adding);
        return effects.stream().mapToInt(PotionEffect::getAmplifier).map(i -> i + 1).sum();
    }

    private Collection<PotionEffect> getEffectsOn(ItemStack item) {
        if (item.getItemMeta() instanceof PotionMeta meta) {
            return meta.getAllEffects();
        }
        return PotionManager.getPotionEnchantmentEffects(item);
    }

    private Collection<PotionEffect> effectsToAdd(Collection<PotionEffect> effects, Collection<PotionEffect> added) {
        List<PotionEffect> list = new ArrayList<>(added.stream().filter(this::isSafeToAdd).map(this::forEquipment).toList());
        if (effects.isEmpty()) return list;
        Set<@NotNull PotionEffectType> collect = effects.stream().map(PotionEffect::getType).collect(Collectors.toSet());
        list.removeIf(effect -> collect.contains(effect.getType()));
        return list;
    }

    private boolean isSafeToAdd(PotionEffect effect) {
        PotionEffectType type = effect.getType();
        if (type == PotionManager.DIMENSIONALITY) return true;
        return !type.isInstant();
    }

    private PotionEffect forEquipment(PotionEffect effect) {
        return effect.withAmplifier(Math.min(effect.getAmplifier(), 1)).withDuration(Math.min(3 * 20, effect.getDuration()));
    }

    public interface PotionEnchantment extends Predicate<ItemStack> {

    }


}
