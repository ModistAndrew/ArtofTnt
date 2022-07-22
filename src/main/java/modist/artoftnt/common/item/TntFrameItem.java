package modist.artoftnt.common.item;

import modist.artoftnt.client.block.entity.TntFrameBEWLR;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.entity.TntFrameData;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TntFrameItem extends BlockItem {
    public final int tier;

    public TntFrameItem(int tier) {
        super(BlockLoader.TNT_FRAMES[tier].get(), ItemLoader.getProperty());
        this.tier = tier;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return TntFrameBEWLR.BEWLR_INSTANCE;
            }
        });
    }

    public TntFrameData getTntFrameData(ItemStack stack){
        return new TntFrameData(tier, getTntFrameDataTag(stack));
    }

    @Nullable
    public CompoundTag getTntFrameDataTag(ItemStack stack){ //mostly for primed tnt frame
        return stack.getTagElement("tntFrameData");
    }

    //notice that when data is empty, should put no tag instead of an empty tag with name "tntFrameData" to avoid
    //mismatch between item stacks with no tag and those with empty tag
    public static void putTntFrameData(ItemStack stack, TntFrameData data){
        if(!data.isEmpty()) {
            stack.addTagElement("tntFrameData", data.serializeNBT());
        }
    }

    @Override
    protected boolean canPlace(BlockPlaceContext pContext, BlockState pState) {
        Player player = pContext.getPlayer();
        ItemStack stack = pContext.getItemInHand();
        TntFrameData data = this.getTntFrameData(stack);
        float f = data.getDeflation();
        VoxelShape shape = Shapes.create(f, f, f, 1 - f, 1 - f, 1 - f);
        CollisionContext collisioncontext = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        return (!this.mustSurvive() || pState.canSurvive(pContext.getLevel(), pContext.getClickedPos())) &&
                isUnobstructed(shape, pContext.getLevel(), pState, pContext.getClickedPos(), collisioncontext);
    }

    private boolean isUnobstructed(VoxelShape voxelshape, Level level, BlockState pState, BlockPos pPos, CollisionContext pContext) {
        return voxelshape.isEmpty() ||
                level.isUnobstructed(null, voxelshape.move(pPos.getX(), pPos.getY(), pPos.getZ()));
    }
}
