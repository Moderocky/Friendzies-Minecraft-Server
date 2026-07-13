package mx.kenzie.survival.potion;

import io.papermc.paper.persistence.PersistentDataContainerView;
import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotionBases {
    public static final PotionBase MILK = register("milk", "Milk", Color.WHITE);


    private static PotionBase register(String id, String name, PotionEffect... effects) {
        return register(id, name, null, effects);
    }

    private static PotionBase register(String id, String name, Color color, PotionEffect... effects) {
        return register(id, Component.text(name), color, effects);
    }

    private static PotionBase register(String id, Component name, Color color, PotionEffect... effects) {
        return new PotionBase(id, name, color, effects);
    }

    public record PotionBase(String id, Component name, Color color, PotionEffect... effects) implements Keyed {
        private static final NamespacedKey POTION_BASE_KEY = Survival.key("potion_base");

        @Override
        public @NotNull NamespacedKey getKey() {
            return Survival.key(id);
        }

        public boolean is(@Nullable ItemMeta meta) {
            return meta != null && this.is(meta.getPersistentDataContainer());
        }

        public boolean is(@Nullable ItemStack stack) {
            return stack != null && this.is(stack.getPersistentDataContainer());
        }

        private boolean is(PersistentDataContainerView view) {
            return view.has(POTION_BASE_KEY, PersistentDataType.STRING)
                    && id.equals(view.get(POTION_BASE_KEY, PersistentDataType.STRING));
        }

        public void apply(ItemMeta meta) {
            meta.getPersistentDataContainer().set(POTION_BASE_KEY, PersistentDataType.STRING, id);
            if (meta instanceof PotionMeta potion) {
                if (color != null)
                    potion.setColor(color);
                if (effects != null) {
                    for (PotionEffect effect : effects) {
                        potion.addCustomEffect(effect, false);
                    }
                }
            }
        }

        public void remove(ItemMeta meta) {
            meta.getPersistentDataContainer().remove(POTION_BASE_KEY);
            if (meta instanceof PotionMeta potion) {
                if (color != null)
                    potion.setColor(null);
                if (effects != null) {
                    for (PotionEffect effect : effects) {
                        potion.removeCustomEffect(effect.getType());
                    }
                }
            }

        }

        public void apply(ItemStack stack) {
            stack.editMeta(this::apply);
        }

        public ItemStack asCopy(ItemStack stack) {
            ItemStack clone = stack.clone();
            this.apply(clone);
            return clone;
        }

    }

}
