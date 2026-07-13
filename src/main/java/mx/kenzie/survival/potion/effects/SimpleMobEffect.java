package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.bukkit.Color;

public class SimpleMobEffect extends MobEffect implements CustomMobEffect {
    public SimpleMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public SimpleMobEffect(MobEffectCategory category, int r, int g, int b) {
        this(category, Color.fromRGB(r, g, b).asRGB());
    }

}
