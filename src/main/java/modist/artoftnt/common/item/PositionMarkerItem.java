package modist.artoftnt.common.item;

import modist.artoftnt.core.addition.TntFrameData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        if(pContext.getPlayer()!=null && pContext.getPlayer().isShiftKeyDown()){
            marker.addTagElement("position", NbtUtils.writeBlockPos(blockpos));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag tag = pStack.getTagElement("position");
        if(tag!=null){
            if(tag.contains("entityName")){
                TntFrameData.addTooltip("entityName", tag.getString("entityName"), pTooltipComponents);
            } else {
                BlockPos pos = NbtUtils.readBlockPos(tag);
                TntFrameData.addTooltip("position",
                        pos.getX()+", "+pos.getY()+", "+pos.getZ(), pTooltipComponents);
            }
            Player player = Minecraft.getInstance().player;
            if(pLevel != null && player!=null){
                Vec3 pos = player.position();
                if(getPos(pLevel, pos, pStack)==null) {
                    TntFrameData.addTooltip("out_of_range", null, pTooltipComponents, ChatFormatting.ITALIC);
                }
            }
        }
    }

    @Nullable
    public Vec3 getPos(@Nullable Level level, Vec3 posFrom, ItemStack stack){
        CompoundTag tag = stack.getTagElement("position");
        if(tag==null){
            return null;
        }
        Vec3 posTo = Vec3.atCenterOf(NbtUtils.readBlockPos(tag));
        return checkDistance(posFrom, posTo) ? posTo : null;
    }
    public boolean checkDistance(Vec3 posFrom, Vec3 posTo){
        return getRange().move(posFrom).contains(posTo);
    }

    public AABB getRange(){
        int r = switch (tier){
            case 0->16;
            case 1->64;
            case 2->256;
            default -> 1024;
        };
        return new AABB(-r, -r, -r, r, r, r);
    }
}
