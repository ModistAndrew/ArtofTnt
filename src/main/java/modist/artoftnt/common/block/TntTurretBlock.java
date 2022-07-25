package modist.artoftnt.common.block;

import modist.artoftnt.common.block.entity.CoolDownBlockEntity;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TntTurretBlock extends CoolDownBlock { //TODO bounding box
    public TntTurretBlock() {
        super(BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS).noOcclusion());
    }

    @Override
    public CoolDownBlockEntity getBlockEntity(BlockPos pPos, BlockState pState) {
        return new TntTurretBlockEntity(pPos, pState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.create(0, 0, 0, 1, 1/16F, 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        System.out.printf("pos:%s\n", pHit.getLocation());
        if (pHand == InteractionHand.MAIN_HAND && pLevel.getBlockEntity(pPos) instanceof TntTurretBlockEntity blockEntity) {
            ItemStack itemstack = pPlayer.getItemInHand(pHand);
            blockEntity.tryPutTnt(pHit.getLocation(), itemstack);
        } //TODO
        return super.use(pState, pLevel, pPos, pPlayer, pHand, pHit);
    }
}