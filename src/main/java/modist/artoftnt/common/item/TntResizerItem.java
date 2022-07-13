package modist.artoftnt.common.item;

import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class TntResizerItem extends Item {
    public TntResizerItem() {
        super(ItemLoader.getProperty());
    }

    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        if(level.getBlockEntity(blockpos) instanceof TntFrameBlockEntity tntFrameBlockEntity) {
            tntFrameBlockEntity.cycleSize();
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.FAIL;
    }

}
