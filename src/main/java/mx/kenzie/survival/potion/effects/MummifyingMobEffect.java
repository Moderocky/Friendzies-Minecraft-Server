package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.skeleton.Parched;

public class MummifyingMobEffect extends SpawningMobEffect<Parched> {

    public MummifyingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, EntityTypes.PARCHED, _ -> true, _ -> 1, r, g, b);
    }

}
