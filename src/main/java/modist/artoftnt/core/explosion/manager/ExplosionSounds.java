package modist.artoftnt.core.explosion.manager;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ExplosionSounds {
    public static Set<SoundEvent> getSoundEvents(int value){
        return Collections.singleton(SoundEvents.CHICKEN_DEATH);
    }

    public static Set<SoundEvent> getTntSoundEvents(int value){
        return Collections.singleton(SoundEvents.TNT_PRIMED);
    }

}
