package mx.kenzie.survival.listener;

import mx.kenzie.survival.Survival;
import mx.kenzie.survival.tools.recipe.AnvilRecipe;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.metadata.FixedMetadataValue;

import static org.bukkit.Material.AIR;

/**
 * @author Moderocky
 * @version 1.0.0
 */
public class AnvilListener implements Listener {

    protected AnvilRecipe getRecipe(ItemStack s1, ItemStack s2) {
        for (AnvilRecipe anvilRecipe : Survival.anvilRecipes) {
            if (anvilRecipe.matches(s1, s2)) {
                return anvilRecipe;
            }
        }
        return null;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        HumanEntity[] humans = event.getViewers().toArray(new HumanEntity[0]);
        AnvilInventory inventory = event.getInventory();
        AnvilView view = event.getView();
        ItemStack s1 = inventory.getFirstItem();
        ItemStack s2 = inventory.getSecondItem();
        ItemStack result;
        AnvilRecipe anvilRecipe = this.getRecipe(s1, s2);
        if (anvilRecipe == null) return;
        result = anvilRecipe.getResult(clone(s1), clone(s2))[2];
        event.setResult(result);
//                inventory.setRepairCost(anvilRecipe.getRepairCost(s1, s2));
        view.setRepairCost(anvilRecipe.getRepairCost(s1, s2));
        for (HumanEntity human : humans) {
            human.setMetadata("custom_anvil", new FixedMetadataValue(Survival.plugin, true));
            human.setMetadata("custom_anvil_cost", new FixedMetadataValue(Survival.plugin, anvilRecipe.getRepairCost(s1, s2)));
        }
//        Survival.tools.fixEnchantmentLore(event.getResult());
    }

    private ItemStack clone(ItemStack item) {
        if (item == null) return null;
        return item.clone();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAnvilClick(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getClickedInventory() instanceof AnvilInventory inventory)) return;
        if (event.getSlot() != 2) return;
        final Player player = (Player) event.getWhoClicked();
        if (!player.hasMetadata("custom_anvil")) return;
        int cost = player.getMetadata("custom_anvil_cost").get(0).asInt();
        if (player.getLevel() < cost && player.getGameMode() != GameMode.CREATIVE) return;
        final ItemStack s1 = inventory.getFirstItem();
        final ItemStack s2 = inventory.getSecondItem();
        AnvilRecipe recipe = this.getRecipe(s1, s2);
        if (recipe == null) return;
        final ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() == AIR) return;
        int amount = result.getAmount();
//        Survival.tools.fixEnchantmentLore(result);
        if (event.getClick() == ClickType.SHIFT_LEFT) {
            if (!this.canHold(player, result) || this.canHoldAmount(player, result) < amount) return;
            player.getInventory().addItem(result.clone());
        } else if (event.getClick() == ClickType.NUMBER_KEY) {
            return;
        } else {
            if (player.getItemOnCursor().getType() != AIR) return;
            player.setItemOnCursor(result.clone());
        }
        final ItemStack[] itemStacks = recipe.getResult(s1, s2);
        event.setCancelled(true);
//        Survival.tools.fixEnchantmentLore(itemStacks[0]);
//        Survival.tools.fixEnchantmentLore(itemStacks[1]);
        inventory.setItem(0, itemStacks[0]);
        inventory.setItem(1, itemStacks[1]);
        player.setLevel(Math.max(player.getLevel() - cost, 0));
        inventory.setItem(2, new ItemStack(AIR));
        recipe.postCompletion(player, s1, s2, itemStacks);
        player.removeMetadata("custom_anvil", Survival.plugin);
        player.removeMetadata("custom_anvil_cost", Survival.plugin);
    }

    protected boolean canHold(Player player, ItemStack bukkitStack) {
        return this.canHold(player.getInventory(), bukkitStack) > 0;
    }

    protected int canHoldAmount(Player player, ItemStack bukkitStack) {
        return this.canHold(player.getInventory(), bukkitStack);
    }

    protected int canHold(org.bukkit.inventory.Inventory inventory, ItemStack itemstack) {
        int remains = itemstack.getAmount();

        for (ItemStack itemstack1 : inventory.getStorageContents()) {
            if (itemstack1 == null || itemstack1.isEmpty()) {
                return itemstack.getAmount();
            }

            if (this.hasRemainingSpaceForItem(inventory, itemstack1, itemstack)) {
                remains -= (Math.min(itemstack1.getMaxStackSize(), inventory.getMaxStackSize())) - itemstack1.getAmount();
            }

            if (remains <= 0) {
                return itemstack.getAmount();
            }
        }

        if (remains <= 0) {
            return itemstack.getAmount();
        } else {
            return itemstack.getAmount() - remains;
        }
    }

    private boolean hasRemainingSpaceForItem(Inventory inventory, ItemStack existingStack, ItemStack stack) {
        return !existingStack.isEmpty()
                && (existingStack.getMaxStackSize() > 1)
                && existingStack.getAmount() < existingStack.getMaxStackSize()
                && existingStack.getAmount() < inventory.getMaxStackSize()
                && existingStack.isSimilar(stack);
    }

}
