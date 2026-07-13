package mx.kenzie.survival.listener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.jetbrains.annotations.NotNull;

public class SilkBreakListener implements Listener {

    private final boolean silkBreak = true;

    @EventHandler(ignoreCancelled = true)
    public void mobSpawn(SpawnerSpawnEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        living.registerAttribute(Attribute.SCALE);
        living.setPersistent(false);
        AttributeInstance attribute = living.getAttribute(Attribute.SCALE);
        if (attribute == null) return;
        attribute.addModifier(new AttributeModifier(NamespacedKey.minecraft("spawner_mob"), -0.2, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
    }

    @EventHandler(ignoreCancelled = true)
    public void event(BlockPlaceEvent event) {
        if (!silkBreak) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
        if (event.getBlock().getType() != Material.SPAWNER) return;
        this.cloneData(event.getItemInHand(), event.getBlock());
    }

    @EventHandler(ignoreCancelled = true)
    public void event(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        if (player.getGameMode() != GameMode.SURVIVAL) return;
        final Block block = event.getBlock();
        final Material material = block.getType();
        final ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (material == Material.SPAWNER && silkBreak && itemStack.getType() == Material.NETHERITE_PICKAXE) {
            if (itemStack.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                event.setDropItems(true);
                event.setExpToDrop(0);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void event(BlockDropItemEvent event) {
        BlockState state = event.getBlockState();
        if (state instanceof CreatureSpawner spawner) {
            if (!silkBreak || event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;
            final ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.NETHERITE_PICKAXE) return;
            if (!itemStack.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) return;
            final ItemStack drop = new ItemStack(Material.SPAWNER);
            if (drop.getItemMeta() instanceof BlockStateMeta meta) {
                meta.setBlockState(spawner);
                drop.setItemMeta(meta);
            }
            Location location = event.getBlock().getLocation();
            Item item = location.getWorld().dropItemNaturally(location.add(0.5, 0.5, 0.5), drop);
            event.getItems().add(item);

        }
    }

    private void cloneData(@NotNull ItemStack item, @NotNull Block block) {
        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            BlockState replacement = meta.getBlockState();
            replacement.copy(block.getLocation()).update(true, false);
        }
    }

    private ItemStack cloneData(@NotNull Block block, @NotNull ItemStack item) {
        BlockState state = block.getState();
        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            meta.setBlockState(state);
            item.setItemMeta(meta);
        }
        return item;
    }

}
