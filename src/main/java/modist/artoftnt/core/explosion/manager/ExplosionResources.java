package modist.artoftnt.core.explosion.manager;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExplosionResources<T> {
    private final List<T> resources = new ArrayList<>();

    public static final ExplosionResources<ParticleOptions> PARTICLES = new ExplosionResources<>();
    public static final ExplosionResources<ParticleOptions> TNT_PARTICLES = new ExplosionResources<>();
    public static final ExplosionResources<SoundEvent> SOUNDS = new ExplosionResources<>();
    public static final ExplosionResources<SoundEvent> TNT_SOUNDS = new ExplosionResources<>();

    public Optional<T> get(int value){
        if(value < 0 || value >= resources.size() || resources.get(value)==null){
            return Optional.empty();
        }
        return Optional.of(resources.get(value));
    }

    @SafeVarargs
    private void register(T... values){
        resources.addAll(List.of(values));
    }

    static {
        PARTICLES.register(ParticleTypes.BUBBLE, ParticleTypes.EXPLOSION_EMITTER, ParticleTypes.EXPLOSION, ParticleTypes.ANGRY_VILLAGER);
        TNT_PARTICLES.register(ParticleTypes.ASH, ParticleTypes.SMOKE, ParticleTypes.EXPLOSION, ParticleTypes.ANGRY_VILLAGER);
        SOUNDS.register(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundEvents.GENERIC_EXPLODE, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundEvents.WITHER_DEATH);
        TNT_SOUNDS.register(SoundEvents.EGG_THROW, SoundEvents.TNT_PRIMED, SoundEvents.VILLAGER_NO, SoundEvents.CHICKEN_DEATH);
    }
}
