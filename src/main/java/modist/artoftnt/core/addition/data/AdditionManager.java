package modist.artoftnt.core.addition.data;

import com.google.gson.*;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Type;
import java.util.Map;

@Mod.EventBusSubscriber
public class AdditionManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    public static final AdditionManager INSTANCE = new AdditionManager();

    public AdditionManager() {
        super(GSON, "tnt_frame_additions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        pObject.forEach((r, j)-> {
            AdditionWrapper wrapper = GSON.fromJson(j, AdditionWrapper.class);
            Addition.register(r, wrapper);
        });
    }

    @SubscribeEvent
    public static void onEvent(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static class AdditionWrapper {
        public String type;
        public String item;
        public float increment;
        public int minTier;
        public int maxCount;
        public float weight;
        public float instability;
        public boolean specialRenderer;
    }

}
