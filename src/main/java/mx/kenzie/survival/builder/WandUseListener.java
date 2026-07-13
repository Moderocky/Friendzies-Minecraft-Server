package mx.kenzie.survival.builder;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.builder.action.*;
import mx.kenzie.survival.builder.task.Task;
import mx.kenzie.survival.utility.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;

import java.util.*;
import java.util.function.Predicate;

public class WandUseListener implements Listener {

    protected Inventory menu;

    @EventHandler
    public void event(PlayerInteractEvent event) {
        if (event.useInteractedBlock() == Event.Result.DENY) return;
        if (event.useItemInHand() == Event.Result.DENY) return;
        final ItemStack stack = event.getItem();
        if (stack == null) return;
        if (!Survival.builder.isWand(stack)) return;
        if (Survival.builder.wandAction(stack, event.getClickedBlock(), event.getAction()))
            event.setUseInteractedBlock(Event.Result.DENY);
        else return;
        event.getPlayer().sendActionBar(Component.text("Set position."));
        final Location location;
        if (event.getInteractionPoint() != null) location = event.getInteractionPoint();
        else {
            final Block block = event.getClickedBlock();
            final BlockFace face = event.getBlockFace();
            assert block != null;
            location = block.getLocation().toCenterLocation().add(face.getDirection().multiply(0.5));
        }
        if (location == null) return;
        location.getWorld().spawnParticle(Particle.FIREWORK, location, 5, 0.3, 0.3, 0.3, 0);
    }

    @EventHandler
    public void event(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;
        final ItemStack stack = event.getOffHandItem();
        if (stack == null) return;
        if (!Survival.builder.isWand(stack)) return;
        event.setCancelled(true);
        final Player player = event.getPlayer();
        this.wandMenu(player);
    }

    public boolean verify(Player player, ItemStack stack, int[][] positions) {
        if (stack == null) return false;
        if (!Survival.builder.isWand(stack)) return false;
        if (positions == null) {
            player.sendActionBar(Component.text("Set two positions first."));
            return false;
        }
        if (!Survival.builder.isValid(player, positions)) {
            player.sendActionBar(Component.text("Your selection is too large."));
            return false;
        }
        return true;
    }

