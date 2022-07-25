package modist.artoftnt.common.event;

import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TargetMarkerItem;
import modist.artoftnt.common.item.TntDefuserItem;
import modist.artoftnt.common.item.TntDispenserItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {

    @SubscribeEvent
    public static void defuse(AttackEntityEvent event) {
        if (!event.getPlayer().level.isClientSide) {
            if (event.getTarget() instanceof PrimedTntFrame tntFrame) {
                ItemStack stack = event.getPlayer().getMainHandItem();
                if (stack.getItem() instanceof TntDefuserItem) {
                    stack.hurtAndBreak
                            (tntFrame.getWeight() * 10, event.getPlayer(),
                                    (p_40858_) -> p_40858_.broadcastBreakEvent
                                            (event.getPlayer().getUsedItemHand()));
                    tntFrame.defuse(EnchantmentHelper.getItemEnchantmentLevel(
                            Enchantments.SILK_TOUCH, stack) <= 0 && !event.getPlayer().getAbilities().instabuild);
                } else if (stack.getItem() instanceof TntDispenserItem) {
                    int knockBack = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.KNOCKBACK, stack);
                    if(knockBack > 0) {
                        event.getPlayer().getMainHandItem().hurtAndBreak
                                (tntFrame.getWeight() * 10, event.getPlayer(),
                                        (p_40858_) -> p_40858_.broadcastBreakEvent
                                                (event.getPlayer().getUsedItemHand()));
                        tntFrame.knocked(event.getPlayer(), knockBack);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void setTargetEntity(PlayerInteractEvent.EntityInteract event) {
        if (!event.getPlayer().level.isClientSide && event.getPlayer().isShiftKeyDown() &&
                event.getPlayer().getMainHandItem().getItem() instanceof TargetMarkerItem item) {
            item.saveEntity(event.getPlayer().getMainHandItem(), event.getTarget());
        }
    }
}
