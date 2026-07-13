package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.InstantaneousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.Color;

public class StabiliserMobEffect extends InstantaneousMobEffect implements CustomMobEffect {

    public StabiliserMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, Color.fromRGB(r, g, b).asRGB());
    }

}
