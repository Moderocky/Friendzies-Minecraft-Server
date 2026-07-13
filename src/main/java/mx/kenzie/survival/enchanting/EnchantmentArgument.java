package mx.kenzie.survival.enchanting;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import mx.kenzie.centurion.TypedArgument;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentArgument extends TypedArgument<Enchantment> {
    final Registry<org.bukkit.enchantments.Enchantment> registry;

    public EnchantmentArgument() {
        super(Enchantment.class);
        this.label = "enchantment";
        this.registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        this.possibilities = registry.stream().map(Enchantment::key).map(Key::asString).toArray(String[]::new);
    }

    @Override
    public boolean matches(String s) {
        try {
            NamespacedKey key = NamespacedKey.fromString(s);
            if (key == null) return false;
            return registry.get(key) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public Enchantment parse(String s) {
        NamespacedKey key = NamespacedKey.fromString(s);
        if (key == null) return null;
        return registry.get(key);
    }

}
