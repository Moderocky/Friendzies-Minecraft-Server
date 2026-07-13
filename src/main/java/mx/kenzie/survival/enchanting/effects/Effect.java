package mx.kenzie.survival.enchanting.effects;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NonNull;

public interface Effect<Type> extends Keyed {

    @NonNull NamespacedKey getKey();

    Type getValue();

}
