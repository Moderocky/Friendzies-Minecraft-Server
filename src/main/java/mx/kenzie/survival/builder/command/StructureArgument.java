package mx.kenzie.survival.builder.command;

import mx.kenzie.centurion.TypedArgument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class StructureArgument extends TypedArgument<Structure> {
    private int lastHash;
    private Structure lastValue;

    public StructureArgument() {
        super(Structure.class);
    }

    @Override
    public boolean matches(String s) {
        this.lastHash = s.hashCode();
        try {
            final NamespacedKey key = NamespacedKey.fromString(s.trim());
            if (key == null) return false;
            this.lastValue = Bukkit.getStructureManager().getStructure(key);
            return lastValue != null;
        } catch (Throwable ex) {
            return false;
        }
    }

    @Override
    public Structure parse(String s) {
        if (lastHash == s.hashCode()) return lastValue;
        try {
            final NamespacedKey key = NamespacedKey.fromString(s.trim());
            if (key == null) return null;
            return lastValue = Bukkit.getStructureManager().getStructure(key);
        } catch (Throwable ex) {
            return null;
        }
    }

    @Override
    public String[] possibilities() {
        final Map<NamespacedKey, Structure> map = Bukkit.getStructureManager().getStructures();
        final List<String> list = new ArrayList<>(map.size());
        for (NamespacedKey key : map.keySet()) list.add(key.toString());
        return list.toArray(new String[0]);
    }

}
