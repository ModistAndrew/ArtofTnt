package modist.artoftnt.common.block;

import modist.artoftnt.common.block.entity.SuckItemBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;
import java.util.function.ToIntFunction;

public class DiminishingLightBlock extends Block {
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = (p_153701_) -> {
        return p_153701_.getValue(LEVEL);
    };

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_153687_) {
        p_153687_.add(LEVEL);
    }

    public VoxelShape getShape(BlockState p_153668_, BlockGetter p_153669_, BlockPos p_153670_, CollisionContext p_153671_) {
        return Shapes.empty();
    }

    public boolean propagatesSkylightDown(BlockState p_153695_, BlockGetter p_153696_, BlockPos p_153697_) {
        return true;
    }
    public RenderShape getRenderShape(BlockState p_153693_) {
        return RenderShape.INVISIBLE;
    }

    public float getShadeBrightness(BlockState p_153689_, BlockGetter p_153690_, BlockPos p_153691_) {
        return 1.0F;
    }

    public DiminishingLightBlock() {
        super(BlockBehaviour.Properties.of(Material.AIR)
                .noDrops().noOcclusion().air().lightLevel(LIGHT_EMISSION));
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(15)));
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
            pLevel.scheduleTick(pPos, this, 20);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        if (!pLevel.isClientSide) {
            if(pState.getValue(LEVEL) > 1){
                pLevel.setBlockAndUpdate(pPos, pState.setValue(LEVEL, pState.getValue(LEVEL)-1));
                pLevel.scheduleTick(pPos, this, 2);
            } else {
                pLevel.setBlockAndUpdate(pPos, Blocks.AIR.defaultBlockState());
            }
        }
    }
}
