package modist.artoftnt.client.sound;

import modist.artoftnt.ArtofTnt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundEventRegistry {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ArtofTnt.MODID);
    public static RegistryObject<SoundEvent> TEST_SOUND = SOUNDS.register("test", () -> {
        return new SoundEvent(new ResourceLocation(ArtofTnt.MODID, "test"));
    });
}
