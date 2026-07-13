package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.Color;

public abstract class SimpleInstantaneousMobEffect extends InstantaneousMobEffect implements CustomMobEffect {
    public SimpleInstantaneousMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public SimpleInstantaneousMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, Color.fromRGB(r, g, b).asRGB());
    }
}
