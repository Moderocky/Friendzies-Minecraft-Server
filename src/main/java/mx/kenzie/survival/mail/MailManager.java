package mx.kenzie.survival.mail;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class MailManager {

    static final NamespacedKey LUGGAGE_KEY = NamespacedKey.minecraft("luggage_label");
    static final NamespacedKey MAILBOX = NamespacedKey.minecraft("mailbox"), MAILBOX_WORLD = NamespacedKey.minecraft("mailbox_world");
    public static ItemStack mailTag;

    public MailManager() {
        mailTag = this.createItem();
    }

    public static void checkMailboxLocation(Player player) {
        Location location = getMailboxLocation(player);
        if (location == null) return;
        Block block = location.getBlock();
        if (block.getState() instanceof Container) return;
        setMailboxLocation(player, null);
    }

    public static @Nullable Location getMailboxLocation(PersistentDataViewHolder player) {
        PersistentDataContainerView container = player.getPersistentDataContainer();
        if (!container.has(MAILBOX, PersistentDataType.INTEGER_ARRAY)) return null;
        if (!container.has(MAILBOX_WORLD, PersistentDataType.STRING)) return null;
        int[] ints = container.get(MAILBOX, PersistentDataType.INTEGER_ARRAY);
        assert ints != null && ints.length == 3;
        String name = container.get(MAILBOX_WORLD, PersistentDataType.STRING);
        if (name == null) return null;
        World world = Bukkit.getWorld(name);
        if (world == null) return null;
        return new Location(world, ints[0], ints[1], ints[2]);
    }

    public static void setMailboxLocation(PersistentDataHolder player, @Nullable Location location) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        if (location == null) {
            container.remove(MAILBOX_WORLD);
            container.remove(MAILBOX);
        } else {
            World world = location.getWorld();
            String name = world.getName();
            container.set(MAILBOX_WORLD, PersistentDataType.STRING, name);
            int[] ints = new int[3];
            ints[0] = location.getBlockX();
            ints[1] = location.getBlockY();
            ints[2] = location.getBlockZ();
            container.set(MAILBOX, PersistentDataType.INTEGER_ARRAY, ints);
        }
    }

    public void setup() {
        final NamespacedKey key = new NamespacedKey(Survival.plugin, "luggage_label");
        final ShapelessRecipe recipe = new ShapelessRecipe(key, mailTag);
        recipe.addIngredient(Material.STRING);
        recipe.addIngredient(Material.PAPER);
        recipe.addIngredient(Material.PAPER);
        recipe.setGroup("survival");
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);
        Bukkit.addRecipe(recipe);
        Bukkit.getPluginManager().registerEvents(new MailListener(this), Survival.plugin);
    }

    protected ItemStack createItem() {
        final ItemStack stack = new ItemStack(Material.NAME_TAG);
        final ItemMeta meta = stack.getItemMeta();
        meta.customName(Component.text("Luggage Label", NamedTextColor.LIGHT_PURPLE).decoration(TextDecoration.ITALIC, false));
        meta.setEnchantmentGlintOverride(true);
        meta.lore(List.of(
                Component.textOfChildren(Component.text("Send Mail ", NamedTextColor.GRAY), Component.keybind("key.use", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false),
                Component.textOfChildren(Component.text("Set Mailbox ", NamedTextColor.GRAY), Component.keybind("key.use", NamedTextColor.AQUA)).decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(LUGGAGE_KEY, PersistentDataType.INTEGER, 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public void selectMail(Player player) {
        MailMenu mailMenu = new MailMenu(player);
        this.withPlayerList(mailMenu::createFor);
    }

    public void depositMail(LivingEntity sender, MailTarget target, ItemStack mail) {
        if (target.box()) {
            Location location = target.location();
            Block block = location.getBlock();
            BlockState state = block.getState();
            if (state instanceof Container container) {
                HashMap<Integer, ItemStack> map = container.getInventory().addItem(mail);
                if (!map.isEmpty()) {
                    this.giveBack(sender, map.values().iterator().next());
                }
            }
        } else if (target.player() instanceof Player player && player.isOnline()) {
            Item item = player.getWorld().dropItem(player.getLocation(), mail);
            item.setPickupDelay(30);
            item.setGravity(false);
            item.setVelocity(new Vector(0, -0.0001, 0));
            item.setOwner(player.getUniqueId());
            item.customName(Component.text("Mail", NamedTextColor.LIGHT_PURPLE));
            item.setCustomNameVisible(true);
            item.setUnlimitedLifetime(true);
        } else {
            this.giveBack(sender, mail);
        }

    }

    protected void giveBack(LivingEntity sender, ItemStack mail) {
        sender.sendActionBar(Component.text("Mail undeliverable.").color(NamedTextColor.RED));
        sender.getWorld().dropItem(sender.getLocation(), mail);
    }

    public <Result> void withPlayerList(Function<OfflinePlayer[], Result> consumer) {
        Survival.IO_QUEUE.queue(() -> {
            @NotNull OfflinePlayer[] players = Bukkit.getOfflinePlayers();
            Bukkit.getScheduler().callSyncMethod(Survival.plugin, () -> consumer.apply(players));
        });
    }

    public boolean isMailBag(ItemStack mail) {
        return mail != null && mail.getItemMeta() instanceof BundleMeta meta
                && meta.getPersistentDataContainer().has(MAILBOX, PersistentDataType.INTEGER);
    }

    public boolean isMailTag(ItemStack mail) {
        return mail != null && mail.hasItemMeta() && mail.getItemMeta().getPersistentDataContainer().has(LUGGAGE_KEY, PersistentDataType.INTEGER);
    }

    public void sendMail(@NotNull HumanEntity sender, MailTarget target, ItemStack item) {
//        new ItemFetcher(Survival.plugin).fetch(item, ItemFetcher.Target.entity(sender), target.toTarget());
//        // todo allay
        this.depositMail(sender, target, item);
    }
}
