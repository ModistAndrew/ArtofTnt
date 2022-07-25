package modist.artoftnt.common.block;

import com.google.common.collect.Lists;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.item.TntDefuserItem;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.addition.InstabilityHelper;
import modist.artoftnt.core.explosion.manager.ExplosionSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
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

//TODO should side be rendered? bottom up
public class TntFrameBlock extends TntBlock implements EntityBlock {
    public final int tier;

    public TntFrameBlock(int tier) {
        super(BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS).noOcclusion());
        this.tier = tier;
    }

    //TODO when addition changed notify
    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) {
                tryExplode(InstabilityHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos, null);
        }
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
    } //TODO:not available

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        tryExplode(InstabilityHelper.ENTITY_ON_BLOCK_MIN_INSTABILITY, pLevel, pPos,
                pEntity instanceof LivingEntity le ? le : null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onProjectileHit(Level pLevel, BlockState pState, BlockHitResult pHit, Projectile pProjectile) {
        tryExplode(InstabilityHelper.PROJECTILE_HIT_BLOCK_MIN_INSTABILITY, pLevel, pHit.getBlockPos(),
                pProjectile.getOwner() instanceof LivingEntity le ? le : null);
    }

    public boolean tryExplode(float minInstability, Level pLevel, BlockPos pPos, @Nullable LivingEntity pEntity) {
        if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
            if (be.getInstability() >= minInstability) {
                PrimedTntFrame tnt = new PrimedTntFrame(be.getDataTag(), pLevel, pPos.getX(), pPos.getY() + be.getDeflation(), pPos.getZ(), pEntity, tier);
                tnt.shoot(0, 1, 0, 1.0F, 1.0F); //default:up
                pLevel.addFreshEntity(tnt);
                float loudness = be.getData().getValue(AdditionType.LOUDNESS);
                int soundType = (int) be.getData().getValue(AdditionType.TNT_SOUND_TYPE);
                ExplosionSounds.getSoundEvents(soundType).forEach(t -> //TODO client; tnt
                        pLevel.playLocalSound(tnt.getX(), tnt.getY(), tnt.getZ(), t,
                                SoundSource.BLOCKS, pLevel.getRandom().nextFloat() * loudness,
                                (1.0F + (pLevel.random.nextFloat() - pLevel.random.nextFloat()) * 0.2F) * 0.7F,
                                false));
                pLevel.gameEvent(pEntity, GameEvent.PRIME_FUSE, pPos);
                pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        return false;
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

    //TODO drop is strange!
    //deal with creative player block drop
    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof TntFrameBlockEntity tntFrameBlockEntity) {
            if (!pLevel.isClientSide) {
                if (pPlayer.isCreative()) {
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
    public static ItemStack dropFrame(boolean shouldFix, TntFrameData data){
        if(shouldFix) {
            data.fixed = true;
        }
        ItemStack is = new ItemStack(ItemLoader.TNT_FRAMES[data.tier].get());
        TntFrameItem.putTntFrameData(is, data);
        return is;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if(pStack.getItem() instanceof TntFrameItem item) {
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

    /*@Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }*/

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pHand == InteractionHand.MAIN_HAND && pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be && !be.fixed()) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            if (Addition.contains(itemstack.getItem())) {
                ItemStack tItem = itemstack.copy();
                tItem.setCount(1);
                if (be.add(tItem)) {
                    if (!pPlayer.isCreative()) {
                        itemstack.shrink(1);
                    }
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
                return InteractionResult.CONSUME;
            }
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        ItemStack stack = new ItemStack(this);
        TntFrameItem.putTntFrameData(stack, new TntFrameData(tier));
        pItems.add(stack);
        //TODO more demo
        //TODO set tag? how? new ItemStack breakpoint
        //TODO command give?
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        if(pStack.getItem() instanceof TntFrameItem item) {
            TntFrameData data = item.getTntFrameData(pStack);
            if (pLevel != null) {
                data.addText(pTooltip);
            }
        }
    }

}
