package modist.artoftnt.client.block.model;

import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
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


    public static void putSpecialItemStackQuads(RenderUtil renderer, ItemStack stack, Addition addition, int index, int slot, boolean up, boolean down) {
        float top = index * 2 + 2 - (index == 7 ? 2 * DELTA : 0F);
        switch (addition.name) {
            case "slime_block" -> {
                renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                        AdditionSlot.getU(slot) + 2, top, AdditionSlot.getV(slot) + 2,
                        SLIME_OUTER, up, down);
                renderer.putCube16(AdditionSlot.getU(slot) + 0.5F, index * 2 + 0.5F, AdditionSlot.getV(slot) + 0.5F,
                        AdditionSlot.getU(slot) + 1.5F, index * 2 + 1.5F, AdditionSlot.getV(slot) + 1.5F,
                        SLIME_INNER, true, true);
            }
            case "honey_block" -> {
                renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                        AdditionSlot.getU(slot) + 2, top, AdditionSlot.getV(slot) + 2,
                        HONEY_OUTER, up, down);
                renderer.putCube16(AdditionSlot.getU(slot) + 0.5F, index * 2 + 0.5F, AdditionSlot.getV(slot) + 0.5F,
                        AdditionSlot.getU(slot) + 1.5F, index * 2 + 1.5F, AdditionSlot.getV(slot) + 1.5F,
                        HONEY_INNER, true, true);
            }
            case "lingering_potion" -> {
                renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                        AdditionSlot.getU(slot) + 2, top, AdditionSlot.getV(slot) + 2,
                        POTION_OUTER, up, down);
                renderer.putCube16(AdditionSlot.getU(slot) + 0.5F, index * 2 + 0.5F, AdditionSlot.getV(slot) + 0.5F,
                        AdditionSlot.getU(slot) + 1.5F, index * 2 + 1.5F, AdditionSlot.getV(slot) + 1.5F,
                        POTION_INNER, true, true, Minecraft.getInstance().getItemColors().getColor(stack, 0));
            }
            case "firework_star" -> {
                renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                        AdditionSlot.getU(slot) + 2, top, AdditionSlot.getV(slot) + 2,
                        FIREWORK_OUTER, up, down);
                renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                        AdditionSlot.getU(slot) + 2, top, AdditionSlot.getV(slot) + 2,
                        FIREWORK_INNER, true, true, Minecraft.getInstance().getItemColors().getColor(stack, 1));
            }
            case "string" -> renderer.putCubeFace16(0, 16, 0, 16, 16, 16, STRINGS[index], Direction.UP);
        }
    }

}
