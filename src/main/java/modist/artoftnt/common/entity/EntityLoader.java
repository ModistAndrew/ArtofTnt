package modist.artoftnt.common.entity;

import modist.artoftnt.ArtofTnt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityLoader {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, ArtofTnt.MODID);

    @SuppressWarnings("unchecked")
    public static final RegistryObject<EntityType<PrimedTntFrame>>[] PRIMED_TNT_FRAMES = new RegistryObject[4];

    static {
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            PRIMED_TNT_FRAMES[i] = ENTITY_TYPES.register("primed_tnt_frame_"+i,
                    () -> EntityType.Builder.<PrimedTntFrame>of((t, l) -> new PrimedTntFrame(t, l, finalI), MobCategory.MISC).fireImmune()
                            .sized(0.98F, 0.98F).clientTrackingRange(10).updateInterval(10).build("primed_tnt_frame"));
        }
    }
}
