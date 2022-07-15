package modist.artoftnt.common.item;

import modist.artoftnt.common.block.entity.TntFrameData;
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

import javax.annotation.Nullable;
import java.util.List;

public class PositionMarkerItem extends Item {
public final int tier;
public final boolean isContainer;

    public PositionMarkerItem(int tier, boolean isContainer) {
        super(ItemLoader.getProperty());
        this.tier = tier;
        this.isContainer = isContainer;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) { //always mark
        ItemStack marker = pContext.getItemInHand();
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if(pContext.getPlayer().isShiftKeyDown()){
            marker.addTagElement("position", NbtUtils.writeBlockPos(blockpos));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag tag = pStack.getTagElement("position");
        if(tag!=null){
            TntFrameData.addTooltip("position", NbtUtils.readBlockPos(tag), pTooltipComponents);
        }
    }

    public BlockPos getPos(ItemStack stack){
        CompoundTag tag = stack.getTagElement("position");
        return tag==null ? null : NbtUtils.readBlockPos(tag);
    }
}
