package mx.kenzie.survival.utility;

import mx.kenzie.clockwork.collection.ClockList;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class ItemBuilder {

    protected ItemStack stack;

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder() {
        this(Material.STICK);
    }

    public ItemBuilder setName(Component name) {
        this.stack.editMeta(meta -> meta.displayName(name));
        return this;
    }

    public ItemBuilder setName(String name) {
        this.stack.editMeta(meta -> meta.displayName(Component.text(name).decoration(TextDecoration.ITALIC, false)));
        return this;
    }

    public ItemBuilder setName(String name, TextColor color) {
        this.stack.editMeta(meta -> meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false)));
        return this;
    }

    public ItemBuilder addLore(Component... lore) {
        this.stack.editMeta(meta -> {
            final List<Component> current = new ArrayList<>(Objects.requireNonNullElse(meta.lore(), Collections.emptyList()));
            current.addAll(List.of(lore));
            meta.lore(current);
        });
        return this;
    }

    public ItemBuilder addLore(String line, TextColor color) {
        this.stack.editMeta(meta -> {
            final List<Component> current = new ArrayList<>(Objects.requireNonNullElse(meta.lore(), Collections.emptyList()));
            current.add(Component.text(line, color).decoration(TextDecoration.ITALIC, false));
            meta.lore(current);
        });
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.stack.setAmount(amount);
        return this;
    }

    public ClockList<Component> getLore() {
        final List<Component> list = this.stack.getItemMeta().lore();
        if (list == null || list.isEmpty()) return new ClockList<>();
        return new ClockList<>(Component.class, list);
    }

    public ItemBuilder setLore(Component... lore) {
        this.stack.editMeta(meta -> meta.lore(List.of(lore)));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <Meta extends ItemMeta> ItemBuilder setMeta(Consumer<Meta> meta) {
        final ItemMeta current = stack.getItemMeta();
        meta.accept((Meta) current);
        this.stack.setItemMeta(current);
        return this;
    }

    public <Meta extends ItemMeta> ItemBuilder setMeta(Class<Meta> type, Consumer<Meta> meta) {
        this.stack.editMeta(type, meta);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <Meta extends ItemMeta> Meta getItemMeta() {
        return (Meta) stack.getItemMeta();
    }

    public ItemBuilder setItemMeta(ItemMeta meta) {
        this.stack.setItemMeta(meta);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <Meta extends ItemMeta> Meta getItemMeta(Class<Meta> meta) {
        return (Meta) stack.getItemMeta();
    }

    public ItemBuilder setMaterial(Material material) {
        this.stack.setType(material);
        return this;
    }

    public ItemBuilder hideFlags(ItemFlag... flags) {
        if (flags.length == 0) this.stack.editMeta(meta -> meta.addItemFlags(ItemFlag.values()));
        else this.stack.editMeta(meta -> meta.addItemFlags(flags));
        return this;
    }

    public ItemStack getItem() {
        return stack;
    }

    public ItemBuilder setItem(ItemStack stack) {
        this.stack = stack;
        return this;
    }

}
