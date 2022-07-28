package modist.artoftnt.client.block.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionSlot;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class AdditionSpecialRenderer {
    private static final float DELTA = 0.0001F; //fix z-fighting
    public static void putSpecialItemStackQuads(BakeModelRenderer renderer, Random random, @Nullable ItemStack stack, @Nullable Addition addition, int index, int slot) {
        if (stack == null || addition == null) { //default side
            renderer.putCube16(0, 0, 0, 16, 16, 16, TextureLoader.DEFAULT_SHAPE, false, false);
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
