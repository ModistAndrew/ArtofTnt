package modist.artoftnt.common.entity;

import modist.artoftnt.common.advancements.critereon.IgniteTntFrameTrigger;
import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.core.addition.TntFrameData;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.addition.InstabilityHelper;
import modist.artoftnt.core.explosion.ExplosionHelper;
import modist.artoftnt.core.explosion.event.PrimedTntFrameHitBlockEvent;
import modist.artoftnt.core.explosion.event.PrimedTntFrameTickEvent;
import modist.artoftnt.core.explosion.manager.ExplosionResources;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class PrimedTntFrame extends AbstractHurtingProjectile {
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
        this.entityData.define(DATA_FUSE, 1);
        this.entityData.define(DATA_LEFT_COUNT, 1);
        this.entityData.define(DATA_COOL_DOWN, 0);
    }

    public CompoundTag getDataTag() {
        return this.entityData.get(DATA_TNT_FRAME);
    }

    public void setDataTag(CompoundTag tag) {
        this.entityData.set(DATA_TNT_FRAME, tag == null ? new CompoundTag() : tag);
    }

    private void setDataTagAndInit(CompoundTag tag) {
        setDataTag(tag);
        setFuse((int) (data.getValue(AdditionType.FUSE))+1);
        setLeftCount((int) (data.getValue(AdditionType.EXPLOSION_COUNT)));
        setCoolDown(0); //may be overwritten when loaded
        if (data.getValue(AdditionType.LIGHT) > 0) {
            this.setGlowingTag(true);
        }
        if(data.getValue(AdditionType.NO_PHYSICS) > 0){
            this.noPhysics = true; //also client!
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
        if(owner instanceof ServerPlayer player){
            IgniteTntFrameTrigger.TRIGGER.trigger(player, this.data);
        }
    }

    @Override
    //both sides!
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        if (DATA_TNT_FRAME.equals(pKey)) {
            this.data.deserializeNBT(getDataTag());
            this.refreshDimensions();
            if(data.getValue(AdditionType.NO_PHYSICS) > 0){
                this.noPhysics = true; //also client!
            }
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
        if(this.getFuse()==(int)(data.getValue(AdditionType.FUSE))+1){ //first tick, play sound
            this.setFuse(this.getFuse()-1);
            if(this.level.isClientSide){
                float loudness = data.getValue(AdditionType.LOUDNESS);
                ExplosionResources.TNT_SOUNDS.get(0,tier).ifPresent(t -> level.playLocalSound(this.getX(), this.getY(), this.getZ(), t,
                        SoundSource.BLOCKS, loudness, data.additions.sound() ? 1 :
                        (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F,
                        false));
            }
            return;
        }
        this.checkOutOfWorld();
        if(this.onGround) { //have to deal here
            float slipperiness = data.getValue(AdditionType.SLIPPERINESS);
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7D+slipperiness, -0.5D, 0.7D+slipperiness));
        }
        BlockHitResult hitresult1 = getBlockHitResult();
        EntityHitResult hitresult2 = getEntityHitResult(this::canHitEntity);
        HitResult hitresult = hitresult1 == null ? hitresult2 : hitresult1;
        if(hitresult1!=null) {
            MinecraftForge.EVENT_BUS.post(new PrimedTntFrameHitBlockEvent.Pre(this, hitresult1));
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        if(hitresult1!=null) {
            MinecraftForge.EVENT_BUS.post(new PrimedTntFrameHitBlockEvent.Post(this, hitresult1));
        }
        if (hitresult != null && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }
        this.updateInWaterStateAndDoFluidPushing();
        MinecraftForge.EVENT_BUS.post(new PrimedTntFrameTickEvent(this));
        int coolDown = this.getCoolDown() - 1;
        if (coolDown >= 0) {
            this.setCoolDown(coolDown); //simply set coolDown
        }
        if(!data.additions.persistent()) {
            int i = this.getFuse() - 1;
            this.setFuse(i);
            if (i <= 0) {
                doExplosion(null);
            }
        }
    }

    @Override
    public void checkOutOfWorld(){
        if (this.getY() < (double)(this.level.getMinBuildHeight() - 64) ||
                (this.data.getValue(AdditionType.LIGHTNESS)>=1 && this.getY() > (double)(this.level.getMaxBuildHeight() + 1024))) {
            this.outOfWorld();
        }
    }

    @Nullable
    private BlockHitResult getBlockHitResult() {
        Vec3 velocity = this.getDeltaMovement();
        BlockHitResult hitresult = null;
        Vec3 c = collide(velocity);
        if (!c.equals(Vec3.ZERO) && !c.equals(velocity)) { //simply use collide
            Vec3 delta = c.subtract(velocity);
            HitResult result1 = level.clip(new ClipContext(this.position(), this.position().add(this.getDeltaMovement()), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            Vec3 position = result1.getLocation();
            hitresult = new BlockHitResult(position, Direction.getNearest(delta.x, delta.y, delta.z),
                    new BlockPos(position), true);
        }
        return hitresult;
    }

    @Nullable
    private EntityHitResult getEntityHitResult(Predicate<Entity> pFilter) {
        return ProjectileUtil.getHitResult(this, this::canHitEntity) instanceof EntityHitResult ret ?
                ret : null;
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        if(pTarget instanceof PrimedTntFrame frame){
            return this.getOwner()!=frame.getOwner(); //avoid multi shot
        }
        return super.canHitEntity(pTarget);
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
            Vec3 vec31 = collideBoundingBox(this, new Vec3(pVec.x, this.maxUpStep, pVec.z), aabb, this.level, list);
            Vec3 vec32 = collideBoundingBox(this, new Vec3(0.0D, this.maxUpStep, 0.0D), aabb.expandTowards(pVec.x, 0.0D, pVec.z), this.level, list);
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

    private void doExplosion(@Nullable Vec3 pos) {
        if (this.getCoolDown() > 0) {
            return;
        }
        if (!this.level.isClientSide) {
            int random = (int)this.data.getValue(AdditionType.RANDOM)+1;
            if(this.random.nextInt(random)<1) {
                this.explode(pos);
            }
        }
        int count = this.getLeftCount() - 1;
        this.setLeftCount(count);
        int coolDown = (int) (data.getValue(AdditionType.EXPLOSION_INTERVAL));
        this.setCoolDown(coolDown);
        if (count <= 0) {
            this.discard();
        }
    }

    protected void explode(@Nullable Vec3 pos) {
        if (pos == null) {
            ExplosionHelper.explode(this.getDeltaMovement(), this.data.additions, this.level, this, this.getX(),
                    this.getY(0.0625D), this.getZ(), 4F, false);
        } else {
            ExplosionHelper.explode(this.getDeltaMovement(), this.data.additions, this.level, this, pos.x,
                    pos.y, pos.z, 4F, false);
        }
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
                InstabilityHelper.TNT_HIT_ENTITY_MIN_INSTABILITY) {
            this.doExplosion(null);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.data.getValue(AdditionType.INSTABILITY) >= InstabilityHelper.TNT_HIT_BLOCK_MIN_INSTABILITY) {
            this.doExplosion(pResult.getLocation());
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        float velocity = this.data.getValue(AdditionType.VELOCITY) * pVelocity; //multiply self
        Vec3 vec3 = (new Vec3(pX, pY, pZ)).normalize().add(this.random.nextGaussian() * (double) 0.0075F * (double) pInaccuracy,
                this.random.nextGaussian() * (double) 0.0075F * (double) pInaccuracy,
                this.random.nextGaussian() * (double) 0.0075F * (double) pInaccuracy).scale(velocity);
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
        return 0.5F;
    }

    public void defuse(boolean shouldFix) {
            if (!this.level.isClientSide) {
                this.spawnAtLocation(TntFrameBlock.dropFrame(shouldFix, data));
            }
            this.discard();
    }

    public void knocked(Entity entity, int amount) {
        this.markHurt();
        if (entity != null) {
            if (!this.level.isClientSide) {
                Vec3 vec3 = entity.getLookAngle();
                this.setDeltaMovement(vec3);
                this.xPower = vec3.x * amount/10D;
                this.yPower = vec3.y * amount/10D;
                this.zPower = vec3.z * amount/10D;
            }
        }
    }
}