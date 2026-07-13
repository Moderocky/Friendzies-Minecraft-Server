package mx.kenzie.survival;

import io.papermc.paper.datapack.Datapack;
import mx.kenzie.clockwork.io.IOQueue;
import mx.kenzie.survival.bag.BagManager;
import mx.kenzie.survival.builder.BuildManager;
import mx.kenzie.survival.command.*;
import mx.kenzie.survival.enchanting.EnchantingManager;
import mx.kenzie.survival.enchanting.EnchantingRegistry;
import mx.kenzie.survival.gathering.GatheringManager;
import mx.kenzie.survival.geyser.DispenserListener;
import mx.kenzie.survival.listener.*;
import mx.kenzie.survival.mail.MailManager;
import mx.kenzie.survival.pipe.PipeManager;
import mx.kenzie.survival.potion.PotionManager;
import mx.kenzie.survival.robot.RobotManager;
import mx.kenzie.survival.tools.recipe.AnvilRecipe;
import mx.kenzie.survival.tools.recipe.ExtraRecipes;
import mx.kenzie.survival.tools.recipe.WoodcuttingRecipes;
import mx.kenzie.survival.utility.editor.BlockTagEditor;
import mx.kenzie.survival.utility.pack.Resources;
import mx.kenzie.survival.utility.web.WebServer;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Set;

public class Survival extends JavaPlugin implements Closeable {

    public static final IOQueue IO_QUEUE = new IOQueue();
    public static final Set<AnvilRecipe> anvilRecipes = new HashSet<>();
    public static Survival plugin;
    public static BuildManager builder;
    public static BagManager bagManager;
    public static MailManager mailManager;
    public static PotionManager potionManager;
    public static EnchantingManager enchantingManager;
    public static EnchantingRegistry enchantingRegistry;
    public static PipeManager pipeManager;
    public static RobotManager robotManager;
    public static GatheringManager gatheringManager;
    public static WebServer webServer;
    public static Resources resources;

    public static NamespacedKey key(String path) {
        if (plugin != null) return new NamespacedKey(plugin, path);
        return new NamespacedKey("server", path);
    }

    @Override
    public void onLoad() {
        Datapack pack = this.getServer().getDatapackManager().getPack(getPluginMeta().getName() + "/provided");
        if (pack != null) {
            if (pack.isEnabled()) {
                this.getLogger().info("The datapack loaded successfully!");
            } else {
                this.getLogger().warning("The datapack failed to load.");
            }
        }
    }

    @Override
    public void onEnable() {
        Survival.plugin = this;
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
        Bukkit.getPluginManager().registerEvents(new PingListener(), this);
        Bukkit.getPluginManager().registerEvents(new PortalListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnimalListener(), this);
        Bukkit.getPluginManager().registerEvents(new EndermanListener(), this);
        Bukkit.getPluginManager().registerEvents(new WaypointListener(), this);
        Bukkit.getPluginManager().registerEvents(new DispenserListener(), this);
        Bukkit.getPluginManager().registerEvents(new SilkBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new FrightenMonstersListener(), this);
        Survival.builder = new BuildManager();
        Survival.builder.setup();
        Survival.bagManager = new BagManager();
        Survival.bagManager.setup();
        Survival.mailManager = new MailManager();
        Survival.mailManager.setup();
        Survival.potionManager.setup();
        Survival.enchantingRegistry.setup();
        Survival.enchantingManager = new EnchantingManager();
        Survival.enchantingManager.setup();
        Survival.pipeManager = new PipeManager();
        Survival.pipeManager.setup();
        Survival.robotManager = new RobotManager();
        Survival.robotManager.setup();
        Survival.webServer = new WebServer(25566);
        Survival.webServer.accept();
        Survival.resources = new Resources();
        Survival.resources.setup();
        Survival.gatheringManager = new GatheringManager();
        Survival.gatheringManager.setup();

        new TeleportCommand().register(this);
        new CompassCommand().register(this);
        new RenameCommand().register(this);
        new WhereCommand().register(this);
        new HelpCommand().register(this);

        WoodcuttingRecipes.register();
        ExtraRecipes.register();
        BeRightBack.register();

        super.onEnable();


        try (BlockTagEditor editor = new BlockTagEditor(Tag.CLIMBABLE)) {
            editor.add(Blocks.IRON_CHAIN.builtInRegistryHolder());
        }
    }

    @Override
    public void onDisable() {
        this.close();
        super.onDisable();
        Survival.potionManager.close();
        Survival.webServer.close();
    }

    @Override
    public void close() {
        Survival.builder = null;
        Survival.plugin = null;
    }

}
