package modist.artoftnt.core.addition;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.JsonUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class AdditionManager extends SimpleJsonResourceReloadListener implements IForgeRegistryEntry<AdditionManager> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    public static final AdditionManager INSTANCE = new AdditionManager();

    public AdditionManager() {
        super(GSON, "tnt_frame_additions");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.jsonElementMap = pObject; //save
        load();
    }

    private AdditionManager load(){
        Addition.clear(); //clear first!
        jsonElementMap.forEach((r, j)-> {
            AdditionWrapper wrapper = GSON.fromJson(j, AdditionWrapper.class);
            Addition.register(r, wrapper);
        });
        return this;
    }

    public static final Codec<AdditionManager> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.STRING.fieldOf("data").forGetter(AdditionManager::save))
                    .apply(instance, AdditionManager::load));

    private Map<ResourceLocation, JsonElement> jsonElementMap = new HashMap<>();

    private String save() {
        return JsonUtil.GSON.toJson(jsonElementMap);
    }

    private static AdditionManager load(String json) {
        INSTANCE.jsonElementMap = JsonUtil.GSON.fromJson(json, TypeToken.getParameterized(Map.class, ResourceLocation.class, JsonElement.class).getType());
        INSTANCE.load();
        return INSTANCE;
    }

    @Override
    public AdditionManager setRegistryName(ResourceLocation name) {
        return INSTANCE;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public Class<AdditionManager> getRegistryType() {
        return AdditionManager.class;
    }

    public static final ResourceLocation NAME = new ResourceLocation(ArtofTnt.MODID, "addition_manager");
    public static final ResourceKey<Registry<AdditionManager>> REGISTRY_KEY = ResourceKey.createRegistryKey(NAME);

    public static final DeferredRegister<AdditionManager> REGISTER = DeferredRegister.create(AdditionManager.REGISTRY_KEY, ArtofTnt.MODID);
    static{
        REGISTER.makeRegistry(AdditionManager.class,
                () -> new RegistryBuilder<AdditionManager>().disableSaving().dataPackRegistry(AdditionManager.CODEC, AdditionManager.CODEC));
        REGISTER.register("addition_manager", ()->INSTANCE);
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
