package mx.kenzie.survival.enchanting.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class HoeEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Block block;
    private final ItemStack item;

    @ApiStatus.Internal
    public HoeEvent(@NotNull final Block block, Player player, ItemStack heldItem) {
        super(player);
        this.block = block;
        this.item = heldItem;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public Block getBlock() {
        return block;
    }

    public ItemStack getItem() {
        return item;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
