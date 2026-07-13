package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffect;
import org.bukkit.craftbukkit.potion.CraftPotionEffectType;
import org.bukkit.potion.PotionEffectType;

public interface CustomMobEffect {

    default PotionEffectType createPotionEffect() {
        return CraftPotionEffectType.minecraftToBukkit((MobEffect) this);
    }

}
