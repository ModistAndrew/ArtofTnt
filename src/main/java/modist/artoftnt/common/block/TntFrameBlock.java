package modist.artoftnt.common.block;

import com.google.common.collect.Lists;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.item.TntDefuserItem;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
            if (pLevel.hasNeighborSignal(pPos)) {
                explode(AdditionHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos, null);
            }
        }
    }

    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.hasNeighborSignal(pPos)) {
            explode(AdditionHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos, null);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TntFrameBlockEntity(pPos, pState, tier);
    }

    @Override
    public void onCaughtFire(BlockState state, Level pLevel, BlockPos pPos, @Nullable net.minecraft.core.Direction face, @Nullable LivingEntity pEntity) {
        explode(AdditionHelper.FIRE_MIN_INSTABILITY, pLevel, pPos, pEntity);
    } //TODO:not available

    public boolean explode(float minInstability, Level pLevel, BlockPos pPos, @Nullable LivingEntity pEntity) {
        if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
            if (be.getInstability() >= minInstability) {
                PrimedTntFrame tnt = new PrimedTntFrame(be.getDataTag(), pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), pEntity, tier);
                tnt.shoot(0, 1, 0, 1.0F, 1.0F); //default:up
                pLevel.addFreshEntity(tnt);
                pLevel.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
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
        explode(AdditionHelper.EXPLOSION_MIN_INSTABILITY, pLevel, pPos, pExplosion.getSourceMob());
    }

    //TODO drop is strange!
    //deal with creative player block drop
    @Override
    public void playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof TntFrameBlockEntity tntFrameBlockEntity) {
            if (!pLevel.isClientSide && pPlayer.isCreative()) {
                Containers.dropContents(pLevel, pPos, NonNullList.of(ItemStack.EMPTY, getDrops(pState, new LootContext.Builder((ServerLevel) pLevel)
                        .withParameter(LootContextParams.BLOCK_ENTITY, tntFrameBlockEntity)
                        .withParameter(LootContextParams.TOOL, pPlayer.getMainHandItem())).toArray(new ItemStack[0])));
            }
        }
        super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        if (pBuilder.getParameter(LootContextParams.BLOCK_ENTITY) instanceof TntFrameBlockEntity be) {
            return pBuilder.getParameter(LootContextParams.TOOL).getItem() instanceof TntDefuserItem ?
                    Lists.newArrayList(getDrop(true, be.getData())) : be.getDrops();
        }
        return super.getDrops(pState, pBuilder);
    }

    public static ItemStack getDrop(boolean shouldFix, TntFrameData data){
        if(shouldFix) {
            data.fixed = true;
        }
        ItemStack is = new ItemStack(ItemLoader.TNT_FRAMES[data.tier].get());
        is.addTagElement("tntFrameData", data.serializeNBT());
        return is;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        CompoundTag compoundtag = pStack.getTagElement("tntFrameData");
        if (pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
            be.readDataTag(compoundtag);
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
        return Shapes.empty(); //return block() will cause strange bugs
        //TODO: still strange when placed
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
        stack.addTagElement("tntFrameData", (new TntFrameData(tier)).serializeNBT());
        pItems.add(stack);
        //TODO more demo
        //TODO set tag? how? new ItemStack breakpoint
        //TODO command give?
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable BlockGetter pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
        super.appendHoverText(pStack, pLevel, pTooltip, pFlag);
        CompoundTag compoundtag = pStack.getTagElement("tntFrameData");
        TntFrameData data = new TntFrameData(tier, compoundtag);
        if(pLevel!=null) {
            data.addText(pTooltip);
        }
    }

}
