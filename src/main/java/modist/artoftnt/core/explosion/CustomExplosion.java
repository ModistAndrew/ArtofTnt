package modist.artoftnt.core.explosion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class CustomExplosion extends Explosion { //have to override private fields and methods involved
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    //private final boolean fire;
    private final net.minecraft.world.level.Explosion.BlockInteraction blockInteraction;
    private final Random random = new Random();
    final Level level;
    final double x;
    final double y;
    final double z;
    final float radius;
    final float[] directionRadii = new float[6];
    final float strength;

    @Nullable
    private final Entity source;
    private final float punch;
    private final float sharpness;
    private final float piercing;
    private final float fire;
    private final float sound;
    private final AbstractExplosionShape explosionShape;
    private final DamageSource damageSource;
    private final ExplosionDamageCalculator damageCalculator;
    private final List<BlockPos> toBlow = Lists.newArrayList();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
    private final Vec3 position;

    public CustomExplosion(AdditionStack stack, Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, List<BlockPos> pPositions) {
        this(stack, pLevel, pSource, null, null, pToBlowX, pToBlowY, pToBlowZ, pRadius, false, Explosion.BlockInteraction.DESTROY);
        this.toBlow.addAll(pPositions);
    }
    //TODO: client no shape
    public CustomExplosion(AdditionStack stack, Level pLevel, @Nullable Entity pSource, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pDamageCalculator, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, net.minecraft.world.level.Explosion.BlockInteraction pBlockInteraction) {
        super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
        this.level = pLevel;
        this.source = pSource;
        //this.radius = pRadius; //ignore parameter and use stack
        this.radius = stack.getValue(AdditionType.RANGE);
        this.punch = stack.getValue(AdditionType.PUNCH);
        this.sharpness = stack.getValue(AdditionType.DAMAGE);
        this.strength = stack.getValue(AdditionType.STRENGTH);
        this.piercing = stack.getValue(AdditionType.PIERCING);
        this.fire = stack.getValue(AdditionType.FLAME);
        this.sound = stack.getValue(AdditionType.LOUDNESS);
        this.directionRadii[0] = stack.getValue(AdditionType.DOWN);
        this.directionRadii[1] = stack.getValue(AdditionType.UP);
        this.directionRadii[2] = stack.getValue(AdditionType.NORTH);
        this.directionRadii[3] = stack.getValue(AdditionType.SOUTH);
        this.directionRadii[4] = stack.getValue(AdditionType.WEST);
        this.directionRadii[5] = stack.getValue(AdditionType.EAST);
        this.x = pToBlowX;
        this.y = pToBlowY;
        this.z = pToBlowZ;
        //this.fire = pFire;
        this.blockInteraction = pBlockInteraction;
        this.damageSource = pDamageSource == null ? DamageSource.explosion(this) : pDamageSource;
        this.damageCalculator = pDamageCalculator == null ? this.makeDamageCalculator(pSource) : pDamageCalculator;
        this.position = new Vec3(this.x, this.y, this.z);
        this.explosionShape = ExplosionShapes.get(stack.getItems(AdditionType.SHAPE), this);
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity pEntity) {
        return pEntity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(pEntity);
    }

    public static float getSeenPercent(Vec3 pExplosionVector, Entity pEntity) {
        //get the percentage of the entity that are exposed to the tnt
        AABB aabb = pEntity.getBoundingBox();
        double d0 = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D; //offset to make sure that d5 and d7 is in the middle
        //for example, 0 0.3 0.6 0.9 -> 0.05 0.35 0.65 0.95
        //for axis Y, it will ignore the top
        if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
            int i = 0;
            int j = 0;

            for (double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
                for (double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
                    for (double d7 = 0.0D; d7 <= 1.0D; d7 += d2) { //go through the aabb
                        double d8 = Mth.lerp(d5, aabb.minX, aabb.maxX);
                        double d9 = Mth.lerp(d6, aabb.minY, aabb.maxY);
                        double d10 = Mth.lerp(d7, aabb.minZ, aabb.maxZ);
                        Vec3 vec3 = new Vec3(d8 + d3, d9, d10 + d4); //go through the aabb
                        if (pEntity.level.clip(new ClipContext(vec3, pExplosionVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pEntity)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }

    /**
     * Does the first part of the explosion (destroy blocks)
     */
    @Override
    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new BlockPos(this.x, this.y, this.z));
        /*Set<BlockPos> set = new HashSet<>();

        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                for (int l = 0; l < 16; ++l) {
                    if (j == 0 || j == 15 || k == 0 || k == 15 || l == 0 || l == 15) { //numerate directions
                        //six faces
                        double d0 = (float) j / 15.0F * 2.0F - 1.0F;
                        double d1 = (float) k / 15.0F * 2.0F - 1.0F;
                        double d2 = (float) l / 15.0F * 2.0F - 1.0F; //-1 to 1
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3; //normalize
                        float f = this.radius * (0.7F + this.level.random.nextFloat() * 0.6F); //strength with randomness in every direction
                        double d4 = this.x;
                        double d6 = this.y;
                        double d8 = this.z; //start from center

                        for (; f > 0.0F; f -= 0.22500001F) { //decay of the explosion
                            BlockPos blockpos = new BlockPos(d4, d6, d8);
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            FluidState fluidstate = this.level.getFluidState(blockpos);
                            if (!this.level.isInWorldBounds(blockpos)) {
                                break;
                            }

                            Optional<Float> optional = this.damageCalculator.getBlockExplosionResistance(this, this.level, blockpos, blockstate, fluidstate);
                            //TODO: water
                            //not air?
                            float f0 = f;
                            if (strength <= 0F) {
                                f = 0F;
                            } else if (optional.isPresent()) {
                                float scale = (1F / strength - 0.1F) / 3;
                                f -= (optional.get() + 0.3F) * scale; //protection by blocks with high resistance
                            }
                            //if resistance is too high, f will be smaller than 0, thus protected
                            if (f > 0.0F && this.damageCalculator.shouldBlockExplode(this, this.level, blockpos, blockstate, f)) {
                                set.add(blockpos);
                            }

                            f += (f0 - f) * piercing; //restore f

                            d4 += d0 * (double) 0.3F;
                            d6 += d1 * (double) 0.3F;
                            d8 += d2 * (double) 0.3F; //search next, with the step of 0.3F, smaller than 1
                        }
                    }
                }
            }
        }*/

        this.toBlow.addAll(explosionShape.getToBlow());
        float f2 = this.radius * 2.0F * Math.max(sharpness, Math.abs(punch)); //the actual radius
        int k1 = Mth.floor(this.x - (double) f2 - 1.0D);
        int l1 = Mth.floor(this.x + (double) f2 + 1.0D);
        int i2 = Mth.floor(this.y - (double) f2 - 1.0D);
        int i1 = Mth.floor(this.y + (double) f2 + 1.0D);
        int j2 = Mth.floor(this.z - (double) f2 - 1.0D);
        int j1 = Mth.floor(this.z + (double) f2 + 1.0D);
        //TODO : larger?
        List<Entity> list = this.level.getEntities(this.source, new AABB(k1, i2, j2, l1, i1, j1));
        //select all entities in the box
        //later will select entities in the sphere
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, list, f2);
        Vec3 vec3 = new Vec3(this.x, this.y, this.z);

        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {
                double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double) f2;
                //distance from the center divided by strength, the smaller, the farther to blow
                if (d12 <= 1.0D) { //in the sphere
                    double d5 = entity.getX() - this.x;
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.y;
                    //tnt can be blown higher
                    double d9 = entity.getZ() - this.z;
                    double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9); //distance
                    if (d13 != 0.0D) { //not centre
                        d5 /= d13;
                        d7 /= d13;
                        d9 /= d13; //normalize
                        double d14 = getSeenPercent(vec3, entity); //get percentage
                        d14 += (1 - d14) * piercing;
                        double d10 = (1.0D - d12) * d14; //total strength
                        if (sharpness > 0) {
                            entity.hurt(this.getDamageSource(), sharpness * ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        } else if (sharpness < 0 && entity instanceof LivingEntity le) {
                            le.heal(-sharpness * ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) f2 + 1.0D)));
                        }
                        double d11 = d10;
                        if (entity instanceof LivingEntity) {
                            d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, d10);
                            //enchantment
                        }
                        entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11 * punch, d7 * d11 * punch, d9 * d11 * punch));
                        if (entity instanceof Player player) {
                            if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                                this.hitPlayers.put(player, new Vec3(d5 * d10 * punch, d7 * d10 * punch, d9 * d10 * punch));
                                //for players, also do on the client side
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    @Override
    public void finalizeExplosion(boolean pSpawnParticles) {
        if (this.level.isClientSide) {
            this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.ARROW_HIT, SoundSource.BLOCKS, random.nextFloat() * 10F * sound, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
            //TODO
        }

        boolean flag = this.blockInteraction != net.minecraft.world.level.Explosion.BlockInteraction.NONE; //explosion type
        if (pSpawnParticles) {
            if (!(this.radius < 2.0F) && flag) {
                this.level.addParticle(ParticleTypes.ANGRY_VILLAGER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            } else {
                this.level.addParticle(ParticleTypes.BUBBLE, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
            }
            //TODO
        }

        if (flag) { //fill blocks with air
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList<>();
            Collections.shuffle(this.toBlow, this.level.random);

            for (BlockPos blockpos : this.toBlow) {
                BlockState blockstate = this.level.getBlockState(blockpos);
                if (!blockstate.isAir()) {
                    BlockPos blockPos1 = blockpos.immutable();
                    this.level.getProfiler().push("explosion_blocks");
                    if (blockstate.canDropFromExplosion(this.level, blockpos, this) && this.level instanceof ServerLevel) {
                        BlockEntity blockentity = blockstate.hasBlockEntity() ? this.level.getBlockEntity(blockpos) : null;
                        LootContext.Builder lootContext$builder = (new LootContext.Builder((ServerLevel) this.level)).withRandom(this.level.random)
                                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockpos))
                                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity)
                                .withOptionalParameter(LootContextParams.THIS_ENTITY, this.source);
                        if (this.blockInteraction == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY) {
                            lootContext$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, this.radius);
                        }
                        //TODO
                        //drop with loot context
                        blockstate.getDrops(lootContext$builder).forEach((p_46074_) -> addBlockDrops(objectArrayList, p_46074_, blockPos1));
                    }

                    blockstate.onBlockExploded(this.level, blockpos, this);
                    //fill blocks with air
                    this.level.getProfiler().pop();
                }
            }

            for (Pair<ItemStack, BlockPos> pair : objectArrayList) {
                Block.popResource(this.level, pair.getSecond(), pair.getFirst());
                //generate items
            }
        }

        if (this.fire > 0F) { //set fire
            for (BlockPos blockPos2 : this.toBlow) {
                if (this.random.nextInt(10) < this.fire && this.level.getBlockState(blockPos2).isAir() && this.level.getBlockState(blockPos2.below()).isSolidRender(this.level, blockPos2.below())) {
                    this.level.setBlockAndUpdate(blockPos2, BaseFireBlock.getState(this.level, blockPos2));
                }
            }
        }

    }

    private static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack pStack, BlockPos pPos) {
        //TODO
        //pStack = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 64);
        int i = pDropPositionArray.size();
        for (int j = 0; j < i; ++j) { //try to merge items into groups with size of 16
            Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, pStack)) {
                ItemStack itemStack1 = ItemEntity.merge(itemstack, pStack, 16);
                pDropPositionArray.set(j, Pair.of(itemStack1, pair.getSecond()));
                if (pStack.isEmpty()) {
                    return;
                }
            }
        }
        pDropPositionArray.add(Pair.of(pStack, pPos));
    }

    @Override
    public DamageSource getDamageSource() {
        return this.damageSource;
    }

    @Override
    public Map<Player, Vec3> getHitPlayers() {
        return this.hitPlayers;
    }

    /**
     * Returns either the entity that placed the explosive block, the entity that caused the explosion or null.
     */
    @Override
    @Nullable
    public LivingEntity getSourceMob() {
        if (this.source == null) {
            return null;
        } else if (this.source instanceof PrimedTnt) {
            return ((PrimedTnt) this.source).getOwner();
        } else if (this.source instanceof LivingEntity) {
            return (LivingEntity) this.source;
        } else {
            if (this.source instanceof Projectile) {
                Entity entity = ((Projectile) this.source).getOwner();
                if (entity instanceof LivingEntity) {
                    return (LivingEntity) entity;
                }
            }

            return null;
        }
    }

    @Override
    public void clearToBlow() {
        this.toBlow.clear();
    }

    @Override
    public List<BlockPos> getToBlow() {
        return this.toBlow;
    }

    @Override
    public Vec3 getPosition() {
        return this.position;
    }

    @Override
    @Nullable
    public Entity getExploder() {
        return this.source;
    }
}
