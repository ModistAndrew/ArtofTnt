package modist.artoftnt.client.block;

import modist.artoftnt.ArtofTnt;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class TextureLoader {
    public static final Set<ResourceLocation> TEXTURES = new HashSet<>();
    public static final ResourceLocation DEFAULT_SHAPE = registerTexture("default_shape");
    public static final ResourceLocation TNT_SHAPER = registerTexture("tnt_shaper");
    public static final ResourceLocation[] FIXED_TOP = new ResourceLocation[4];
    public static final ResourceLocation[] TOP = new ResourceLocation[4];
    public static final ResourceLocation[] WEIGHT = new ResourceLocation[8];
    public static final ResourceLocation[] INSTABILITY = new ResourceLocation[8];
    public static final ResourceLocation[] BASE_TOP =new ResourceLocation[16];
    public static final ResourceLocation[] BASE_TNT =new ResourceLocation[4];
    private static final String SPECIAL = "special/";
    static {
        for (int i = 0; i < 8; i++) {
            WEIGHT[i] = registerTexture("weight_" + i);
            INSTABILITY[i] = registerTexture("instability_" + i);
        }
        for (int i = 0; i < 16; i++) {
            BASE_TOP[i] = registerTexture("base_top_" + i);
        }
        for (int i = 0; i < 4; i++) {
            FIXED_TOP[i] = registerTexture("fixed_top_" + i);
            TOP[i] = registerTexture("top_"+i);
            BASE_TNT[i] = registerTexture("base_tnt_" + i);
        }
    }
    private static ResourceLocation registerTexture(String name) {
        ResourceLocation resourceLocation = getLocation(name);
        TEXTURES.add(resourceLocation);
        return resourceLocation;
    }

    public static ResourceLocation getLocation(String name) {
        return new ResourceLocation(ArtofTnt.MODID, SPECIAL+name);
    }

}