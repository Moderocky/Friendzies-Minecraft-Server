package mx.kenzie.survival.potion.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;


public class DefendingArrow extends Arrow {
    public static final int DEGREE_STEP = 8;
    private static final Vector AXIS = new Vector(0, 1, 0);
    public boolean rotating = true;    public Vector original = new Vector(1, 0, 0), current = original;

    public DefendingArrow(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.persist = false;
    }

    public DefendingArrow(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(level, x, y, z, pickupItemStack, firedFromWeapon);
        this.setNoGravity(true);
        this.persist = false;
    }

    public DefendingArrow(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(level, owner, pickupItemStack, firedFromWeapon);
        this.setNoGravity(true);
        this.persist = false;
    }

    public void fireAt(LivingEntity entity) {
        if (!rotating) return;
        this.rotating = false;
        this.setNoGravity(false);
        this.performRangedAttack(entity, 1.8F);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        if (rotating) {
            Entity owner1 = this.getOwner();
            if (owner1 != null && entity.is(owner1)) return false;
            if (entity.getType().isAllowedInPeaceful()) return false;
        }
        return super.canCollideWith(entity);
    }

    public void performRangedAttack(LivingEntity target, float power) {
        AbstractArrow arrow = this;
        double xd = target.getX() - this.getX();
        double yd = target.getY(0.3333333333333333) - arrow.getY();
        double zd = target.getZ() - this.getZ();
        double distanceToTarget = Math.sqrt(xd * xd + zd * zd);
        this.shoot(xd, yd + distanceToTarget * (double) 0.2F, zd, power, (float) (2));

        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    @Override
    public void tick() {
        super.tick();
        this.tickMovement();
    }

    public void tickMovement() {
        if (!this.isAlive() || !rotating) return;
        Entity owner = this.getOwner();
        if (owner == null || owner.level() != this.level()) {
            this.remove(RemovalReason.CHANGED_DIMENSION);
            return;
        }

        this.current.rotateAroundAxis(AXIS, Math.toRadians(DEGREE_STEP));
        final Vec3 aim = new Vec3(owner.getX() + current.getX(), owner.getY() + current.getY(), owner.getZ() + current.getZ());
        double distanceToSqr = this.distanceToSqr(aim);
        if (distanceToSqr > 32 * 32) {
            this.remove(RemovalReason.DISCARDED);
            return;
        }
        if (distanceToSqr < 7) this.moveOrInterpolateTo(aim);
        else {
            final Vec3 us = new Vec3(this.getX(), this.getY(), this.getZ());
            final Vec3 step = us.add(aim.subtract(us).normalize());
            this.moveOrInterpolateTo(step);
        }
    }


}
