package mx.kenzie.survival.listener;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.action.BreakAction;
import mx.kenzie.survival.enchanting.EnchantingRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class TreeChopListener implements Listener {
    public static final Function<Integer, Integer> MAX_CHOP_AMOUNT = x -> (1 + x) * 40;
    private static final BlockFace[] BLOCK_FACES = new BlockFace[]{
            BlockFace.DOWN,
            BlockFace.UP,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };
    private final Set<Material> naturalLogs = new LinkedHashSet<>(Arrays.asList(
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.ACACIA_LOG,
            Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG,
            Material.CHERRY_LOG,
            Material.PALE_OAK_LOG,
            Material.WARPED_STEM,
            Material.CRIMSON_STEM,
            Material.MUSHROOM_STEM
    ));
    private final Set<Material> naturalLeaves = new LinkedHashSet<>(Arrays.asList(
            Material.OAK_LEAVES,
            Material.SPRUCE_LEAVES,
            Material.BIRCH_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.ACACIA_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES,
            Material.CHERRY_LEAVES,
            Material.PALE_OAK_LEAVES,
            Material.WARPED_WART_BLOCK,
            Material.NETHER_WART_BLOCK,
            Material.RED_MUSHROOM_BLOCK
    ));

    private static int getConnectedBlocks(Block block, Set<Block> results, Material material, List<Block> todo, int i) {
        for (BlockFace face : BLOCK_FACES) {
            Block b = block.getRelative(face);
            if (b.getType() == material) {
                if (results.add(b)) {
                    if (i < 1) return i;
                    i--;
                    todo.add(b);
                }
            }
        }
        return i;
    }

    @EventHandler
    public void harvest(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Material material = block.getType();
        if (!Tag.CROPS.isTagged(material) && !Tag.LEAVES.isTagged(material)) return;
        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.HARVESTER, item);
        if (level < 0) return;
        int maxChopAmount = MAX_CHOP_AMOUNT.apply(level);
        final List<Block> blocks = this.getVaguelyConnected(material, block.getLocation(), maxChopAmount / 4);
        for (Block point : blocks) {
            if (block.getBlockData() instanceof Ageable ageable) {
                if (ageable.getAge() < ageable.getMaximumAge()) continue;
            } else if (block.getBlockData() instanceof Leaves leaves) {
                if (leaves.isPersistent()) continue;
            }
            if (this.remainingDurability(item) < 2) break;
            if (point.getLocation().distanceSquared(block.getLocation()) > (Math.pow((level + 1) * 5, 2))) return;
            block.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, point.getLocation(), 8, point.getBlockData());
            if (!point.breakNaturally(item, true, true)) continue;
            item = item.damage(1, event.getPlayer());
        }
    }

    @EventHandler
    public void onEvent(BlockBreakEvent event) {
        final Block block = event.getBlock();
        final Material material = block.getType();
        if (!Tag.LOGS.isTagged(material) && material != Material.MUSHROOM_STEM) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!(item.getItemMeta() instanceof Damageable) || this.remainingDurability(item) < 2) return;
        int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.HARVESTER, item);
        if (level < 0) return;
        int maxChopAmount = MAX_CHOP_AMOUNT.apply(level);
        if (!this.isTree(block)) return;
        final boolean damages = true;
        final List<Block> blocks;
        if (material == Material.MUSHROOM_STEM) blocks = this.getMushroom(block.getLocation(), maxChopAmount);
        else if (material == Material.MANGROVE_LOG) {
            blocks = this.getVaguelyConnected(material, block.getLocation(), maxChopAmount);
            blocks.addAll(this.getVaguelyConnected(Material.MANGROVE_ROOTS, block.getLocation(), maxChopAmount));
        } else blocks = this.getVaguelyConnected(material, block.getLocation(), maxChopAmount);
        for (Block point : blocks) {
            if ((block.getY() > point.getY()) && block.getType() != Material.MANGROVE_ROOTS) continue;
//            if (point.getType() != material) continue; todo mushrooms :(
//            if (!Survival.isNatural(block)) continue;
            if (this.remainingDurability(item) < 2) break;
//            block.getWorld().spawnParticle(Particle.BLOCK_CRUMBLE, point.getLocation(), 12, point.getBlockData());
//            if (!BreakAction.BreakTask.modifyByEvent(point, state, player, list)) continue;
//            if (!point.breakNaturally(item, true, true)) continue;
////            Survival.setNatural(block);
//            item = item.damage(1, event.getPlayer());

            BlockState state = block.getState();
            List<ItemStack> list = new ArrayList<>(state.getDrops(item, player));
            if (!BreakAction.BreakTask.modifyByEvent(point, state, player, list)) continue;
            Location location = point.getLocation().add(0.5, 0.5, 0.5);
            World world = block.getWorld();
            world.spawnParticle(Particle.BLOCK_CRUMBLE, location, 6, point.getBlockData());
            point.setType(Material.AIR, true);
            item = item.damage(1, player);
            for (ItemStack stack : list) {
                world.dropItemNaturally(block.getLocation(), stack);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void gouge(BlockBreakEvent event) {
        final Block block = event.getBlock();
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        int level = Survival.enchantingRegistry.getLevel(EnchantingRegistry.GOUGE, item);
        if (level < 0) return;
        if (!block.isPreferredTool(item)) return;
        Set<Block> blocks = new HashSet<>();
        this.touching(block, level, blocks);
        if (blocks.size() < 2) return;
        blocks.remove(block);
        Location centre = block.getLocation().add(0.5, 0.5, 0.5);
        for (Block point : blocks) {
            if (this.remainingDurability(item) < 2) break;
            BlockState state = block.getState();
            List<ItemStack> list = new ArrayList<>(state.getDrops(item, player));
            if (!BreakAction.BreakTask.modifyByEvent(point, state, player, list)) continue;
            Location location = point.getLocation().add(0.5, 0.5, 0.5);
            World world = block.getWorld();
            world.spawnParticle(Particle.BLOCK_CRUMBLE, location, 6, point.getBlockData());
            point.setType(Material.AIR, true);
            item = item.damage(1, event.getPlayer());
            for (ItemStack stack : list) {
                world.dropItemNaturally(centre, stack);
            }
        }
    }

    private void touching(Block block, int level, Set<Block> blocks) {
        blocks.add(block);
        if (level < 1) return;
        Material type = block.getType();
        final BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);
            if (type != relative.getType()) continue;
            this.touching(relative, level - 1, blocks);
        }
    }

    private List<Block> getMushroom(Location location, int maxChopAmount) {
        final Set<Block> set = new HashSet<>(getVaguelyConnectedBlocks(location, Material.MUSHROOM_STEM, maxChopAmount));
        for (Block block : set.toArray(new Block[0])) {
            set.addAll(getVaguelyConnectedBlocks(block.getLocation(), Material.RED_MUSHROOM_BLOCK, maxChopAmount));
            set.addAll(getVaguelyConnectedBlocks(block.getLocation(), Material.BROWN_MUSHROOM_BLOCK, maxChopAmount));
        }
        return new ArrayList<>(set);
    }

    private int remainingDurability(ItemStack stack) {
        final int max = stack.getType().getMaxDurability();
        return max - (stack.getItemMeta() instanceof Damageable damageable ? damageable.getDamage() : 0);
    }

    private boolean isTree(@NotNull Block block) {
        if (naturalLogs.contains(block.getType())) {
            return doesTouchNaturalLeaf(getConnected(block.getType(), block.getLocation(), 128), getLeaf(block), getOtherLeaf(block));
        } else return block.getType() == Material.MUSHROOM_STEM;
    }

    private Material getOtherLeaf(Block block) {
        if (block.getType() == Material.MUSHROOM_STEM) return Material.BROWN_MUSHROOM_BLOCK;
        return null;
    }

    public Material getLeaf(@NotNull Block block) {
        if (naturalLogs.contains(block.getType())) {
            return naturalLeaves.toArray(new Material[0])[new ArrayList<>(naturalLogs).indexOf(block.getType())];
        }
        return null;
    }

    private boolean doesTouchNaturalLeaf(List<Block> blocks, Material material, Material other) {
        for (Block block : blocks) {
            for (BlockFace face : BLOCK_FACES) {
                Block b = block.getRelative(face);
                if (b.getType() == material || b.getType() == other) {
                    return !(b.getBlockData() instanceof Leaves leaves) || !leaves.isPersistent();
                }
            }
        }
        return false;
    }

    private List<Block> getConnected(Material material, Location location, int limit) {
        return new ArrayList<>(getConnectedBlocks(location, material, limit));
    }

    private List<Block> getVaguelyConnected(Material material, Location location, int limit) {
        return new ArrayList<>(getVaguelyConnectedBlocks(location, material, limit));
    }

    private Set<Block> getConnectedBlocks(Location location, Material material, int i) {
        Block block = location.getBlock();
        Set<Block> set = new HashSet<>();
        final LinkedList<Block> list = new LinkedList<>();
        list.add(block);
        while ((block = list.poll()) != null && i > 0) {
            i = getConnectedBlocks(block, set, material, list, i);
        }
        return set;
    }

    private int getVaguelyConnectedBlocks(Block block, Set<Block> results, Material material, List<Block> todo, int i) {
        for (Block b : this.getBlocksInRadius(block.getLocation(), 1)) {
            if (b.getType() == material) {
                if (results.add(b)) {
                    if (i < 1) return i;
                    i--;
                    todo.add(b);
                }
            }
        }
        return i;
    }

    private Set<Block> getVaguelyConnectedBlocks(Location location, Material material, int i) {
        Block block = location.getBlock();
        Set<Block> set = new HashSet<>();
        LinkedList<Block> list = new LinkedList<>();
        list.add(block);
        while ((block = list.poll()) != null && i > 0) {
            i = getVaguelyConnectedBlocks(block, set, material, list, i);
        }
        return set;
    }

    private List<Block> getBlocksInRadius(@NotNull Location location, double radius) {
        List<Block> blocks = new ArrayList<>();
        location = location.clone();
        for (double x = location.getX() - radius; x <= location.getX() + radius; x++) {
            for (double y = location.getY() - radius; y <= location.getY() + radius; y++) {
                for (double z = location.getZ() - radius; z <= location.getZ() + radius; z++) {
                    blocks.add(new Location(location.getWorld(), x, y, z).getBlock());
                }
            }
        }
        return blocks;
    }
}
