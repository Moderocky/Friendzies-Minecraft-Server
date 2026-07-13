package mx.kenzie.survival.enchanting;

import mx.kenzie.ancillary.Enchantment;
import mx.kenzie.ancillary.Helper;
import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.effects.Effect;
import net.kyori.adventure.text.Component;
import net.minecraft.tags.TagKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public record EnchantmentDefinition(String id, Component name, int weight, int maxLevel, String[] precluded,
                                    String[] supported, String[] primary, int minBaseCost, int maxBaseCost,
                                    int minPerLevel, int maxPerLevel, int anvilCost, EquipmentSlot[] slots,
                                    Effect<?>[] effects,
                                    TagKey<net.minecraft.world.item.enchantment.Enchantment>[] tags) implements Keyed {


    public Enchantment toData() {
        Enchantment enchantment = new Enchantment();
        enchantment.description = Helper.componentSerialiser.apply(name);
        enchantment.weight = weight;
        enchantment.max_level = maxLevel;
        enchantment.exclusive_set = Arrays.stream(precluded).toList();
        enchantment.supported_items = Arrays.stream(supported).toList();
        enchantment.primary_items = Arrays.stream(primary).toList();
        enchantment.min_cost.base = minBaseCost;
        enchantment.min_cost.per_level_above_first = minPerLevel;
        enchantment.max_cost.base = maxBaseCost;
        enchantment.max_cost.per_level_above_first = maxPerLevel;
        enchantment.anvil_cost = anvilCost;
        enchantment.slots = Arrays.stream(slots).map(EquipmentSlot::getGroup).map(EquipmentSlotGroup::toString).toList();
        for (Effect<?> effect : effects) {
            enchantment.effects.put(effect.getKey().asString(), effect.getValue());
        }
        return enchantment;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return Survival.key(id);
    }
}
