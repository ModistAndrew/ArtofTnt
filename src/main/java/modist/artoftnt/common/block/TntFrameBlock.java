package modist.artoftnt.common.block;

import com.google.common.collect.Lists;
import modist.artoftnt.ArtofTntConfig;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.item.TntDefuserItem;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.common.loot.functions.TntFrameFunctionWrapper;
import modist.artoftnt.common.loot.functions.TntFrameFunctions;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.InstabilityHelper;
import modist.artoftnt.core.addition.TntFrameData;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class TntFrameBlock extends TntBlock implements EntityBlock {
    public final int tier;

    public TntFrameBlock(int tier) {
        super(BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS).noOcclusion().randomTicks());
        this.tier = tier;
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        tryExplode(InstabilityHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos, null);
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        tryExplode(InstabilityHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos, null);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TntFrameBlockEntity(pPos, pState, tier);
    }

    @Override
    public void onCaughtFire(BlockState state, Level pLevel, BlockPos pPos, @Nullable net.minecraft.core.Direction face, @Nullable LivingEntity pEntity) {
        tryExplode(InstabilityHelper.FIRE_MIN_INSTABILITY, pLevel, pPos, pEntity);
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        tryExplode(InstabilityHelper.ENTITY_HIT_BLOCK_MIN_INSTABILITY, pLevel, pPos,
                pEntity instanceof LivingEntity le ? le : null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity) {
        tryExplode(InstabilityHelper.ENTITY_HIT_BLOCK_MIN_INSTABILITY, pLevel, pPos,
                pEntity instanceof LivingEntity le ? le : null);
    }

    @Override
    public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        tryExplode(InstabilityHelper.PROJECTILE_HIT_BLOCK_MIN_INSTABILITY, pLevel, pHit.getBlockPos(),
                pProjectile.getOwner() instanceof LivingEntity le ? le : null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        tryExplode(InstabilityHelper.RANDOM_BLOCK_MIN_INSTABILITY, pLevel, pPos, null);
    }


    public void tryExplode(float minInstability, Level pLevel, BlockPos pPos, @Nullable LivingEntity pEntity) {
        if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
            if (be.getInstability() >= minInstability) {
                PrimedTntFrame tnt = new PrimedTntFrame(be.getDataTag(), pLevel, pPos.getX(), pPos.getY() + be.getDeflation(), pPos.getZ(), pEntity, tier);
                tnt.shoot(0, 1, 0, 1.0F, 1.0F); //default:up
                pLevel.addFreshEntity(tnt);
                pLevel.gameEvent(pEntity, GameEvent.PRIME_FUSE, pPos);
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        this.wasExploded(level, pos, explosion); //first explode, or can't get BE
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    @Override
    public void wasExploded(Level pLevel, BlockPos pPos, Explosion pExplosion) {
        tryExplode(InstabilityHelper.EXPLODED_MIN_INSTABILITY, pLevel, pPos, pExplosion.getSourceMob());
    }

    //deal with creative player block drop
    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof TntFrameBlockEntity tntFrameBlockEntity) {
            if (!pLevel.isClientSide) {
                if (pPlayer.getAbilities().instabuild) {
                    Block.popResource(pLevel, pPos, dropFrame(false, tntFrameBlockEntity.getData()));
                } else if (!(pPlayer.getMainHandItem().getItem() instanceof TntDefuserItem)) {
                    tryExplode(InstabilityHelper.BREAK_BLOCK_INSTABILITY, pLevel, pPos, pPlayer);
                }
            }
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        if (pBuilder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof TntFrameBlockEntity be) {
            return pBuilder.getParameter(LootContextParams.TOOL).getItem() instanceof TntDefuserItem ?
                    Lists.newArrayList(dropFrame(EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.SILK_TOUCH, pBuilder.getParameter(LootContextParams.TOOL)) <= 0, be.getData())) : be.getDrops();
        }
        return super.getDrops(pState, pBuilder);
    }

    public static ItemStack dropFrame(boolean shouldFix, TntFrameData data) {
        boolean temp = data.fixed;
        if (shouldFix) {
            data.fixed = true;
        }
        ItemStack is = new ItemStack(ItemLoader.TNT_FRAMES[data.tier].get());
        TntFrameItem.putTntFrameData(is, data);
        data.fixed = temp;
        return is;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.getItem() instanceof TntFrameItem item) {
            CompoundTag compoundtag = item.getTntFrameDataTag(pStack);
            if (pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
                be.readDataTag(compoundtag);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
            float f = be.getDeflation();
            return Shapes.create(f, f, f, 1 - f, 1 - f, 1 - f);
        }
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand == InteractionHand.MAIN_HAND && pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be && !be.fixed()) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            if (Addition.contains(itemstack.getItem())) {
                ItemStack tItem = itemstack.copy();
                tItem.setCount(1);
                if (pPlayer.isCrouching()) {
                    if (be.draw(tItem)) {
                        if (!pPlayer.getAbilities().instabuild) {
                            ItemStack stack1 = itemstack.copy();
                            stack1.setCount(1);
                            pPlayer.getInventory().add(stack1);
                        }
                        return InteractionResult.sidedSuccess(pLevel.isClientSide);
                    }
                    return InteractionResult.CONSUME;
                } else {
                    AdditionStack.AdditionResult result = be.add(tItem);
                    if (ArtofTntConfig.ENABLE_ADDITION_REPLY.get() && pPlayer instanceof ServerPlayer player && result.reply() != null) {
                        player.sendMessage(result.reply(), Util.NIL_UUID);
                    }
                    if (result.success()) {
                        if (!pPlayer.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                        return InteractionResult.sidedSuccess(pLevel.isClientSide);
                    }
                    return InteractionResult.CONSUME;
                }
            }
        }
        ItemStack itemstack = pPlayer.getItemInHand(pHand); //super, but may not set air
        if (!itemstack.is(Items.FLINT_AND_STEEL) && !itemstack.is(Items.FIRE_CHARGE)) {
            return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
        } else {
            onCaughtFire(pState, pLevel, pPos, pHit.getDirection(), pPlayer);
            Item item = itemstack.getItem();
            if (!pPlayer.isCreative()) {
                if (itemstack.is(Items.FLINT_AND_STEEL)) {
                    itemstack.hurtAndBreak(1, pPlayer, (p_57425_) -> {
                        p_57425_.broadcastBreakEvent(pHand);
                    });
                } else {
                    itemstack.shrink(1);
                }
            }
            pPlayer.awardStat(Stats.ITEM_USED.get(item));
            return InteractionResult.sidedSuccess(pLevel.isClientSide);
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        ItemStack stack = new ItemStack(this);
        for (TntFrameFunctionWrapper function : TntFrameFunctions.FUNCTIONS[tier]) {
            pItems.add(function.apply(stack.copy()));
        }
        super.fillItemCategory(pTab, pItems);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if (pStack.getItem() instanceof TntFrameItem item) {
            TntFrameData data = item.getTntFrameData(pStack);
            if (pLevel != null) {
                data.addText(pTooltip);
            }
        }
    }

}