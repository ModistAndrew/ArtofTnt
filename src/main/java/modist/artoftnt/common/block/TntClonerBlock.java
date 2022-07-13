package modist.artoftnt.common.block;

import modist.artoftnt.common.block.entity.SuckItemBlockEntity;
import modist.artoftnt.common.block.entity.TntClonerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

public class TntClonerBlock extends SuckItemBlock {
    public TntClonerBlock() {
        super(BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS).noOcclusion());
    }

    @Override
    public SuckItemBlockEntity getBlockEntity(BlockPos pPos, BlockState pState) {
        return new TntClonerBlockEntity(pPos, pState);
    }
}
