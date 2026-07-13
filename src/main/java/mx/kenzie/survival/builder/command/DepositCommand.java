package mx.kenzie.survival.builder.command;

import mx.kenzie.centurion.ColorProfile;
import mx.kenzie.centurion.MinecraftCommand;
import mx.kenzie.centurion.TypedArgument;
import mx.kenzie.clockwork.collection.ClockList;
import mx.kenzie.survival.Survival;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collection;

import static mx.kenzie.centurion.Arguments.BOOLEAN;
import static mx.kenzie.centurion.Arguments.INTEGER;
import static mx.kenzie.centurion.CommandResult.LAPSE;
import static mx.kenzie.centurion.CommandResult.PASSED;

public class DepositCommand extends MinecraftCommand {
    static final ItemArgument ITEM = new ItemArgument();
    static final TypedArgument<Boolean> NEAT = BOOLEAN.described("Whether to add items to any available chest.").labelled("neatly").asOptional(),
            NEAR = BOOLEAN.described("Whether to use nearby containers or storage.").labelled("nearby").asOptional();

    public DepositCommand() {
        super("Deposit resources into your storage.");
        this.permission = null; // = "survival.command.deposit";
    }

    public static void deposit(ClockList<ItemStack> items, GetCommand.StorageArea area, Player player, boolean safe) {
        final DepositTask task = new DepositTask(area, player, items, safe);
        Survival.builder.runTask(task);

    }

