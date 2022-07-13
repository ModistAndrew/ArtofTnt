package modist.artoftnt.common.block;

import modist.artoftnt.common.block.entity.SuckItemBlockEntity;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

public class TntTurretBlock extends SuckItemBlock { //TODO bounding box
    public TntTurretBlock() {
        super(BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS).noOcclusion());
    }

    @Override
    public SuckItemBlockEntity getBlockEntity(BlockPos pPos, BlockState pState) {
        return new TntTurretBlockEntity(pPos, pState);
    }
}