    public Inventory getMenu() {
        if (menu != null) return menu;
        final Inventory inventory = Bukkit.createInventory(null, 36, Component.text("Building Tools"));
        inventory.setItem(0, new ItemBuilder(Material.FLINT)
                .setName("Set Area", NamedTextColor.YELLOW)
                .addLore("Replaces all blocks with provided materials.", NamedTextColor.GRAY)
                .addLore("Requires building materials.", NamedTextColor.GRAY)
                .addLore("Requires digging tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(1, new ItemBuilder(Material.BUCKET)
                .setName("Fill Area", NamedTextColor.YELLOW)
                .addLore("Fills in any gaps.", NamedTextColor.GRAY)
                .addLore("Requires building materials.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(2, new ItemBuilder(Material.IRON_PICKAXE)
                .setName("Break Area", NamedTextColor.YELLOW)
                .addLore("Destroys blocks.", NamedTextColor.GRAY)
                .addLore("Requires digging tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(3, new ItemBuilder(Material.GOLDEN_PICKAXE)
                .setName("Break Area Selectively", NamedTextColor.YELLOW)
                .addLore("Destroys certain blocks.", NamedTextColor.GRAY)
                .addLore("Requires digging tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(4, new ItemBuilder(Material.IRON_SHOVEL)
                .setName("Set Surface", NamedTextColor.YELLOW)
                .addLore("Replaces surface blocks.", NamedTextColor.GRAY)
                .addLore("Requires building materials.", NamedTextColor.GRAY)
                .addLore("Requires digging tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(5, new ItemBuilder(Material.GOLDEN_AXE)
                .setName("Replace Area", NamedTextColor.YELLOW)
                .addLore("Replaces certain blocks with provided materials.", NamedTextColor.GRAY)
                .addLore("Requires building materials.", NamedTextColor.GRAY)
                .addLore("Requires digging tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(6, new ItemBuilder(Material.MAP)
                .setName("Save Structure", NamedTextColor.YELLOW)
                .addLore("Save a structure to be rebuilt elsewhere.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(7, new ItemBuilder(Material.FILLED_MAP)
                .setName("Build Structure", NamedTextColor.YELLOW)
                .addLore("Builds a structure from the provided book.", NamedTextColor.GRAY)
                .addLore("Requires blueprint.", NamedTextColor.GRAY)
                .addLore("Requires building materials.", NamedTextColor.GRAY)
                .addLore("Requires digging tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(8, new ItemBuilder(Material.WOODEN_AXE)
                .setName("Strip Logs in Area", NamedTextColor.YELLOW)
                .addLore("Replaces logs with their stripped variants.", NamedTextColor.GRAY)
                .addLore("Requires cutting tools.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(9, new ItemBuilder(Material.CHEST_MINECART)
                .setName("Transfer Contents", NamedTextColor.YELLOW)
                .addLore("Transfers items from #1 to #2.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(10, new ItemBuilder(Material.REPEATER)
                .setName("Sort All Contents", NamedTextColor.YELLOW)
                .addLore("Sorts items between all containers.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(11, new ItemBuilder(Material.COMPARATOR)
                .setName("Sort Contents", NamedTextColor.YELLOW)
                .addLore("Sorts each container.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(12, new ItemBuilder(Material.SPYGLASS)
                .setName("Search Contents", NamedTextColor.YELLOW)
                .addLore("Searches all containers for an item.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
//        inventory.setItem(13, new ItemBuilder(Material.HOPPER)
//                .setName("Hopper Filter", NamedTextColor.YELLOW)
//                .addLore("Edits the filter of targeted hoppers.", NamedTextColor.GRAY)
//                .hideFlags()
//                .getItem());
        inventory.setItem(18 + 9, new ItemBuilder(Material.SHULKER_SHELL)
                .setName("Mark Storage", NamedTextColor.YELLOW)
                .addLore("Marks your selection as your storage area.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        inventory.setItem(35, new ItemBuilder(Material.BARRIER)
                .setName("Cancel Tasks", NamedTextColor.YELLOW)
                .addLore("Cancels currently-running tasks.", NamedTextColor.GRAY)
                .addLore("You may not recoup some resources.", NamedTextColor.GRAY)
                .hideFlags()
                .getItem());
        return menu = inventory;
    }

    public void wandMenu(Player player) {
        final Inventory inventory = this.getMenu();
        player.openInventory(inventory);
    }

    @EventHandler
    public void event(InventoryClickEvent event) {
        this.interact(event);
    }

    @EventHandler
    public void event(InventoryDragEvent event) {
        this.interact(event);
    }

    public void interact(InventoryInteractEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getInventory().equals(menu)) return;
        if (event instanceof InventoryClickEvent click) {
            if (click.getClickedInventory() == null) return;
            if (click.isShiftClick()) event.setCancelled(true);
            if (!click.getClickedInventory().equals(menu)) return;
            event.setCancelled(true);
            final ItemStack stack = player.getEquipment().getItemInMainHand();
            final int[][] positions = Survival.builder.wandPositions(stack);
            switch (click.getSlot()) {
                case 0 -> {
                    if (this.verify(player, stack, positions)) this.acceptMaterials(player, Action.SET, positions);
                    else player.closeInventory();
                }
                case 1 -> {
                    if (this.verify(player, stack, positions)) this.acceptMaterials(player, Action.FILL, positions);
                    else player.closeInventory();
                }
                case 2 -> {
                    if (this.verify(player, stack, positions)) this.acceptMaterials(player, Action.BREAK, positions);
                    else player.closeInventory();
                }
                case 3 -> {
                    if (this.verify(player, stack, positions)) {
                        final BoundingBox box = new BoundingBox(
                                positions[0][0], positions[0][1], positions[0][2],
                                positions[1][0], positions[1][1], positions[1][2]
                        );
                        final SelectorInventory inventory = new SelectorInventory(player, this.typesIn(box, player.getWorld()),
                                set -> {
                                    this.acceptMaterials(player, new BreakSelectiveAction(set), positions);
                                });
                        player.openInventory(inventory.getInventory());
                    } else player.closeInventory();
                }
                case 4 -> {
                    if (this.verify(player, stack, positions))
                        this.acceptMaterials(player, Action.SET_SURFACE, positions);
                    else player.closeInventory();
                }
                case 5 -> {
                    if (this.verify(player, stack, positions)) {
                        final BoundingBox box = new BoundingBox(
                                positions[0][0], positions[0][1], positions[0][2],
                                positions[1][0], positions[1][1], positions[1][2]
                        );
                        final SelectorInventory inventory = new SelectorInventory(player, this.typesIn(box, player.getWorld()),
                                set -> {
                                    this.acceptMaterials(player, new ReplaceAction(set), positions);
                                });
                        player.openInventory(inventory.getInventory());
                    } else player.closeInventory();
                }
                case 6 -> {
                    if (this.verify(player, stack, positions)) Action.SAVE.run(player, null, positions);
                    player.closeInventory();
                }
                case 7 -> {
                    if (this.verify(player, stack, positions))
                        this.acceptMaterials(player, Action.LOAD, positions);
                    else player.closeInventory();
                }
                case 8 -> {
                    if (this.verify(player, stack, positions)) {
                        final BoundingBox box = new BoundingBox(
                                positions[0][0], positions[0][1], positions[0][2],
                                positions[1][0], positions[1][1], positions[1][2]
                        );
                        final SelectorInventory inventory = new SelectorInventory(player, this.typesIn(box, player.getWorld(), StripAction::isStrippable),
                                set -> {
                                    this.acceptMaterials(player, new StripAction(set), positions);
                                });
                        player.openInventory(inventory.getInventory());
                    } else player.closeInventory();
                }
                case 9 -> {
                    if (this.verify(player, stack, positions)) Action.TRANSFER.run(player, null, positions);
                    player.closeInventory();
                }
                case 10 -> {
                    if (this.verify(player, stack, positions)) Action.SORT.run(player, null, positions);
                    player.closeInventory();
                }
                case 11 -> {
                    if (this.verify(player, stack, positions)) Action.SINGLE_SORT.run(player, null, positions);
                    player.closeInventory();
                }
                case 12 -> {
                    if (this.verify(player, stack, positions)) {
                        final BoundingBox box = new BoundingBox(
                                positions[0][0], positions[0][1], positions[0][2],
                                positions[1][0], positions[1][1], positions[1][2]
                        );
                        final WriteMenu menu = new WriteMenu(player, material -> new FindAction(material).run(player, null, positions));
                        menu.findMaterials(box);
                        player.closeInventory();
                        menu.open();
                    } else player.closeInventory();
                }
//                case 13 -> {
////                    if (this.verify(player, stack, positions)) Action.EDIT_FILTER.run(player, null, positions);
////                    else
//                    player.closeInventory();
//                }
                case 18 + 9 -> {
                    if (this.verify(player, stack, positions)) Action.MARK_STORAGE.run(player, null, positions);
                    player.closeInventory();
                }
                case 35 -> {
                    player.closeInventory();
                    for (Task task : Survival.builder.tasks) {
                        if (task.owner() == player) task.cancel();
                    }
                }
            }
        } else if (event instanceof InventoryDragEvent drag) {
            for (Integer slot : drag.getRawSlots()) {
                if (slot < menu.getSize()) event.setCancelled(true);
            }
        }
    }

    public void acceptMaterials(Player player, Action action, int[][] positions) {
        final BuildingInventory inventory = new BuildingInventory(action, player, positions);
        Bukkit.getPluginManager().registerEvents(inventory, Survival.plugin);
        player.openInventory(inventory.getInventory());
    }

    protected BlockData[] typesIn(BoundingBox box, World world) {
        return this.typesIn(box, world, _ -> true);
    }

    protected BlockData[] typesIn(BoundingBox box, World world, Predicate<Block> filter) {
        final Location start = new Location(world, box.getMinX(), box.getMinY(), box.getMinZ());
        final Set<BlockData> data = new LinkedHashSet<>();
        final int height = (int) box.getHeight() + 1, width = (int) box.getWidthX() + 1, length = (int) box.getWidthZ() + 1;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                for (int z = 0; z < length; z++) {
                    final Block block = start.clone().add(x, y, z).getBlock();
                    if (filter.test(block))
                        data.add(block.getBlockData());
                }
        final List<BlockData> list = new ArrayList<>(data);
        list.sort(Comparator.comparing(BlockData::getMaterial));
        return list.toArray(new BlockData[0]);
    }

}
