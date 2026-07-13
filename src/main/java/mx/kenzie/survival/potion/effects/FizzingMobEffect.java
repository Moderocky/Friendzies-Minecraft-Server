package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.cubemob.SulfurCube;

public class FizzingMobEffect extends SpawningMobEffect<SulfurCube> {

    public FizzingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, EntityTypes.SULFUR_CUBE, cube -> {
            cube.setSize(1, true);
            return true;
        }, random -> random.nextInt(1, 3), r, g, b);
    }


}
