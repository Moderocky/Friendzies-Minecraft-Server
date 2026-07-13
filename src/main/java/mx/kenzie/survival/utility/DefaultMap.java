package mx.kenzie.survival.utility;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultMap<Key, Value> implements Map<Key, Value> {

    protected final Map<Key, Value> map;
    protected final Function<Key, Value> defaultFunction;

    public DefaultMap(Supplier<Map<Key, Value>> backingMapCreator, Function<Key, Value> defaultFunction) {
        this.map = backingMapCreator.get();
        this.defaultFunction = defaultFunction;
    }

    public DefaultMap() {
        this(HashMap::new, _ -> null);
    }

    public DefaultMap(Value defaultValue) {
        this(HashMap::new, _ -> defaultValue);
    }

    public DefaultMap(Supplier<Value> defaultValue) {
        this(HashMap::new, _ -> defaultValue.get());
    }

    public DefaultMap(Function<Key, Value> defaultValue) {
        this(HashMap::new, defaultValue);
    }

    public DefaultMap(Supplier<Map<Key, Value>> backingMapCreator, Value defaultValue) {
        this(backingMapCreator, _ -> defaultValue);
    }

    public DefaultMap(Map<Key, Value> defaultMap) {
        this(HashMap::new, defaultMap::get);
    }

    public DefaultMap(Map<Key, Value> defaultMap, Function<Key, Value> defaultFunction) {
        this(() -> defaultMap, defaultFunction);
    }

    public DefaultMap(Map<Key, Value> defaultMap, Value defaultValue) {
        this(() -> defaultMap, defaultValue);
    }

    public static <Key, Value> DefaultMap<Key, Value> emptyMap(Value defaultValue) {
        return new DefaultMap<>(Collections::emptyMap, defaultValue);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public Value get(Object key) {
        if (map.containsKey(key)) return map.get(key);
        return defaultFunction.apply((Key) key);
    }

    @Override
    public @Nullable Value put(Key key, Value value) {
        return map.put(key, value);
    }

    @Override
    public Value remove(Object key) {
        return map.remove(key);
    }

    @Override
    public void putAll(@NonNull Map<? extends Key, ? extends Value> m) {
        map.putAll(m);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public @NonNull Set<Key> keySet() {
        return map.keySet();
    }

    @Override
    public @NonNull Collection<Value> values() {
        return map.values();
    }

    @Override
    public @NonNull Set<Entry<Key, Value>> entrySet() {
        return map.entrySet();
    }

    @Override
    public String toString() {
        return map.toString();
    }
}
