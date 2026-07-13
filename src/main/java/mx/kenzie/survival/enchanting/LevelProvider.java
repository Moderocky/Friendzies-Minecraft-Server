package mx.kenzie.survival.enchanting;

import java.util.Map;

@FunctionalInterface
public interface LevelProvider {

    static LevelProvider constant(float value) {
        return new Constant(value);
    }

    static LevelProvider perLevel(float value) {
        return new Linear(value, value);
    }

    static LevelProvider perLevel(float base, float perLevel) {
        return new Linear(base, perLevel);
    }

    static LevelProvider lookup(float fallback, float... levelValues) {
        return new Lookup(fallback, levelValues);
    }

    static LevelProvider lookup(float... levelValues) {
        assert levelValues.length > 0;
        return lookup(levelValues[levelValues.length - 1], levelValues);
    }

    static LevelProvider clamped(LevelProvider provider, float min, float max) {
        return new Clamped(provider, min, max);
    }

    Object serialise();

    record Lookup(float fallback, float... values) implements LevelProvider {
        @Override
        public Object serialise() {
            return Map.of("type", "minecraft:lookup", "values", values, "fallback", fallback);
        }
    }

    record Clamped(LevelProvider provider, float min, float max) implements LevelProvider {
        @Override
        public Object serialise() {
            return Map.of("type", "minecraft:clamped", "value", provider.serialise(), "min", min, "max", max);
        }

    }

    record Constant(float value) implements LevelProvider {
        @Override
        public Object serialise() {
            return value;
        }
    }

    record Linear(float base, float perLevel) implements LevelProvider {
        @Override
        public Object serialise() {
            return Map.of("type", "minecraft:linear", "base", base, "per_level_above_first", perLevel);
        }
    }


}
