package modist.artoftnt.common.sound;

import modist.artoftnt.ArtofTnt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundEventRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ArtofTnt.MODID);
    @SuppressWarnings("unchecked")
    public static RegistryObject<SoundEvent>[] TNT_SOUND = new RegistryObject[8];
    static {
        for(int i=0; i<8; i++){
            int finalI = i;
            TNT_SOUND[i]  = SOUNDS.register("tnt_sound_"+i,
                    () -> new SoundEvent(new ResourceLocation(ArtofTnt.MODID, "tnt_sound_"+ finalI)));
        }
    }
}
