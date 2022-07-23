package modist.artoftnt.common.entity;

import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.addition.InstabilityHelper;
import modist.artoftnt.core.explosion.ExplosionHelper;
import modist.artoftnt.core.explosion.event.PrimedTntFrameHitBlockEvent;
import modist.artoftnt.core.explosion.event.PrimedTntFrameHitEntityEvent;
import modist.artoftnt.core.explosion.event.PrimedTntFrameTickEvent;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PrimedTntFrame extends AbstractHurtingProjectile { //TODO:needn't event
    public final int tier;
    private static final EntityDataAccessor<CompoundTag> DATA_TNT_FRAME = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.COMPOUND_TAG);
    private static final EntityDataAccessor<Integer> DATA_LEFT_COUNT = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_COOL_DOWN = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FUSE = SynchedEntityData.defineId(PrimedTntFrame.class, EntityDataSerializers.INT);

    public final TntFrameData data; //cache

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
        if(data.getValue(AdditionType.LIGHT)>0){
            this.setGlowingTag(true);
        }
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
        HitResult hitresult = getHitResult(this::canHitEntity); //TODO ?
        if (hitresult != null && hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        HitResult eventHitResult = ProjectileUtil.getHitResult(this, this::canHitEntity);
        HitResult.Type type = eventHitResult.getType();
        if (type == HitResult.Type.ENTITY) {
            MinecraftForge.EVENT_BUS.post(new PrimedTntFrameHitEntityEvent(this, (EntityHitResult)eventHitResult));
        } else if (type == HitResult.Type.BLOCK) {
            MinecraftForge.EVENT_BUS.post(new PrimedTntFrameHitBlockEvent(this, (BlockHitResult)eventHitResult));
        } //use different system for explosion and event, or move will be strange
        MinecraftForge.EVENT_BUS.post(new PrimedTntFrameTickEvent(this));
        this.updateInWaterStateAndDoFluidPushing();
        int coolDown = this.getCoolDown() - 1;
        if (coolDown >= 0) {
            this.setCoolDown(coolDown); //simply set coolDown
        }
        int i = this.getFuse() - 1;
        this.setFuse(i);
        if (i <= 0) {
            doExplosion();
        }
    }

    @Nullable
    private HitResult getHitResult(Predicate<Entity> pFilter) {
        Vec3 velocity = this.getDeltaMovement();
        HitResult hitresult = null;
        Vec3 c = collide(velocity);
        if (!c.equals(velocity)) { //simply use collide
            hitresult = new BlockHitResult(this.position(), Direction.getNearest(velocity.x, velocity.y, velocity.z).getOpposite(),
                    this.blockPosition(), true);
        }
        for (Entity entity1 : level.getEntities(this, this.getBoundingBox(), pFilter)) {
            AABB aabb = entity1.getBoundingBox();
            if (aabb.intersects(this.getBoundingBox())) {
                return new EntityHitResult(entity1); //entity first
            }
        }
        return hitresult;
    }

    private Vec3 collide(Vec3 pVec) {
        AABB aabb = this.getBoundingBox();
        List<VoxelShape> list = this.level.getEntityCollisions(this, aabb.expandTowards(pVec));
        Vec3 vec3 = pVec.lengthSqr() == 0.0D ? pVec : collideBoundingBox(this, pVec, aabb, this.level, list);
        boolean flag = pVec.x != vec3.x;
        boolean flag1 = pVec.y != vec3.y;
        boolean flag2 = pVec.z != vec3.z;
        boolean flag3 = this.onGround || flag1 && pVec.y < 0.0D;
        if (this.maxUpStep > 0.0F && flag3 && (flag || flag2)) {
            Vec3 vec31 = collideBoundingBox(this, new Vec3(pVec.x, (double) this.maxUpStep, pVec.z), aabb, this.level, list);
            Vec3 vec32 = collideBoundingBox(this, new Vec3(0.0D, (double) this.maxUpStep, 0.0D), aabb.expandTowards(pVec.x, 0.0D, pVec.z), this.level, list);
            if (vec32.y < (double) this.maxUpStep) {
                Vec3 vec33 = collideBoundingBox(this, new Vec3(pVec.x, 0.0D, pVec.z), aabb.move(vec32), this.level, list).add(vec32);
                if (vec33.horizontalDistanceSqr() > vec31.horizontalDistanceSqr()) {
                    vec31 = vec33;
                }
            }

            if (vec31.horizontalDistanceSqr() > vec3.horizontalDistanceSqr()) {
                return vec31.add(collideBoundingBox(this, new Vec3(0.0D, -vec31.y + pVec.y, 0.0D), aabb.move(vec31), this.level, list));
            }
        }
        return vec3;
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

    protected boolean canHitEntity(Entity pTarget) {
        if (!pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable()) {
            return data.additions.shouldAttack(getOwner(), pTarget);
        }
        return false;
    }

    protected void explode() {
        ExplosionHelper.explode(this.getDeltaMovement(), this.data.additions, this.level, this, this.getX(), this.getY(0.0625D), this.getZ(),
                4F, false);
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
    protected void onHitEntity(EntityHitResult pResult) {
        if (this.data.getValue(AdditionType.INSTABILITY) >=
                InstabilityHelper.tntHitEntityMinInstability(pResult.getEntity()==this.getOwner())) {
            this.doExplosion();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.data.getValue(AdditionType.INSTABILITY) >= InstabilityHelper.TNT_HIT_BLOCK_MIN_INSTABILITY) {
            this.doExplosion(); //TODO
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
            this.spawnAtLocation(TntFrameBlock.dropFrame(true, data));
        }
        this.discard();
    }
}
