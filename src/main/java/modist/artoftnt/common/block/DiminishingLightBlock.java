package modist.artoftnt.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.Random;

public class DiminishingLightBlock extends LightBlock {
    public DiminishingLightBlock() {
        super(BlockBehaviour.Properties.of(Material.AIR).randomTicks()
                .noDrops().noOcclusion().air().lightLevel(LIGHT_EMISSION));
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (!pLevel.isClientSide) {
            pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
        }
    }
}
