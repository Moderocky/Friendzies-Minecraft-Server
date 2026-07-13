package mx.kenzie.survival.builder.command;

import mx.kenzie.centurion.TypedArgument;
import mx.kenzie.clockwork.collection.ClockList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemArgument extends TypedArgument<Material> {

    private int lastHash;
    private ItemStack lastValue;

    public ItemArgument() {
        super(Material.class);
        this.label = "item";
        final ClockList<String> possibilities = new ClockList<>();
        for (Material value : Material.values()) {
            if (value.isLegacy()) continue;
            if (value.isAir()) continue;
            if (!value.isItem()) continue;
            possibilities.add(value.getKey().getKey());
        }
        this.possibilities = possibilities.toArray();
    }

    @Override
    public boolean matches(String s) {
        this.lastHash = s.hashCode();
        try {
            this.lastValue = Bukkit.getItemFactory().createItemStack(s);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public Material parse(String s) {
        if (s.hashCode() == lastHash) return lastValue.getType();
        this.lastHash = s.hashCode();
        return (lastValue = Bukkit.getItemFactory().createItemStack(s)).getType();
    }

}
