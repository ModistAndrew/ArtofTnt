package modist.artoftnt.common.entity;

import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.addition.InstabilityHelper;
import modist.artoftnt.core.explosion.ExplosionHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import javax.annotation.Nullable;
import java.util.List;

public class PrimedTntFrame extends AbstractHurtingProjectile {
    public final int tier;
    private static final EntityDataAccessor<CompoundTag> DATA_TNT_FRAME = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> DATA_LEFT_COUNT = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COOL_DOWN = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);

    private final TntFrameData data; //cache

    public PrimedTntFrame(EntityType<? extends PrimedTntFrame> p_32076_, Level p_32077_, int tier) {
        super(p_32076_, p_32077_);
        this.tier = tier;
        this.data = new TntFrameData(tier);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TNT_FRAME, new CompoundTag());
        this.entityData.define(DATA_FUSE, 0);
        this.entityData.define(DATA_LEFT_COUNT, 1);
        this.entityData.define(DATA_COOL_DOWN, 0);
    }

    public CompoundTag getDataTag() { //TODO command default tag shouldn't be new? just empty TntFrameData...
        return this.entityData.get(DATA_TNT_FRAME);
    }

    public void setDataTag(CompoundTag tag) {
        this.entityData.set(DATA_TNT_FRAME, tag == null ? new CompoundTag() : tag);
    }

    private void setDataTagAndInit(CompoundTag tag) {
        setDataTag(tag);
        setFuse((int) (data.getValue(AdditionType.FUSE)));
        setLeftCount((int) (data.getValue(AdditionType.EXPLOSION_COUNT)));
        setCoolDown(0); //may be overwritten when loaded
    }

    public int getWeight() {
        return (int) this.data.getWeight();
    }

    public void setFuse(int pFuse) {
        this.entityData.set(DATA_FUSE, pFuse);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE);
    }

    public void setLeftCount(int count) {
        this.entityData.set(DATA_LEFT_COUNT, count);
    }

    public int getLeftCount() {
        return this.entityData.get(DATA_LEFT_COUNT);
    }

    public void setCoolDown(int coolDown) {
        this.entityData.set(DATA_COOL_DOWN, coolDown);
    }

    public int getCoolDown() {
        return this.entityData.get(DATA_COOL_DOWN);
    }

    public PrimedTntFrame(CompoundTag tag, Level level, double x, double y, double z, @Nullable LivingEntity owner, int tier) {
        this(EntityLoader.PRIMED_TNT_FRAMES[tier].get(), level, tier);
        this.setPos(x + 0.5D, y, z + 0.5D); //offset
        double d0 = level.random.nextDouble() * (double) ((float) Math.PI * 2F);
        //this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setOwner(owner);
        setDataTagAndInit(tag);
    }

    @Override
    //both sides!
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_TNT_FRAME.equals(pKey)) {
            this.data.deserializeNBT(getDataTag());
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return super.getDimensions(pPose).scale(data.size);
    }

    @Override
    public void refreshDimensions() { //update bounding box
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.refreshDimensions();
        this.setPos(d0, d1, d2);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        float punch = data.getValue(AdditionType.TNT_PUNCH) - data.getValue(AdditionType.TNT_DRAW);
        if (punch != 0) {
            doPunch(punch);
        }
        //super.tick();

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D,
                    -0.04D * (1 - data.getValue(AdditionType.LIGHTNESS)),
                    0.0D));
        }

        HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        this.updateInWaterStateAndDoFluidPushing();

        int coolDown = this.getCoolDown() - 1;
        if (coolDown >= 0) {
            this.setCoolDown(coolDown);
        }

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            doExplosion();
        } else {
            if (this.level.isClientSide) {
                this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }


    private void doExplosion() {
        if (this.getCoolDown() > 0) {
            return;
        }
        if (!this.level.isClientSide) {
            this.explode();
        }
        int count = this.getLeftCount() - 1;
        this.setLeftCount(count);
        int coolDown = (int) (data.getValue(AdditionType.EXPLOSION_INTERVAL));
        this.setCoolDown(coolDown);
        if (count <= 0) {
            this.discard();
        }
    }

    private void doPunch(float punch) {
        float range = data.getValue(AdditionType.RANGE);
        List<Entity> list = this.level.getEntities(this,
                new AABB(this.position().add(-range, -range, -range), this.position().add(range, range, range)));
        for (Entity e : list) {
            float distancePercentage = (float) (Math.sqrt(e.distanceToSqr(this.position())) / range);
            if (distancePercentage <= 1F) {
                float p = Math.max(-distancePercentage, punch * (1 - distancePercentage)); //avoid draw too much
                e.setDeltaMovement(e.getDeltaMovement().add(e.position().subtract(this.position()).normalize().
                        multiply(p, p, p)));
            }
        }
    }

    protected void explode() {
        ExplosionHelper.explode(this.data.additions, this.level, this, this.getX(), this.getY(0.0625D), this.getZ(),
                4F, false, Explosion.BlockInteraction.DESTROY);
    }

    @Override
    public EntityType<?> getType() {
        return EntityLoader.PRIMED_TNT_FRAMES[tier].get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        pCompound.put("tntFrameData", getDataTag());
        pCompound.putInt("leftCount", getLeftCount());
        pCompound.putInt("coolDown", getCoolDown());
        pCompound.putInt("fuse", getFuse());
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        setDataTag(pCompound.getCompound("tntFrameData"));
        setLeftCount(pCompound.getInt("leftCount"));
        setCoolDown(pCompound.getInt("coolDown"));
        setFuse(pCompound.getInt("fuse"));
        super.readAdditionalSaveData(pCompound);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) { //TODO can hit entity
        if(this.data.getValue(AdditionType.INSTABILITY) >= InstabilityHelper.TNT_HIT_ENTITY_MIN_INSTABILITY) {
            this.doExplosion();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) { //TODO sometimes not available, combine with super.tick?
        if(this.data.getValue(AdditionType.INSTABILITY) >= InstabilityHelper.TNT_HIT_BLOCK_MIN_INSTABILITY) {
            this.doExplosion();
        }
        float elasticity = data.getValue(AdditionType.ELASTICITY);
        float stickiness = Math.max(0, 1 + elasticity - data.getValue(AdditionType.STICKINESS));
        switch (pResult.getDirection().getAxis()) {
            case X -> this.setDeltaMovement(this.getDeltaMovement().multiply(-elasticity, stickiness, stickiness));
            case Y -> this.setDeltaMovement(this.getDeltaMovement().multiply(stickiness, -elasticity, stickiness));
            case Z -> this.setDeltaMovement(this.getDeltaMovement().multiply(stickiness, stickiness, -elasticity));
        }
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        float velocity = this.data.getValue(AdditionType.VELOCITY) * pVelocity; //multiply self
        Vec3 vec3 = (new Vec3(pX, pY, pZ)).normalize().add(this.random.nextGaussian() * (double) 0.0075F * (double) pInaccuracy,
                this.random.nextGaussian() * (double) 0.0075F * (double) pInaccuracy,
                this.random.nextGaussian() * (double) 0.0075F * (double) pInaccuracy).scale((double) velocity);
        this.setDeltaMovement(vec3);
        double d0 = vec3.horizontalDistance();
        this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vec3.y, d0) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    public float getPickRadius() {
        return 0.0F;
    }

    public void defuse() {
        if (!this.level.isClientSide) {
            this.spawnAtLocation(TntFrameBlock.getDrop(true, data));
        }
        this.discard();
    }
}
