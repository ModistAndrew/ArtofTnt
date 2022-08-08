package modist.artoftnt.common.block;

import modist.artoftnt.common.block.entity.RemoteExploderBlockEntity;
import modist.artoftnt.core.addition.InstabilityHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RemoteExploderBlock extends Block implements EntityBlock {
    public RemoteExploderBlock() {
        super(BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS).noOcclusion());
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide && pHand == InteractionHand.MAIN_HAND && pLevel.getBlockEntity(pPos) instanceof RemoteExploderBlockEntity be) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            if (itemstack.isEmpty() && !be.isEmpty()) {
                if (be.isEmpty()) {
                    return InteractionResult.FAIL;
                }
                pPlayer.setItemInHand(pHand, be.popMarker());
                return InteractionResult.SUCCESS;
            }
            ItemStack tItem = itemstack.copy();
            tItem.setCount(1);
            if (!be.accept(tItem)) {
                return InteractionResult.FAIL;
            }
            be.pushMarker(tItem);
            if(!pPlayer.getAbilities().instabuild){
                itemstack.shrink(1);
                pPlayer.setItemInHand(pHand, itemstack);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            if (pLevel.getBlockEntity(pPos) instanceof RemoteExploderBlockEntity blockEntity) {
                Containers.dropContents(pLevel, pPos, blockEntity.getDrops());
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) {
            if (pLevel.hasNeighborSignal(pPos)) {
                explode(InstabilityHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.hasNeighborSignal(pPos)) {
                explode(InstabilityHelper.signalToMinInstability(pLevel.getBestNeighborSignal(pPos)), pLevel, pPos);
        }
    }

    public void explode(float minInstability, Level pLevel, BlockPos pPos) {
        if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof RemoteExploderBlockEntity be) {
            be.explode(minInstability);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RemoteExploderBlockEntity(pPos, pState);
    }
}
