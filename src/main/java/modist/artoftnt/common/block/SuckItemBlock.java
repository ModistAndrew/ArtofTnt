package modist.artoftnt.common.block;

import modist.artoftnt.common.block.entity.RemoteExploderBlockEntity;
import modist.artoftnt.common.block.entity.SuckItemBlockEntity;
import modist.artoftnt.core.addition.AdditionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class SuckItemBlock extends Block implements EntityBlock {
    public SuckItemBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!pOldState.is(pState.getBlock())) {
            if (pLevel.hasNeighborSignal(pPos)) {
                if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof SuckItemBlockEntity be) {
                    be.activated();
                }
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (pLevel.hasNeighborSignal(pPos)) {
            if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof SuckItemBlockEntity be) {
                be.activated();
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            if (pLevel.getBlockEntity(pPos) instanceof SuckItemBlockEntity blockEntity) {
                Containers.dropContents(pLevel, pPos, blockEntity.getDrops());
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    public abstract SuckItemBlockEntity getBlockEntity(BlockPos pPos, BlockState pState);

    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return getBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return (l, s, t, be) -> {
            if (be instanceof SuckItemBlockEntity suckItemBlockEntity && be.getType() == pBlockEntityType) {
                suckItemBlockEntity.tick();
            }
        };
    }

}
