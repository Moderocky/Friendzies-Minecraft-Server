package mx.kenzie.survival.potion.effects;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.block.WeatheringCopper;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTransformEvent;

public class CorruptingMobEffect extends SimpleInstantaneousMobEffect implements CustomMobEffect {

    public CorruptingMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public CorruptingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, r, g, b);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity mob, int amplification) {
        LivingEntity result = switch (mob) {
            case Pig entity ->
                    entity.convertTo(EntityTypes.ZOMBIFIED_PIGLIN, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Villager entity ->
                    entity.convertTo(EntityTypes.WITCH, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Creeper entity -> {
                entity.setPowered(true);
                yield entity;
            }
            case MushroomCow entity -> {
                entity.setVariant(entity.getVariant() == MushroomCow.Variant.RED ? MushroomCow.Variant.BROWN : MushroomCow.Variant.RED);
                entity.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0F, 1.0F);
                yield entity;
            }

            case CopperGolem entity -> {
                entity.setWeatherState(WeatheringCopper.WeatherState.OXIDIZED);
                yield entity;
            }

            case Skeleton entity ->
                    entity.convertTo(EntityTypes.WITHER_SKELETON, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);

            case Horse entity ->
                    entity.convertTo(EntityTypes.SKELETON_HORSE, ConversionParams.single(entity, true, true), (ConversionParams.AfterConversion) (zombified) -> zombified.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 0)), EntityTransformEvent.TransformReason.INFECTION, CreatureSpawnEvent.SpawnReason.INFECTION);
            default -> mob;
        };
        return super.applyEffectTick(serverLevel, mob, amplification);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplification) {
        return super.shouldApplyEffectTickThisTick(tickCount, amplification);
    }

    @Override
    public void onEffectAdded(LivingEntity mob, int amplifier) {
        super.onEffectAdded(mob, amplifier);
    }
}
