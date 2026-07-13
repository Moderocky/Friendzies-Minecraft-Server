package mx.kenzie.survival.builder;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.bag.BagManager;
import mx.kenzie.survival.builder.action.Action;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.Closeable;
import java.util.Map;

import static mx.kenzie.survival.Survival.bagManager;

public class BuildingInventory implements Listener, Closeable {

    public final Inventory inventory;
    public final Action action;
    public final Player owner;
    protected final int[][] positions;
    public int state;
    protected transient ItemStack bag;

    public BuildingInventory(Action action, Player owner, int[][] positions) {
        this.action = action;
        this.owner = owner;
        this.positions = positions;
        this.inventory = Bukkit.createInventory(null, 36, Component.text("Task Resources"));
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void unregister() {
        InventoryCloseEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void event(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        final Player player = (Player) event.getPlayer();
        switch (state) {
            case 0:
                this.checkHasBag();
                this.action.run(player, this, positions);
                this.state++;
                break;
            case 2:
                for (ItemStack stack : this.getInventory()) {
                    if (stack == null) continue;
                    this.giveBack(player, stack);
                }
                this.state++;
                this.unregister();
                break;
        }
    }

    protected void checkHasBag() {
        BagManager bagManager = Survival.bagManager;
        for (ItemStack stack : this.inventory) {
            if (bagManager.isBag(stack)) {
                bag = stack;
                break;
            }
        }
    }

    public void giveBack(Player player, ItemStack stack) {
        final Map<Integer, ItemStack> result = player.getInventory().addItem(stack);
        for (ItemStack value : result.values()) player.getWorld().dropItem(player.getLocation(), value);
    }

    public void close() {
        if (owner == null || inventory.isEmpty()) {
            this.unregister();
            return;
        }
        this.state = 2;
        if (this.owner.openInventory(inventory) == null) {
            for (ItemStack stack : this.getInventory()) {
                if (stack == null) continue;
                this.giveBack(owner, stack);
            }
            this.inventory.clear();
        }
    }

    public boolean store(Iterable<ItemStack> stacks) {
        boolean held = true;
        for (ItemStack stack : stacks) {
            if (bag != null) bagManager.addItems(bag, stack);
            else {
                final Map<Integer, ItemStack> map = this.inventory.addItem(stack);
                if (map.isEmpty()) continue;
                for (ItemStack value : map.values()) this.giveBack(owner, value);
                held = false;
            }
        }
        return held;
    }
}
