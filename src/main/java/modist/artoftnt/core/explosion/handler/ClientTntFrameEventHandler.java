package modist.artoftnt.core.explosion.handler;

import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.event.PrimedTntFrameTickEvent;
import modist.artoftnt.core.explosion.manager.ExplosionResources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTntFrameEventHandler {
    @SubscribeEvent
    public static void particleEvent(PrimedTntFrameTickEvent event) {
        if(event.tnt.level.isClientSide) {
            int particle = (int) event.data.getValue(AdditionType.TNT_PARTICLE);
            ExplosionResources.TNT_PARTICLES.get(particle, event.data.tier).ifPresent(p -> event.tnt.level.addParticle(p, event.tnt.getX(), event.tnt.getY(), event.tnt.getZ(),
                    0.0D, 0.0D, 0.0D));
        }
    }
}
