package mx.kenzie.survival.utility.editor;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import org.bukkit.Material;
import org.bukkit.craftbukkit.inventory.CraftItemType;
import org.bukkit.inventory.ItemType;

import java.io.Closeable;

public class ItemEditor implements Closeable {

    protected final ItemType type;
    protected final Item item;
    protected DataComponentMap defaults;
    protected DataComponentMap.Builder builder;


    public ItemEditor(Material material) {
        this(material.asItemType());
    }

    public ItemEditor(ItemType itemType) {
        this.type = itemType;
        this.item = ((CraftItemType<?>) itemType).getHandle();
        this.defaults = item.components();
        this.builder = DataComponentMap.builder();
        this.addDefaults();
    }

    public void addDefaults() {
        this.builder.addAll(defaults);
    }

    public void clear() {
        this.builder = DataComponentMap.builder();
    }

    public void copyFrom(Material other) {
        this.copyFrom(other.asItemType());
    }

    public void copyFrom(ItemType other) {
        this.builder.addAll(((CraftItemType<?>) other).getHandle().components());
    }

    public <Type> void edit(DataComponentType<Type> type, Type value) {
        this.builder.set(type, value);
    }

    public <Type> void remove(DataComponentType<Type> type) {
        this.builder.set(type, null);
    }

    protected void overwrite() {
        //noinspection deprecation
        this.item.builtInRegistryHolder().bindComponents(this.builder.build());
    }

    @Override
    public void close() {
        this.overwrite();
    }
}
