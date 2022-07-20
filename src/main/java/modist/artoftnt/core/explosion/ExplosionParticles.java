package modist.artoftnt.core.explosion;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.Set;
public class ExplosionParticles {
    public static Set<ParticleOptions> getParticles(int value){
        return Collections.singleton(ParticleTypes.ANGRY_VILLAGER);
    }

    public static Set<ParticleOptions> getTNTParticles(int value){
        return Collections.singleton(ParticleTypes.COMPOSTER);
    }

}
