package modist.artoftnt.client.block.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class AdditionSpecialRenderer {
    private static final float DELTA = 0.0001F; //fix z-fighting
    public static final Set<ResourceLocation> TEXTURES = new HashSet<>();
    private static final ResourceLocation SLIME_OUTER = registerTexture("slime_outer");
    private static final ResourceLocation SLIME_INNER = registerTexture("slime_inner");
    private static final ResourceLocation HONEY_OUTER = registerTexture("honey_outer");
    private static final ResourceLocation HONEY_INNER = registerTexture("honey_inner");
    private static final ResourceLocation POTION_OUTER = registerTexture("potion_outer");
    private static final ResourceLocation POTION_INNER = registerTexture("potion_inner");
    private static final ResourceLocation FIREWORK_OUTER = registerTexture("firework_outer");
    private static final ResourceLocation FIREWORK_INNER = registerTexture("firework_inner");
    private static final ResourceLocation[] STRINGS = new ResourceLocation[8];
    public static final ResourceLocation DEFAULT_SHAPE = registerTexture("default_shape");
    public static final ResourceLocation FIXED_TOP = registerTexture("fixed_top");
    public static final ResourceLocation TOP = registerTexture("top");
    public static final ResourceLocation[] WEIGHT = new ResourceLocation[8];
    public static final ResourceLocation[] INSTABILITY = new ResourceLocation[8];
    public static final ResourceLocation[] TNT_FRAME_SIDE = new ResourceLocation[4];
    public static final ResourceLocation[] TNT_FRAME_TOP = new ResourceLocation[4];
    public static final ResourceLocation[] TNT_FRAME_BOTTOM = new ResourceLocation[4];

    static {
        for (int i = 0; i < 8; i++) {
            STRINGS[i] = registerTexture("string_" + i);
            WEIGHT[i] = registerTexture("weight_" + i);
            INSTABILITY[i] = registerTexture("instability_" + i);
        }
        for (int i = 0; i < 4; i++) {
            TNT_FRAME_SIDE[i] = registerTexture("tnt_frame_side_" + i);
            TNT_FRAME_TOP[i] = registerTexture("tnt_frame_top_" + i);
            TNT_FRAME_BOTTOM[i] = registerTexture("tnt_frame_bottom_" + i);
        }
    }


    private static ResourceLocation registerTexture(String name) {
        ResourceLocation resourceLocation = Addition.createResourceLocation(name);
        TEXTURES.add(resourceLocation);
        return resourceLocation;
    }


    public static void putSpecialItemStackQuads(BakeModelRenderer renderer, Random random, @Nullable ItemStack stack, @Nullable Addition addition, int index, int slot, boolean up, boolean down) {
        if (stack == null || addition == null) { //default side
            renderer.putCube16(0, 0, 0, 16, 16, 16, DEFAULT_SHAPE, false, false);
            return;
        }
        if (addition.type.slot.index < 16) {
            Matrix4f mtx = Matrix4f.createScaleMatrix(0.125F, 0.125F-DELTA, 0.125F);
            mtx.translate(new Vector3f(AdditionSlot.getU(slot)/16F, (index * 2+DELTA)/16F, AdditionSlot.getV(slot)/16F));
            Transformation temp = renderer.globalTransform;
            renderer.setTransform(temp.compose(new Transformation(mtx)));
            renderer.transformItem(Minecraft.getInstance().getModelManager().getModel(addition.resourceLocation).getQuads(
                    null, null, random, EmptyModelData.INSTANCE), stack);
            Arrays.stream(Direction.values()).forEach(d -> renderer.transformItem(Minecraft.getInstance().getModelManager().getModel(addition.resourceLocation).getQuads(
                    null, d, random, EmptyModelData.INSTANCE), stack)); //all faces
            renderer.setTransform(temp); //pop
        } else if (addition.type.slot.index == 16) {
            renderer.putCubeFace16(0, 16, 0, 16, 16, 16,
                    addition.appendIndex(index), Direction.UP);
        } else if (addition.type.slot.index == 17) {
            renderer.putCube16(0, 0, 0, 16, 16, 16, addition.resourceLocation, false, false);
        }
    }

}
