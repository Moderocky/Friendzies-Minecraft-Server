package mx.kenzie.survival.pipe;

import io.papermc.paper.util.MCUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftRayTraceResult;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Marker;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Predicate;

public class Pipe {


    protected final transient List<Display> displays = new ArrayList<>();
    protected transient Marker source;
    protected Block input, output;
    protected BlockFace start, end;
    protected transient Location inputPoint, outputPoint;

    private static RayTraceResult trace(Location a, Location b, @Nullable Predicate<? super Block> predicate) {
        CraftWorld world = (CraftWorld) a.getWorld();

        Vec3 startPos = MCUtil.toVec3(a);
        Vec3 endPos = MCUtil.toVec3(b);

        HitResult hitResult = world.getHandle().clip(new ClipContext(startPos, endPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()), predicate);
        return CraftRayTraceResult.convertFromInternal(world.getHandle(), hitResult);
    }

    public void undraw() {
        this.displays.forEach(Entity::remove);
        this.displays.clear();
    }

    public void redraw() {
        this.undraw();
        this.draw();
    }

    public InventoryHolder getInputHolder() {
        return (InventoryHolder) input.getState();
    }

    public InventoryHolder getOutputHolder() {
        return (InventoryHolder) output.getState();
    }

    public void tick() {
        if (!(input.getState() instanceof InventoryHolder from)) return;
        if (!(output.getState() instanceof InventoryHolder to)) return;
        this.move(from.getInventory(), to.getInventory());
    }

    protected void move(Inventory from, Inventory to) {
        ListIterator<ItemStack> iterator = from.iterator();
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (item == null) continue;
            if (item.isEmpty()) continue;

        }
    }

    public boolean validateContainers() {
        return input.getState() instanceof InventoryHolder && output.getState() instanceof InventoryHolder;
    }

    public boolean validate() {
        this.inputPoint = null;
        this.outputPoint = null;
        Location a = this.inputPoint();
        Location b = this.outputPoint();
        if (a.distanceSquared(b) > PipeManager.MAX_PIPE_DISTANCE * PipeManager.MAX_PIPE_DISTANCE) return false;
        RayTraceResult trace = trace(a, b, null);
        if (trace.getHitBlock() != null) return false;
        return this.validateContainers();
    }

    protected void draw() {


    }

    protected Location inputPoint() {
        if (inputPoint != null) return inputPoint;
        return inputPoint = input.getLocation().add(0.5, 0.5, 0.5).add(start.getModX() * 0.5, start.getModY() * 0.5, start.getModZ() * 0.5);
    }

    protected Location outputPoint() {
        if (outputPoint != null) return outputPoint;
        return outputPoint = output.getLocation().add(0.5, 0.5, 0.5).add(end.getModX() * 0.5, end.getModY() * 0.5, end.getModZ() * 0.5);
    }

}
