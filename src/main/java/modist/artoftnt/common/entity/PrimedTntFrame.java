package modist.artoftnt.common.entity;

import com.sun.jna.platform.win32.WinUser;
import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.ExplosionHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
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
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class PrimedTntFrame extends AbstractHurtingProjectile {
    public final int tier;
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<CompoundTag> DATA_TNT_FRAME = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.FLOAT);
    private final TntFrameData data;

    public PrimedTntFrame(EntityType<? extends PrimedTntFrame> p_32076_, Level p_32077_, int tier) {
        super(p_32076_, p_32077_);
        this.tier = tier;
        this.data = new TntFrameData(tier);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_TNT_FRAME, new CompoundTag());
        this.entityData.define(DATA_FUSE, 0);
        this.entityData.define(DATA_SIZE, 1F);
    }

    public CompoundTag getDataTag() { //TODO command default tag shouldn't be new? just empty TntFrameData...
        return this.entityData.get(DATA_TNT_FRAME);
    }

    private void setDataTag(CompoundTag tag) {
        this.entityData.set(DATA_TNT_FRAME, tag == null ? new CompoundTag() : tag);
        this.data.deserializeNBT(tag);
        setFuse((int) (data.getValue(AdditionType.FUSE) + data.getValue(AdditionType.LINGERING)));
        setSize(data.size);
        //this.refreshDimensions();
    }

    public int getWeight(){
        return (int)this.data.getWeight();
    }

    public void setFuse(int pFuse) {
        this.entityData.set(DATA_FUSE, pFuse);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE);
    }

    public void setSize(float size) {
        this.entityData.set(DATA_SIZE, size);
    }

    public float getSize() {
        return this.entityData.get(DATA_SIZE);
    }

    private int getLingeringFuse() {
        return (int)data.getValue(AdditionType.LINGERING);
    }

    private float getLightness() {
        return data.getValue(AdditionType.LIGHTNESS);
    }

    public PrimedTntFrame(CompoundTag tag, Level level, double x, double y, double z, @Nullable LivingEntity owner, int tier) {
        this(EntityLoader.PRIMED_TNT_FRAMES[tier].get(), level, tier);
        this.setPos(x+0.5D, y, z+0.5D); //offset
        double d0 = level.random.nextDouble() * (double) ((float) Math.PI * 2F);
        //this.setDeltaMovement(-Math.sin(d0) * 0.02D, 0.2F, -Math.cos(d0) * 0.02D);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.setOwner(owner);
        setDataTag(tag);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        //TODO update data
        if (DATA_SIZE.equals(pKey)) {
            this.refreshDimensions();
        }
        if(DATA_TNT_FRAME.equals(pKey)){
            this.data.deserializeNBT(getDataTag());
        }
        super.onSyncedDataUpdated(pKey);
    }

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return super.getDimensions(pPose).scale(getSize());
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
        //super.tick();
        HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }

        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D,
                    -0.04D * (1-getLightness()),
                    0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98D));
        if (this.onGround) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D, -0.5D, 0.7D));
        }

        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= getLingeringFuse() && i % 5 == 0) { //TODO
            if (!this.level.isClientSide) {
                this.explode();
            }
        }
        if (i <= 0) {
            if (!this.level.isClientSide) {
                this.explode();
            }
            this.discard();
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level.isClientSide) {
                this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5D, this.getZ(), 0.0D, 0.0D, 0.0D);
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
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        setDataTag(pCompound.getCompound("tntFrameData"));
        super.readAdditionalSaveData(pCompound);
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (!this.level.isClientSide) {
            this.explode();
        }
        this.discard();
    }

    /*@Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level.isClientSide) {
            this.explode();
        }
        this.discard();
    }*/

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        float velocity = this.data.getValue(AdditionType.VELOCITY)*pVelocity; //multiply self
        Vec3 vec3 = (new Vec3(pX, pY, pZ)).normalize().add(this.random.nextGaussian() * (double)0.0075F * (double)pInaccuracy,
                this.random.nextGaussian() * (double)0.0075F * (double)pInaccuracy,
                this.random.nextGaussian() * (double)0.0075F * (double)pInaccuracy).scale((double)velocity);
        this.setDeltaMovement(vec3);
        double d0 = vec3.horizontalDistance();
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, d0) * (double)(180F / (float)Math.PI)));
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
        if(!this.level.isClientSide){
            this.spawnAtLocation(TntFrameBlock.getDrop(true, data));
        }
        this.discard();
    }
}
