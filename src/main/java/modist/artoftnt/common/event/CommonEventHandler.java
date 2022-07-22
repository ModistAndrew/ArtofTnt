package modist.artoftnt.common.event;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TargetMarkerItem;
import modist.artoftnt.common.item.TntDefuserItem;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {

    @SubscribeEvent
    public static void defuse(AttackEntityEvent event) {
        if(!event.getPlayer().level.isClientSide &&
                event.getPlayer().getMainHandItem().getItem() instanceof TntDefuserItem) {
            if(event.getTarget() instanceof PrimedTntFrame tntFrame) {
                event.getPlayer().getMainHandItem().hurtAndBreak
                        (tntFrame.getWeight(), event.getPlayer(),
                                (p_40858_) -> p_40858_.broadcastBreakEvent
                                        (event.getPlayer().getUsedItemHand()));
                tntFrame.defuse();
            }
        }
    }

    @SubscribeEvent
    public static void setTargetEntity(PlayerInteractEvent.EntityInteract event) {
        if(!event.getPlayer().level.isClientSide && event.getPlayer().isShiftKeyDown() &&
                event.getPlayer().getMainHandItem().getItem() instanceof TargetMarkerItem item) {
            item.saveEntity(event.getPlayer().getMainHandItem(), event.getTarget());
        }
    }
}
