package mx.kenzie.survival;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import mx.kenzie.survival.attributes.AttributeManager;
import mx.kenzie.survival.enchanting.EnchantingRegistry;
import mx.kenzie.survival.potion.PotionManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class SurvivalBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        Survival.attributeManager = new AttributeManager();
        Survival.attributeManager.preSetup();

        Survival.potionManager = new PotionManager();
        Survival.potionManager.preSetup();

        Survival.enchantingRegistry = new EnchantingRegistry();
        Survival.enchantingRegistry.preSetup();

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(
                event -> {
                    try {
                        URI uri = EnchantingRegistry.dataLocation();
                        event.registrar().discoverPack(uri, "enchantments");
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));

//        context.getLifecycleManager().registerEventHandler(LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(
//                event -> {
//                    event.registrar().addToTag(TagKey.create(RegistryKey.ENCHANTMENT, "curse"), Survival.enchantingRegistry.getCurses());
//                }
//        ));

    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return PluginBootstrap.super.createPlugin(context);
    }
}
