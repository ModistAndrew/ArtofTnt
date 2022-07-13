package modist.artoftnt.common.event;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TntDefuserItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {

    @SubscribeEvent
    public static void registerDispenser(AttackEntityEvent event) {
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
}
