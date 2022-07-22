package modist.artoftnt.core.explosion.handler;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.event.PrimedTntFrameTickEvent;
import modist.artoftnt.core.explosion.manager.ExplosionParticles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTntFrameEventHandler {
    @SubscribeEvent
    public static void particleEvent(PrimedTntFrameTickEvent event) {
        int particle = (int) event.data.getValue(AdditionType.TNT_PARTICLE);
        ExplosionParticles.getTNTParticles(particle).forEach(p ->
                event.tnt.level.addParticle(p, event.tnt.getX(), event.tnt.getY(), event.tnt.getZ(),
                        1.0D, 0.0D, 0.0D));
    }
}
