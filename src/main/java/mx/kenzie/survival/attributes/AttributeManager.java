package mx.kenzie.survival.attributes;

import mx.kenzie.survival.Survival;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.bukkit.Bukkit;
import org.bukkit.Registry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;

import static net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS;

public class AttributeManager {

    public static final org.bukkit.attribute.Attribute TESTING_ATTRIBUTE = new CustomAttribute("testing_attribute");

    protected void registerAttributes() throws InvocationTargetException, IllegalAccessException {
        this.register(TESTING_ATTRIBUTE, ranged(0.5, 0, 5).setSentiment(Attribute.Sentiment.NEGATIVE).setSyncable(false));
    }


    private static Method register;
    private static Collection<Attribute> internalAttributes = new HashSet<>();


    public void setup() {
        this.resolveBukkitHandles();
    }

    public void preSetup() {
        try {
            register = Attributes.class.getDeclaredMethod("register", String.class, Attribute.class);
            register.setAccessible(true);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to access MobEffects register", e);
        }

        try {
            this.registerAttributes();
        } catch (Exception e) {
            Survival.plugin.getLogger().log(Level.SEVERE, "Unable to register attributes", e);
        }

    }

    private void resolveBukkitHandles() {
        for (CustomAttribute attribute : toReify) {
            attribute.fulfil(Registry.ATTRIBUTE.getOrThrow(attribute.getKey()));
        }
    }

    private Collection<CustomAttribute> toReify = new ArrayList<>();

    private void register(org.bukkit.attribute.Attribute attribute, AttributeCreatorFunctor creator) throws InvocationTargetException, IllegalAccessException {
        if (!(attribute instanceof CustomAttribute custom))
            throw new IllegalArgumentException("Attribute is not a CustomAttribute");
        custom.instance = creator.apply(custom.id);
        //noinspection unchecked
        custom.internal = (net.minecraft.core.Holder<Attribute>) register.invoke(null, custom.id, custom.instance);
        this.toReify.add(custom);
        internalAttributes.add(custom.instance);
    }


    protected static AttributeCreatorFunctor ranged(double defaultValue, double minValue, double maxValue) {
        return (id) -> new RangedAttribute("attribute.name." + id, defaultValue, minValue, maxValue);
    }

    private static boolean isCustom(ItemAttributeModifiers.Entry entry) {
        return internalAttributes.contains(entry.attribute().value());
    }

    public static ItemStack modifyForSerialisation(ItemStack item) {
        ItemAttributeModifiers modifiers = item.getComponents().get(ATTRIBUTE_MODIFIERS);
        if (modifiers == null) return item;
        List<ItemAttributeModifiers.Entry> list = new ArrayList<>(modifiers.modifiers());
        if (list.removeIf(AttributeManager::isCustom)) {
            item = item.copy();
            item.set(ATTRIBUTE_MODIFIERS, new ItemAttributeModifiers(list));
        }
        return item;
    }

    protected interface AttributeCreatorFunctor extends Function<String, Attribute> {

        default AttributeCreatorFunctor setSyncable(boolean syncable) {
            return (id) -> this.apply(id).setSyncable(syncable);
        }

        default AttributeCreatorFunctor setSentiment(Attribute.Sentiment sentiment) {
            return (id) -> this.apply(id).setSentiment(sentiment);
        }

    }

}
