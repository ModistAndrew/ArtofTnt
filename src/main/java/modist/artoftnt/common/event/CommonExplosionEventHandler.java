package modist.artoftnt.common.event;

import com.mojang.datafixers.util.Pair;
import modist.artoftnt.common.item.PositionMarkerItem;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import modist.artoftnt.core.explosion.ExplosionSpecialBlockDrops;
import modist.artoftnt.core.explosion.event.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonExplosionEventHandler {

    @SubscribeEvent
    public static void damageEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        float percentage = event.percentage;
        float damage = explosion.getAdditionStack().getValue(AdditionType.DAMAGE);
        if (damage > 0) {
            event.entity.hurt(explosion.getDamageSource(), damage * ((int) ((percentage * percentage + percentage) / 2.0D * 7.0D + 1.0D)));
        }
    }

    @SubscribeEvent
    public static void potionEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        if (event.entity instanceof LivingEntity le) {
            for (ItemStack stack : explosion.getAdditionStack().getItems(AdditionType.POTION)) {
                Potion potion = PotionUtils.getPotion(stack);
                if (potion != Potions.EMPTY) {
                    if (le.isAffectedByPotions()) {
                        for (MobEffectInstance mobeffectinstance : potion.getEffects()) {
                            mobeffectinstance = new MobEffectInstance(mobeffectinstance.getEffect(), (int) (mobeffectinstance.getDuration() * event.percentage),
                                    mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible());
                            if (mobeffectinstance.getEffect().isInstantenous()) {
                                mobeffectinstance.getEffect().applyInstantenousEffect(explosion.getSource(), explosion.getSourceMob(),
                                        le, mobeffectinstance.getAmplifier(), 0.5D);
                            } else {
                                le.addEffect(new MobEffectInstance(mobeffectinstance), explosion.getSource());
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void punchEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        float punch = explosion.getAdditionStack().getValue(AdditionType.PUNCH) -
                explosion.getAdditionStack().getValue(AdditionType.DRAW);
        Entity entity = event.entity;
        float percentage = event.percentage;
        if (punch != 0) {
            double d5 = entity.getX() - explosion.x;
            double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - explosion.y;
            //tnt can be blown higher
            double d9 = entity.getZ() - explosion.z;
            double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9); //distance
            if (d13 != 0.0D) {
                d5 /= d13;
                d7 /= d13;
                d9 /= d13; //normalize
                double d11 = percentage;
                if (entity instanceof LivingEntity) {
                    d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener((LivingEntity) entity, percentage);
                    //enchantment
                }
                entity.setDeltaMovement(entity.getDeltaMovement().add(d5 * d11 * punch, d7 * d11 * punch, d9 * d11 * punch));
                if (entity instanceof Player player) {
                    if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                        explosion.getHitPlayers().put(player, new Vec3(d5 * percentage * punch, d7 * percentage * punch, d9 * percentage * punch));
                        //for players, also do on the client side
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void flameEvent(CustomExplosionEntityEvent event) {
        float flame = event.explosion.getAdditionStack().getValue(AdditionType.FLAME);
        if (flame > 0) {
            if (!event.entity.fireImmune()) {
                event.entity.setRemainingFireTicks((int) (flame * 200 * event.percentage));
            }
        }
    }

    @SubscribeEvent
    public static void lightningEvent(CustomExplosionEntityEvent event) {
        CustomExplosion explosion = event.explosion;
        float lightning = explosion.getAdditionStack().getValue(AdditionType.LIGHTNING);
        if (lightning > 0) {
            BlockPos pos = event.entity.blockPosition();
            if (event.entity instanceof LivingEntity && explosion.level.canSeeSky(pos) && explosion.random.nextInt(4) < lightning * event.percentage) {
                summonLightningBolt(explosion, pos);
            }
        }
    }

    @SubscribeEvent
    public static void blockDropEvent(CustomExplosionBlockDropEvent event) {
        BlockPos blockPos = event.pos;
        CustomExplosion explosion = event.explosion;
        BlockState blockstate = explosion.level.getBlockState(blockPos);
        float drop = explosion.getAdditionStack().getValue(AdditionType.DROP);
        if (!blockstate.isAir()) {
            if (drop > 0 && blockstate.canDropFromExplosion(explosion.level, blockPos, explosion)) {
                BlockEntity blockentity = blockstate.hasBlockEntity() ? explosion.level.getBlockEntity(blockPos) : null;
                LootContext.Builder lootContext$builder = (new LootContext.Builder((ServerLevel) explosion.level)).withRandom(explosion.level.random)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                        .withParameter(LootContextParams.TOOL, ItemStack.EMPTY)
                        .withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockentity)
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, explosion.getSource());
                if (explosion.blockInteraction == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY) {
                    if (drop < 1) {
                        lootContext$builder.withParameter(LootContextParams.EXPLOSION_RADIUS, 1 / drop);
                    }
                }
                blockstate.getDrops(lootContext$builder).forEach(stack -> CustomExplosion.addBlockDrops(event.objectArrayList, stack, blockPos));
            }
        }
    }

    @SubscribeEvent
    public static void specialBlockDropEvent(CustomExplosionBlockDropEvent event) {
        BlockPos blockPos = event.pos;
        CustomExplosion explosion = event.explosion;
        BlockState blockstate = explosion.level.getBlockState(blockPos);
        float temperature = explosion.getAdditionStack().getValue(AdditionType.TEMPERATURE);
        float strength = explosion.getAdditionStack().getValue(AdditionType.STRENGTH);
        float drop = explosion.getAdditionStack().getValue(AdditionType.DROP);
        if (!blockstate.isAir()) {
            if (drop > 0 && blockstate.canDropFromExplosion(explosion.level, blockPos, explosion)) {
                ItemStack specialDrop = ExplosionSpecialBlockDrops.getSpecialDrop(blockstate.getBlock(), event.percentage, temperature, strength);
                if (!specialDrop.isEmpty()) {
                    CustomExplosion.addBlockDrops(event.objectArrayList, specialDrop, blockPos);
                }
            }
        }
    }

    @SubscribeEvent
    public static void lightningBlockEvent(CustomExplosionBlockBreakEvent event) {
        CustomExplosion explosion = event.explosion;
        float lightning = explosion.getAdditionStack().getValue(AdditionType.LIGHTNING);
        if (lightning > 4F) {
            if (explosion.random.nextInt(10) < lightning * event.percentage &&
                    explosion.level.getBlockState(event.pos).isAir() &&
                    explosion.level.getBlockState(event.pos.below()).isSolidRender(explosion.level, event.pos.below())) {
                summonLightningBolt(explosion, event.pos);
            }
        }
    }

    @SubscribeEvent
    public static void flameBlockEvent(CustomExplosionBlockBreakEvent event) {
        CustomExplosion explosion = event.explosion;
        float flame = explosion.getAdditionStack().getValue(AdditionType.FLAME);
        if (flame > 0F) {
            if (explosion.random.nextInt(10) < flame * event.percentage &&
                    explosion.level.getBlockState(event.pos).isAir() &&
                    explosion.level.getBlockState(event.pos.below()).isSolidRender(explosion.level, event.pos.below())) {
                explosion.level.setBlockAndUpdate(event.pos, BaseFireBlock.getState(explosion.level, event.pos));
            }
        }
    }

    @SubscribeEvent
    public static void temperatureBlockEvent(CustomExplosionBlockBreakEvent event) {
        CustomExplosion explosion = event.explosion;
        float temperature = explosion.getAdditionStack().getValue(AdditionType.TEMPERATURE);
        if (temperature > 4F) {
            BlockState state = explosion.level.getBlockState(event.pos);
            if (explosion.random.nextInt(10) < temperature * event.percentage && state.is(BlockTags.BASE_STONE_OVERWORLD)) {
                explosion.level.setBlockAndUpdate(event.pos, Fluids.LAVA.defaultFluidState().createLegacyBlock());
            }
        }
    }

    @SubscribeEvent
    public static void doBlockDropEvent(CustomExplosionDoBlockDropEvent event) {
        for (Pair<ItemStack, BlockPos> pair : event.objectArrayList) {
                Block.popResource(event.explosion.level, pair.getSecond(), pair.getFirst());
        }
    }

    @SubscribeEvent
    public static void specialDoBlockDropEvent(CustomExplosionDoBlockDropEvent event) {
        CustomExplosion explosion = event.explosion;
        BlockPos containerPos = null;
        AtomicReference<IItemHandler> container = new AtomicReference<>();
        ItemStack marker = explosion.getAdditionStack().getItems(AdditionType.CONTAINER).isEmpty() ?
                null : explosion.getAdditionStack().getItems(AdditionType.CONTAINER).peek();
        if (marker != null && marker.getItem() instanceof PositionMarkerItem item) {
            if (item.isContainer) {
                BlockPos pos = item.getPos(marker);
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
        for (Pair<ItemStack, BlockPos> pair : event.objectArrayList) {
            if (containerPos != null) {
                ItemStack remain = pair.getFirst();
                if (container.get()!=null) {
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
        }
    }

    private static void summonLightningBolt(CustomExplosion explosion, BlockPos pos) {
        Level level = explosion.level;
        LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(level);
        lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
        Entity source = explosion.getSourceMob();
        lightningbolt.setCause(source instanceof ServerPlayer ? (ServerPlayer) source : null);
        level.addFreshEntity(lightningbolt);
        level.playSound(null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
    }
}
