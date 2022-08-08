package modist.artoftnt.core.explosion.manager;

import modist.artoftnt.common.sound.SoundEventRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ExplosionResources<T> {
    private final List<T> resources = new ArrayList<>();

    public static final ExplosionResources<ParticleOptions> PARTICLES = new ExplosionResources<>();
    public static final ExplosionResources<ParticleOptions> TNT_PARTICLES = new ExplosionResources<>();
    public static final ExplosionResources<SoundEvent> SOUNDS = new ExplosionResources<>();
    public static final ExplosionResources<SoundEvent> TNT_SOUNDS = new ExplosionResources<>();

    public Optional<T> get(int value, int tier){
        if(value==0){
            return resources.get(tier)==null ? Optional.empty() : Optional.of(resources.get(tier));
        }
        value += 3;
        if(value < 0 || value >= resources.size() || resources.get(value)==null){
            return Optional.empty();
        }
        return Optional.of(resources.get(value));
    }

    @SafeVarargs
    private void register(T... values){
        resources.addAll(Arrays.asList(values));
    }

    static {
        PARTICLES.register(ParticleTypes.BUBBLE_POP, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, ParticleTypes.ANGRY_VILLAGER);
        TNT_PARTICLES.register(null, null, null, null);
        TNT_PARTICLES.register(ParticleTypes.SMOKE, ParticleTypes.COMPOSTER, ParticleTypes.FLAME, ParticleTypes.SNOWFLAKE,
                ParticleTypes.ASH, ParticleTypes.FALLING_WATER, ParticleTypes.FALLING_LAVA, ParticleTypes.GLOW);
        SOUNDS.register(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundEvents.GENERIC_EXPLODE, SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundEvents.WITHER_DEATH);
        for(int i=0; i<8; i++){
            SOUNDS.register(SoundEventRegistry.TNT_SOUND[i].get());
        }
        TNT_SOUNDS.register(null, SoundEvents.UI_BUTTON_CLICK, SoundEvents.TNT_PRIMED, SoundEvents.ANVIL_PLACE);
    }
}
