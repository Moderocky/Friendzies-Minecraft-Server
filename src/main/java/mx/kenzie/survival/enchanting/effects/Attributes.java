package mx.kenzie.survival.enchanting.effects;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.enchanting.LevelProvider;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;

import java.util.*;

public record Attributes(Attribute... attributes) implements Effect<List<Object>> {

    @Override
    public @NonNull NamespacedKey getKey() {
        return NamespacedKey.minecraft("attributes");
    }

    @Override
    public List<Object> getValue() {
        return Arrays.stream(attributes).map(Attribute::serialise).toList();
    }

    public enum Operation {
        ADD_VALUE,
        ADD_MULTIPLIED_BASE,
        ADD_MULTIPLIED_TOTAL
    }

    public record Attribute(NamespacedKey attribute, NamespacedKey id, LevelProvider amount, Operation operation) {

        public Attribute(String attribute, NamespacedKey id, LevelProvider amount, Operation operation) {
            this(NamespacedKey.minecraft(attribute), id, amount, operation);
        }

        public Attribute(String attribute, String id, LevelProvider amount, Operation operation) {
            this(NamespacedKey.minecraft(attribute), Survival.key(id), amount, operation);
        }

        public Attribute(String attribute, String id, LevelProvider amount) {
            this(NamespacedKey.minecraft(attribute), Survival.key(id), amount, Operation.ADD_VALUE);
        }

        public Attribute(String attribute, String id, float perLevel) {
            this(NamespacedKey.minecraft(attribute), Survival.key(id), LevelProvider.perLevel(perLevel), Operation.ADD_VALUE);
        }

        Object serialise() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("attribute", attribute.asString());
            map.put("id", id.asString());
            map.put("amount", amount.serialise());
            map.put("operation", operation.name().toLowerCase(Locale.ROOT));
            return map;
        }

    }

}
