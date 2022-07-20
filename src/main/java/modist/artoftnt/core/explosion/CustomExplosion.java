package modist.artoftnt.core.explosion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.*;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.event.*;
import modist.artoftnt.core.explosion.shape.AbstractExplosionShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.*;

public class CustomExplosion extends Explosion { //have to override private fields and methods involved
    private static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    //private final boolean fire;
    public final net.minecraft.world.level.Explosion.BlockInteraction blockInteraction;
    public final Random random = new Random();
    public final Level level;
    public final double x;
    public final double y;
    public final double z;
    private final float radius;
    private final float strength;
    @Nullable
    private final Entity source;

    private final AbstractExplosionShape explosionShape;
    private final DamageSource damageSource;
    private final List<BlockPos> toBlow = Lists.newArrayList();
    private final Map<Player, Vec3> hitPlayers = Maps.newHashMap();
    private final Vec3 position;
    private final AdditionStack stack;

    public CustomExplosion(AdditionStack stack, Level pLevel, @Nullable Entity pSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, List<BlockPos> pPositions) {
        this(stack, pLevel, pSource, null, null, pToBlowX, pToBlowY, pToBlowZ, pRadius, false, Explosion.BlockInteraction.DESTROY);
        this.toBlow.addAll(pPositions);
    }

    //TODO: client no shape
    public CustomExplosion(AdditionStack stack, Level pLevel, @Nullable Entity pSource, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pDamageCalculator, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, boolean pFire, net.minecraft.world.level.Explosion.BlockInteraction pBlockInteraction) {
        super(pLevel, pSource, pDamageSource, pDamageCalculator, pToBlowX, pToBlowY, pToBlowZ, pRadius, pFire, pBlockInteraction);
        this.stack = stack;
        this.level = pLevel;
        this.source = pSource;
        this.radius = stack.getValue(AdditionType.RANGE);
        this.strength = stack.getValue(AdditionType.STRENGTH);
        this.x = pToBlowX;
        this.y = pToBlowY;
        this.z = pToBlowZ;
        this.blockInteraction = pBlockInteraction;
        this.damageSource = pDamageSource == null ? DamageSource.explosion(this) : pDamageSource;
        this.position = new Vec3(this.x, this.y, this.z);
        this.explosionShape = ExplosionShapes.get(stack.getItems(AdditionType.SHAPE), this);
    }

    public AdditionStack getAdditionStack(){
        return stack;
    }

    private ExplosionDamageCalculator makeDamageCalculator(@Nullable Entity pEntity) {
        return pEntity == null ? EXPLOSION_DAMAGE_CALCULATOR : new EntityBasedExplosionDamageCalculator(pEntity);
    }

    /**
     * Does the first part of the explosion (destroy blocks)
     */
    @Override
    public void explode() {
        this.level.gameEvent(this.source, GameEvent.EXPLODE, new BlockPos(this.x, this.y, this.z));
        if (this.strength > 0) {
            explosionShape.generateData();
        }
        this.toBlow.addAll(explosionShape.getToBlow());
        Object2FloatMap<Entity> list = explosionShape.getEntities();
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, this, list.keySet().stream().toList(), radius * 2);
        for (Entity entity : list.keySet()) {
            float percentage = list.getFloat(entity); //percentage of explosion the entity get
            MinecraftForge.EVENT_BUS.post(new CustomExplosionEntityEvent(this, entity, percentage));
        }
    }

    private float getBlockPercentage(BlockPos pos) {
        float percentage = explosionShape.getToBlowData().get(pos).strength() / strength;
        return percentage;
    }

    /**
     * Does the second part of the explosion (sound, particles, drop spawn)
     */
    @Override
    public void finalizeExplosion(boolean pSpawnParticles) {
        if (this.level.isClientSide) {
            MinecraftForge.EVENT_BUS.post(new CustomExplosionClientEvent(this));
            //TODO
        }

        boolean flag = this.blockInteraction != net.minecraft.world.level.Explosion.BlockInteraction.NONE; //explosion type

        if (flag && !this.level.isClientSide) { //fill blocks with air
            ObjectArrayList<Pair<ItemStack, BlockPos>> objectArrayList = new ObjectArrayList<>();
            Collections.shuffle(this.toBlow, this.level.random);

            for (BlockPos blockpos : this.toBlow) {
                BlockPos pos1 = blockpos.immutable();
                if(!MinecraftForge.EVENT_BUS.post(new CustomExplosionBlockBreakEvent.Pre(this, pos1, getBlockPercentage(pos1)))) {
                    MinecraftForge.EVENT_BUS.post(new CustomExplosionBlockDropEvent(this, pos1, getBlockPercentage(pos1), objectArrayList));
                    BlockState blockstate = level.getBlockState(pos1);
                    blockstate.onBlockExploded(level, pos1, this);
                    MinecraftForge.EVENT_BUS.post(new CustomExplosionBlockBreakEvent.Post(this, pos1, getBlockPercentage(pos1)));
                }
            }

            MinecraftForge.EVENT_BUS.post(new CustomExplosionDoBlockDropEvent(this, objectArrayList));
        }
    }

    public static void addBlockDrops(ObjectArrayList<Pair<ItemStack, BlockPos>> pDropPositionArray, ItemStack pStack, BlockPos pPos) {
        //TODO
        int i = pDropPositionArray.size();
        for (int j = 0; j < i; ++j) { //try to merge items into groups with size of 16(64?)
            Pair<ItemStack, BlockPos> pair = pDropPositionArray.get(j);
            ItemStack itemstack = pair.getFirst();
            if (ItemEntity.areMergable(itemstack, pStack)) {
                ItemStack itemStack1 = ItemEntity.merge(itemstack, pStack, 64);
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

    public Entity getSource() {
        return this.source;
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
