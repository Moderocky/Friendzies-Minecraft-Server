package mx.kenzie.survival.potion.effects;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;

import java.util.Optional;

public class CrawlingMobEffect extends SpawningMobEffect<CaveSpider> {

    public CrawlingMobEffect(MobEffectCategory category, int r, int g, int b) {
        super(category, EntityTypes.CAVE_SPIDER, spider -> {
            CraftLivingEntity living = spider.getBukkitLivingEntity();
            living.registerAttribute(Attribute.SCALE);
            living.registerAttribute(Attribute.MOVEMENT_SPEED);
            Optional.ofNullable(living.getAttribute(Attribute.SCALE)).ifPresent(attribute -> attribute.addModifier(new AttributeModifier(KEY, -0.47, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
            Optional.ofNullable(living.getAttribute(Attribute.MOVEMENT_SPEED)).ifPresent(attribute -> attribute.addModifier(new AttributeModifier(KEY, -0.68, AttributeModifier.Operation.MULTIPLY_SCALAR_1)));
            return true;
        }, random -> random.nextInt(1, 3), r, g, b);
    }


}