    @Override
    public MinecraftBehaviour create() {
        final ColorProfile profile = this.getProfile();
        return command("deposit")
                .arg("that", NEAT, NEAR, (sender, arguments) -> { // box
                    if (!(sender instanceof Player player)) return LAPSE;
                    final GetCommand.StorageArea area = GetCommand.getStorageArea(player, arguments.get(1));
                    if (area == null) {
                        player.sendMessage(Component.text("You have no storage area.", profile.light()));
                        return PASSED;
                    }
                    int count = 0;
                    final ClockList<ItemStack> items = new ClockList<>(ItemStack.class, 27);
                    final Block block = player.getTargetBlockExact(5);
                    final Inventory inventory;
                    if (block == null || !(block.getState() instanceof BlockInventoryHolder holder)) {
                        player.sendMessage(Component.text("You must target a storage block.", profile.dark()));
                        return PASSED;
                    } else inventory = holder.getInventory();
                    for (ItemStack stack : inventory) {
                        if (stack == null) continue;
                        if (stack.getType() == Material.AIR) continue;
                        if (stack.getAmount() < 1) continue;
                        items.add(stack.clone());
                        count += stack.getAmount();
                    }
                    if (items.isEmpty()) return PASSED;
                    inventory.clear();
                    deposit(items, area, player, arguments.get(0));
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Depositing ", profile.dark()),
                            Component.text(count, profile.highlight()),
                            Component.text(" items into storage.", profile.dark())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("inventory", NEAT, NEAR, (sender, arguments) -> { // 27 container slots
                    if (!(sender instanceof Player player)) return LAPSE;
                    final GetCommand.StorageArea area = GetCommand.getStorageArea(player, arguments.get(1));
                    if (area == null) {
                        player.sendMessage(Component.text("You have no storage area.", profile.light()));
                        return PASSED;
                    }
                    int count = 0;
                    final ClockList<ItemStack> items = new ClockList<>(ItemStack.class, 27);
                    final PlayerInventory inventory = player.getInventory();
                    for (int index = 0; index < 27; index++) {
                        final ItemStack stack = inventory.getItem(index + 9);
                        if (stack == null) continue;
                        if (stack.getType() == Material.AIR) continue;
                        if (stack.getAmount() < 1) continue;
                        items.add(stack.clone());
                        count += stack.getAmount();
                        stack.setAmount(0);
                    }
                    if (items.isEmpty()) return PASSED;
                    deposit(items, area, player, arguments.get(0));
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Depositing ", profile.dark()),
                            Component.text(count, profile.highlight()),
                            Component.text(" items into storage.", profile.dark())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("everything", NEAT, NEAR, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final GetCommand.StorageArea area = GetCommand.getStorageArea(player, arguments.get(1));
                    if (area == null) {
                        player.sendMessage(Component.text("You have no storage area.", profile.light()));
                        return PASSED;
                    }
                    final ClockList<ItemStack> items = new ClockList<>(ItemStack.class, 45);
                    int count = 0;
                    for (ItemStack stack : player.getInventory()) {
                        if (stack == null) continue;
                        if (stack.getType() == Material.AIR) continue;
                        if (stack.getAmount() < 1) continue;
                        items.add(stack.clone());
                        count += stack.getAmount();
                    }
                    if (items.isEmpty()) return PASSED;
                    player.getInventory().clear();
                    deposit(items, area, player, arguments.get(0));
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Depositing ", profile.dark()),
                            Component.text(count, profile.highlight()),
                            Component.text(" items into storage.", profile.dark())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("this", NEAT, NEAR, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final GetCommand.StorageArea area = GetCommand.getStorageArea(player, arguments.get(1));
                    if (area == null) {
                        player.sendMessage(Component.text("You have no storage area.", profile.light()));
                        return PASSED;
                    }
                    final ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() == Material.AIR || item.getAmount() < 1) return PASSED;
                    final ItemStack copy = item.clone();
                    final Material material = copy.getType();
                    final int amount = copy.getAmount();
                    if (amount < 1) return PASSED;
                    item.setAmount(0);
                    final ClockList<ItemStack> items = new ClockList<>(copy);
                    deposit(items, area, player, arguments.get(0));
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Depositing ", profile.dark()),
                            Component.text(amount, profile.highlight()),
                            Component.text("× ", profile.pop()),
                            Component.translatable(material, profile.highlight()),
                            Component.text(" into storage.", profile.dark())
                    ));
                    //</editor-fold>
                    return PASSED;
                })
                .arg("item", ITEM.described("The type of item to deposit."), INTEGER.withLapse(1).asOptional()
                        .described("The number of items to deposit.").labelled("amount"), NEAT, NEAR, (sender, arguments) -> {
                    if (!(sender instanceof Player player)) return LAPSE;
                    final GetCommand.StorageArea area = GetCommand.getStorageArea(player, arguments.get(3));
                    if (area == null) {
                        player.sendMessage(Component.text("You have no storage area.", profile.light()));
                        return PASSED;
                    }
                    final Material material = arguments.get(0);
                    final int amount = arguments.get(1);
                    if (amount < 1) return PASSED;
                    final ClockList<ItemStack> items = this.getItems(player.getInventory(), material, amount);
                    deposit(items, area, player, arguments.get(2));
                    //<editor-fold desc="Message" defaultstate="collapsed">
                    player.sendMessage(Component.textOfChildren(
                            Component.text("Depositing ", profile.dark()),
                            Component.text(amount, profile.highlight()),
                            Component.text("× ", profile.pop()),
                            Component.translatable(material, profile.highlight()),
                            Component.text(" into storage.", profile.dark())
                    ));
                    //</editor-fold>
                    return PASSED;
                });
    }

    protected ClockList<ItemStack> getItems(PlayerInventory inventory, Material material, int amount) {
        if (amount < 1) return new ClockList<>();
        if (material.isAir()) return new ClockList<>();
        int count = 0;
        final ClockList<ItemStack> list = new ClockList<>(ItemStack.class);
        for (ItemStack stack : inventory) {
            if (stack == null) continue;
            if (stack.getType() != material) continue;
            if (stack.getAmount() < 1) continue;
            final int take = Math.min(stack.getAmount(), amount - count);
            count += take;
            final ItemStack taken = stack.clone();
            taken.setAmount(take);
            stack.setAmount(stack.getAmount() - take);
            list.add(taken);
            if (count >= amount) break;
        }
        return list;
    }

    static class DepositTask extends GetCommand.StorageTask {
        protected final boolean safe;
        protected int remaining;
        protected int initial = 0;

        public DepositTask(GetCommand.StorageArea area, Player player, ClockList<ItemStack> items, boolean safe) {
            super(area, player, items.size(), null);
            this.safe = safe;
            this.items = items;
            this.countUp();
            for (ItemStack item : items) initial += item.getAmount();
        }

        @Override
        public Component name() {
            return Component.text("Depositing Items");
        }

        protected void countUp() {
            this.remaining = 0;
            this.finished = (items == null || items.isEmpty());
            if (finished) return;
            for (ItemStack item : items) remaining += item.getAmount();
        }

        @Override
        public int remaining() {
            return remaining;
        }

        @Override
        protected boolean check(Inventory inventory) {
            if (safe) items = this.attemptAdd(inventory);
            else items = new ClockList<>(ItemStack.class, new ArrayList<>(inventory.addItem(items.toArray()).values()));
            this.countUp();
            return false;
        }

        protected ClockList<ItemStack> attemptAdd(Inventory inventory) {
            final ClockList<ItemStack> list = new ClockList<>(ItemStack.class, items.size());
            for (ItemStack item : items) {
                if (!inventory.contains(item.getType())) {
                    list.add(item);
                    continue;
                }
                final Collection<ItemStack> result = inventory.addItem(item).values();
                if (result.isEmpty()) continue;
                list.addAll(result);
            }
            return list;
        }

        @Override
        public void onEnd() {
            super.onEnd();
        }

        @Override
        protected void sendMessage(int count) {
            final ColorProfile profile = DEFAULT_PROFILE;
            //<editor-fold desc="Message" defaultstate="collapsed">
            this.player.sendMessage(Component.textOfChildren(
                    Component.text("Deposited ", profile.dark()),
                    Component.text(initial - remaining, profile.highlight()),
                    Component.text(" items into storage.", profile.dark())
            ));
            //</editor-fold>
        }

    }

}

