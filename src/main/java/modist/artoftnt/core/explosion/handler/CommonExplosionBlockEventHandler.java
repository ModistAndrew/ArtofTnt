package modist.artoftnt.core.explosion.handler;

import com.mojang.datafixers.util.Pair;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.DiminishingLightBlock;
import modist.artoftnt.common.item.PositionMarkerItem;
import modist.artoftnt.common.item.TntFireworkStarItem;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import modist.artoftnt.core.explosion.event.*;
import modist.artoftnt.core.explosion.manager.ExplosionSpecialDrops;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonExplosionBlockEventHandler {
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void blockDropEvent(CustomExplosionBlockDropEvent event) {
        BlockPos blockPos = event.pos;
        CustomExplosion explosion = event.explosion;
        BlockState blockstate = explosion.level.getBlockState(blockPos);
        if (!blockstate.isAir()) {
            if (blockstate.canDropFromExplosion(explosion.level, blockPos, explosion)) {
                BlockEntity blockentity = blockstate.hasBlockEntity() ? explosion.level.getBlockEntity(blockPos) : null;
                LootContext.Builder lootContext$builder = (new LootContext.Builder((ServerLevel) explosion.level)).withRandom(explosion.level.random)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.getSource());
                blockstate.getDrops(lootContext$builder).forEach(stack -> CustomExplosion.addBlockDrops(event.objectArrayList, stack, blockPos));
            }
        }
    }

    @SubscribeEvent
    public static void specialBlockDropEvent(CustomExplosionBlockDropEvent event) {
        BlockPos blockPos = event.pos;
        CustomExplosion explosion = event.explosion;
        BlockState blockstate = explosion.level.getBlockState(blockPos);
        if (!blockstate.isAir()) {
            if (blockstate.canDropFromExplosion(explosion.level, blockPos, explosion)) {
                ItemStack specialDrop = ExplosionSpecialDrops.ITEMS.getSpecialDrop(blockstate, event);
                if (specialDrop != null) {
                    CustomExplosion.addBlockDrops(event.objectArrayList, specialDrop.copy(), blockPos);
                    event.setCanceled(true); //skip
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void setBlockDropChance(CustomExplosionBlockDropEvent event) {
        float drop = event.data.getValue(AdditionType.DROP);
        if (event.explosion.random.nextFloat() > drop) {
            event.setCanceled(true); //skip
        }
    }

    @SubscribeEvent
    public static void lightningEvent(CustomExplosionBlockBreakEvent.Post event) {
        CustomExplosion explosion = event.explosion;
        float lightning = event.data.getValue(AdditionType.LIGHTNING);
        if (lightning > 4F) {
            if (explosion.random.nextInt(10) < lightning * event.percentage &&
                    explosion.level.getBlockState(event.pos).isAir() &&
                    explosion.level.getBlockState(event.pos.below()).isSolidRender(explosion.level, event.pos.below())) {
                summonLightningBolt(explosion, event.pos);
            }
        }
    }

    @SubscribeEvent
    public static void flameEvent(CustomExplosionBlockBreakEvent.Post event) {
        CustomExplosion explosion = event.explosion;
        float flame = event.data.getValue(AdditionType.FLAME);
        if (flame > 0F) {
            if (explosion.random.nextInt(10) < flame * event.percentage &&
                    explosion.level.getBlockState(event.pos).isAir() &&
                    explosion.level.getBlockState(event.pos.below()).isSolidRender(explosion.level, event.pos.below())) {
                explosion.level.setBlockAndUpdate(event.pos, BaseFireBlock.getState(explosion.level, event.pos));
                if (explosion.random.nextInt(20) < flame * event.percentage) {
                    BlockState under = explosion.level.getBlockState(event.pos.below());
                    if (under.is(BlockTags.BASE_STONE_OVERWORLD)) {
                        explosion.level.setBlockAndUpdate(event.pos.below(), Blocks.NETHERRACK.defaultBlockState()); //under
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void lightEvent(CustomExplosionFinishedEvent event) {
        CustomExplosion explosion = event.explosion;
        float light = event.data.getValue(AdditionType.LIGHT);
        if (light > 0) {
            AABB aabb = new AABB(new BlockPos(explosion.getPosition().add(-light, -light, -light)),
                    new BlockPos(explosion.getPosition().add(light, light, light)));
            explosion.level.getEntities(explosion.getSource(), aabb).stream().filter(e ->
                    e.distanceToSqr(explosion.getPosition()) < light * light).forEach(e -> {
                if (e instanceof LivingEntity le) {
                    if (le.isAffectedByPotions()) {
                        MobEffectInstance mobeffectinstance = new MobEffectInstance(MobEffects.GLOWING, (int) (light * 20), 0); //ignore percentage
                        le.addEffect(new MobEffectInstance(mobeffectinstance), explosion.getSource());
                    }
                }
            });
            BlockPos.betweenClosedStream(aabb).filter(bp ->
                    explosion.level.getBlockState(bp).isAir() &&
                            explosion.getPosition().distanceToSqr(Vec3.atCenterOf(bp)) < light * light).map(bp -> bp.immutable()).forEach(bp -> {
                explosion.level.setBlockAndUpdate(bp, BlockLoader.DIMINISHING_LIGHT.get().defaultBlockState().
                        setValue(DiminishingLightBlock.LEVEL, 15)); //simply use 15 ad ignore strength
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void blowUpEvent(CustomExplosionFinishingEvent event) {
        CustomExplosion explosion = event.explosion;
        Level l = explosion.level;
        if(l instanceof ServerLevel level) {
            float blowUp = event.data.getValue(AdditionType.BLOW_UP);
            if (blowUp > 0) {
                List<BlockPos> remove = new ArrayList<>();
                explosion.getToBlow().forEach(pos -> {
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir()) {
                        remove.add(pos);
                        Entity entity = createFallingBlock(level, Vec3.atCenterOf(pos),
                                explosion.getPosition().add(explosion.getVec().scale(blowUp))
                                        .subtract(Vec3.atCenterOf(pos)).scale(-blowUp), state);
                        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
                        level.addFreshEntity(entity);
                    }
                });
                explosion.getToBlow().removeAll(remove);
            }
        }
    }

    private static Entity createFallingBlock(ServerLevel pLevel, Vec3 pPos, Vec3 movement, BlockState pState){
        CompoundTag tag = new CompoundTag();
        tag.put("BlockState", NbtUtils.writeBlockState(pState));
        ListTag listtag = new ListTag();
        listtag.add(DoubleTag.valueOf(movement.x));
        listtag.add(DoubleTag.valueOf(movement.y));
        listtag.add(DoubleTag.valueOf(movement.z));
        tag.put("Motion", listtag);
        tag.putString("id", "minecraft:falling_block");
        return EntityType.loadEntityRecursive(tag, pLevel, (p_138828_) -> {
            p_138828_.moveTo(pPos.x, pPos.y, pPos.z, p_138828_.getYRot(), p_138828_.getXRot());
            return p_138828_;
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void replaceBlockEvent(CustomExplosionBlockBreakEvent.Pre event) {
        CustomExplosion explosion = event.explosion;
        BlockState state = explosion.level.getBlockState(event.pos);
        BlockState newState = ExplosionSpecialDrops.BLOCKS.getSpecialDrop(state, event);
        if (newState != null) {
            explosion.level.setBlockAndUpdate(event.pos, newState);
            event.setCanceled(true); //prevent block drop
        }
    }

    @SubscribeEvent
    public static void doBlockDropEvent(CustomExplosionFinishedEvent event) {
        for (Pair<ItemStack, BlockPos> pair : event.objectArrayList) {
            Block.popResource(event.explosion.level, pair.getSecond(), pair.getFirst());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void specialDoBlockDropEvent(CustomExplosionFinishedEvent event) {
        CustomExplosion explosion = event.explosion;
        BlockPos containerPos = null;
        AtomicReference<IItemHandler> container = new AtomicReference<>();
        ItemStack marker = event.data.getItems(AdditionType.CONTAINER).isEmpty() ?
                null : event.data.getItems(AdditionType.CONTAINER).peek();
        if (marker != null && marker.getItem() instanceof PositionMarkerItem item) {
            if (item.isContainer) {
                BlockPos pos = new BlockPos(item.getPos(event.explosion.level, explosion.getPosition(), marker));
                if (pos != null) {
                    containerPos = pos;
                    BlockEntity be = explosion.level.getBlockEntity(pos);
                    if (be != null) {
                        be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c ->
                                container.set(c));
                    }
                }
            }
        }
        if (containerPos != null) { //has pos
            for (Pair<ItemStack, BlockPos> pair : event.objectArrayList) {
                ItemStack remain = pair.getFirst();
                if (container.get() != null) {
                    for (int i = 0; i < container.get().getSlots(); i++) {
                        remain = container.get().insertItem(i, remain, false);
                        if (remain.isEmpty()) {
                            break;
                        }
                    }
                }
                if (!remain.isEmpty()) {
                    Block.popResource(explosion.level, containerPos, remain);
                }
            }
            event.setCanceled(true);
        }
    }

    static void summonLightningBolt(CustomExplosion explosion, BlockPos pos) {
        Level level = explosion.level;
        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
        Entity source = explosion.getSourceMob();
        lightningbolt.setCause(source instanceof ServerPlayer ? (ServerPlayer) source : null);
        level.addFreshEntity(lightningbolt);
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
    }

    @SubscribeEvent
    public static void fireworkEvent(CustomExplosionFinishingEvent event) {
        CustomExplosion explosion = event.explosion;
        event.data.getItems(AdditionType.FIREWORK).forEach(itemStack -> {
            if (itemStack.getItem() instanceof TntFireworkStarItem) {
                TntFireworkStarItem.shoot(explosion.level, itemStack, explosion);
            }
        });
    }
}
