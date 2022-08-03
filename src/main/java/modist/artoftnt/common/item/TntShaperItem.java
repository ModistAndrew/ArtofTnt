package modist.artoftnt.common.item;

import modist.artoftnt.client.block.entity.TntFrameBEWLR;
import modist.artoftnt.common.block.ModBlockTags;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.core.addition.TntFrameData;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class TntShaperItem extends Item {
    public TntShaperItem() {
        super(ItemLoader.getProperty().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        ItemStack shaper = pContext.getItemInHand();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        BlockState disguise = getState(shaper);
        if (level.getBlockEntity(blockpos) instanceof TntFrameBlockEntity tntFrameBlockEntity && disguise!=null) {
            tntFrameBlockEntity.toggleDisguise(disguise);
        } else {
            if (accept(level,blockpos,blockstate)) { //only allow full block
                shaper.addTagElement("blockState", NbtUtils.writeBlockState(blockstate));
            } else {
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag tag = pStack.getTagElement("blockState");
        if(tag!=null){
            TntFrameData.addTooltip("blockState", NbtUtils.readBlockState(tag).getBlock(), pTooltipComponents);
        }
    }

    private boolean accept(Level level, BlockPos pos, BlockState state) {
        return state.is(ModBlockTags.DISGUISE);
    }

    @Nullable
    public BlockState getState(ItemStack stack){
        CompoundTag tag = stack.getTagElement("blockState");
        return tag==null ? null : NbtUtils.readBlockState(tag);
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

}