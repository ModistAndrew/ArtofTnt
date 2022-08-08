package modist.artoftnt.core.explosion.handler;

import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import modist.artoftnt.core.explosion.event.CustomExplosionFinishingEvent;
import modist.artoftnt.core.explosion.manager.ExplosionResources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.mineplugin.locusazzurro.pyrotechnicraft.data.ItemRegistry;
import org.mineplugin.locusazzurro.pyrotechnicraft.world.data.FireworkEngine;
import org.mineplugin.locusazzurro.pyrotechnicraft.world.data.FireworkWrapper;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientExplosionEventHandler {
    @SubscribeEvent
    public static void soundEvent(CustomExplosionFinishingEvent event) {
        if(event.explosion.level.isClientSide) {
            CustomExplosion explosion = event.explosion;
            float loudness = event.data.getValue(AdditionType.LOUDNESS);
            int soundType = (int) event.data.getValue(AdditionType.SOUND_TYPE);
            if (!event.data.globalSound()) { //not global
                ExplosionResources.SOUNDS.get(soundType, event.data.tier).ifPresent(t -> explosion.level.playLocalSound(explosion.x, explosion.y, explosion.z, t,
                        SoundSource.BLOCKS, loudness, event.data.sound() ? 1 :
                                (1.0F + (explosion.level.random.nextFloat() - explosion.level.random.nextFloat()) * 0.2F) * 0.7F,
                        false));
            }
        }
    }

    @SubscribeEvent
    public static void particleEvent(CustomExplosionFinishingEvent event) {
        if(event.explosion.level.isClientSide) {
            CustomExplosion explosion = event.explosion;
            ExplosionResources.PARTICLES.get(0, event.data.tier).ifPresent(p -> explosion.level.addParticle(p, explosion.x, explosion.y, explosion.z,
                    0.0D, 0.0D, 0.0D));
        }
    }

    @SubscribeEvent
    public static void fireworkEvent(CustomExplosionFinishingEvent event) {
        if(event.explosion.level.isClientSide) {
            CustomExplosion explosion = event.explosion;
            event.data.getItems(AdditionType.FIREWORK).forEach(itemStack -> {
                CompoundTag compoundtag = itemStack.isEmpty() ? null : itemStack.getTag();
                if (compoundtag != null) {
                    if(itemStack.is(ItemRegistry.FIREWORK_ORB.get())) {
                        pyrotechnicraftFirework(explosion, itemStack);
                    } else {
                        CompoundTag explosions = compoundtag.getCompound("Explosion");
                        compoundtag.remove("Explosion");
                        ListTag list = new ListTag();
                        list.add(explosions);
                        compoundtag.put("Explosions", list);
                        Vec3 vec3 = explosion.getSource() == null ? Vec3.ZERO : explosion.getSource().getDeltaMovement();
                        explosion.level.createFireworks(explosion.x, explosion.y, explosion.z, vec3.x, vec3.y, vec3.z, compoundtag);
                    }
                }
            });
        }
    }

    private static void pyrotechnicraftFirework(CustomExplosion explosion, ItemStack stack) {
        FireworkEngine.createFirework(explosion.level, explosion.getPosition(), explosion.getVec(),
                FireworkWrapper.wrapSingleFireworkExplosion(stack));
    }

}