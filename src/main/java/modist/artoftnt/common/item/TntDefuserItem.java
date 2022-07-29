package modist.artoftnt.common.item;

import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TntDefuserItem extends Item { //see CommonEventHandler
    public TntDefuserItem() {
        super(ItemLoader.getProperty().durability(384));
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return enchantment.category.equals(EnchantmentCategory.BREAKABLE)
                || enchantment.equals(Enchantments.SILK_TOUCH);
    }

    @Override
    public int getEnchantmentValue() {
        return 1;
    }

    @Override
    public boolean mineBlock(ItemStack pStack, Level pLevel, BlockState pState, BlockPos pPos, LivingEntity pEntityLiving) {
        if (!pLevel.isClientSide && pLevel.getBlockEntity(pPos) instanceof TntFrameBlockEntity be) {
            pStack.hurtAndBreak((int)be.getData().getWeight(), pEntityLiving, (p_40992_) -> p_40992_.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }
}
