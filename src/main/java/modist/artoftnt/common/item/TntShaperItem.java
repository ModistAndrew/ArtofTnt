package modist.artoftnt.common.item;

import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeMod;

import javax.annotation.Nullable;
import java.util.List;

public class TntShaperItem extends Item {
    public TntShaperItem() {
        super(ItemLoader.getProperty());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        float attrib = (float)pPlayer.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        attrib = pPlayer.isCreative() ? attrib : attrib - 0.5F;
        if(pPlayer.pick(attrib, 0.0F, false).getType() == HitResult.Type.MISS) {
            itemstack.removeTagKey("blockState");
            return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
        }
        return InteractionResultHolder.pass(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        ItemStack shaper = pContext.getItemInHand();
        BlockPos blockpos = pContext.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        CompoundTag tag = shaper.getTagElement("blockState");
        if (level.getBlockEntity(blockpos) instanceof TntFrameBlockEntity tntFrameBlockEntity) {
            tntFrameBlockEntity.toggleDisguise(tag == null ? Blocks.AIR.defaultBlockState() : NbtUtils.readBlockState(shaper.getTagElement("blockState")));
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
        return state.isCollisionShapeFullBlock(level, pos) &&
                !ItemBlockRenderTypes.canRenderInLayer(state, RenderType.translucent());
    }

}