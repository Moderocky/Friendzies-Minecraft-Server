package mx.kenzie.survival.attributes;

import mx.kenzie.survival.Survival;
import net.minecraft.core.Holder;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

@SuppressWarnings("ALL")
class CustomAttribute implements Attribute {

    protected Holder<net.minecraft.world.entity.ai.attributes.Attribute> internal;
    protected net.minecraft.world.entity.ai.attributes.Attribute instance;
    protected Attribute attribute;
    protected final String id;

    CustomAttribute(String id) {
        this.id = id;
    }

    void fulfil(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public @NotNull Sentiment getSentiment() {
        return attribute.getSentiment();
    }

    @Override
    public double getDefaultValue() {
        return attribute.getDefaultValue();
    }

    @Override
    public String translationKey() {
        return attribute.translationKey();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        if (attribute == null)
            return Survival.key(id);
        return attribute.getKey();
    }

    @Override
    public @NotNull String getTranslationKey() {
        return attribute.getTranslationKey();
    }

    @Override
    public int compareTo(@NonNull Attribute other) {
        return attribute.compareTo(other);
    }

    @Override
    public @NotNull String name() {
        return attribute.name();
    }

    @Override
    public int ordinal() {
        return attribute.ordinal();
    }
}
