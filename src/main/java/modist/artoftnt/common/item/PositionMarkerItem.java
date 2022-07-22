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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
            if(tag.contains("entityClass")){
                TntFrameData.addTooltip("entityClass", tag.getString("entityClass"), pTooltipComponents);
                TntFrameData.addTooltip("UUID", tag.getString("UUID"), pTooltipComponents);
            } else {
                TntFrameData.addTooltip("position", NbtUtils.readBlockPos(tag), pTooltipComponents);
            }
        }
    }

    @Nullable
    public Vec3 getPos(Vec3 posFrom, ItemStack stack){
        CompoundTag tag = stack.getTagElement("position");
        if(tag==null){
            return null;
        }
        Vec3 posTo = Vec3.atCenterOf(NbtUtils.readBlockPos(tag));
        return checkDistance(posFrom, posTo) ? posTo : null;
    }

    public boolean checkDistance(Vec3 pos1, Vec3 pos2){
        return true;
    }

    public AABB getRange(){
        return new AABB(-64, -64, -64, 64, 64, 64);
    }
}
