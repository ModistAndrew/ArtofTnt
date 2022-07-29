package modist.artoftnt.client.block.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionSlot;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TntFrameBlockBakedModel implements IDynamicBakedModel {

    private static final float DELTA = 0.001F; //fix z-fighting
    private final BakedModel existingModel;

    public TntFrameBlockBakedModel(BakedModel existingModel) {
        this.existingModel = existingModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        if(side!=null){
            return Collections.emptyList();
        }
        @NotNull TntFrameData data;
        TntFrameData data1 = extraData.getData(TntFrameBlockEntity.ADDITIONS_MODEL_PROPERTY);
        if(data1 != null){
            data =data1;
        } else {
            if(state!=null && state.getBlock() instanceof TntFrameBlock block){
                data = new TntFrameData(block.tier);
            } else {
                data = new TntFrameData(0);
            }
        }
        BakeModelRenderer renderer = getRenderUtil(state, data); //initialize
        Direction[] directions = new Direction[]{
                Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, null
        };
        if (data.disguise != null) { //disguise
                Arrays.stream(directions).forEach(d -> renderer.transformBlock(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(data.disguise).
                        getQuads(data.disguise, d, rand, EmptyModelData.INSTANCE), data.disguise));
            return renderer.getQuads();
        }
        Arrays.stream(directions).forEach(d -> {
            renderer.transform(existingModel.getQuads(state, d, rand, extraData)); //frame
        });
        for (int i = 0; i < 18; i++) {
            putItemStackQuads(renderer, data.getItemStacks(i), i);
        }
        putSideQuads(data.fixed, renderer, data.getItems(AdditionType.SHAPE).isEmpty() ? null : Addition.fromItem(data.getItems(AdditionType.SHAPE).peek().getItem()),
                data.getWeight(), data.getValue(AdditionType.INSTABILITY), data.tier);
        return renderer.getQuads();
    }
    private void putSideQuads(boolean fixed, BakeModelRenderer renderer, Addition shape, float weight, float instability, int tier) {
        renderer.putCube16(0, 0, 0, 16, 16, 16, getWeightTexture(weight), false, false);
        renderer.putCube16(0, 0, 0, 16, 16, 16, getInstabilityTexture(instability), false, false);
        renderer.putCubeFace16(0, 16 - DELTA, 0, 16, 16 - DELTA, 16,
                fixed ? TextureLoader.FIXED_TOP[tier] : TextureLoader.TOP[tier], Direction.UP);
    }

    private ResourceLocation getInstabilityTexture(float instability) {
        return TextureLoader.INSTABILITY[getInstabilityTextureId(instability)];
    }

    private ResourceLocation getWeightTexture(float weight) {
        return TextureLoader.WEIGHT[getWeightTextureId(weight)];
    }

    public static int getInstabilityTextureId(float instability) {
        return instability > 7F ? 7 : (int) instability;
    }

    public static int getWeightTextureId(float weight) {
        return weight > 7F ? 7 : (int) weight;
    }

    private void putItemStackQuads(BakeModelRenderer renderer, Stack<ItemStack> stacks, int slot) {
        Stack<ItemStack> stacks1 = AdditionStack.AdditionTypeStorage.getSplit(stacks);
        for (int i = 0; i < stacks1.size(); i++) {
            putSingleItemStackQuads(renderer, stacks1.get(i), i, slot,
                    i == stacks1.size() - 1 || Addition.fromItem(stacks1.get(i + 1).getItem()).specialRenderer,
                    i != 0 && Addition.fromItem(stacks1.get(i - 1).getItem()).specialRenderer);
        }
        if (slot == 17 && stacks.isEmpty()) { //empty shape special
            putSingleItemStackQuads(renderer, null, 0, slot, true, true);
        }
    }

    private void putSingleItemStackQuads(BakeModelRenderer renderer, @Nullable ItemStack stack, int index, int slot, boolean up, boolean down) {
        if (stack == null) {
            renderer.putSpecialItemStackQuads(null, null, index, slot);
            return;
        }
        Addition addition = Addition.fromItem(stack.getItem());
        if (!addition.specialRenderer && addition.type.slot.index < 16) {
            renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                    AdditionSlot.getU(slot) + 2, index * 2 + 2 - (index == 7 ? 2 * DELTA : 0), AdditionSlot.getV(slot) + 2,
                    addition.resourceLocation, up, down);
        } else {
            renderer.putSpecialItemStackQuads(stack, addition, index, slot);
        }
    }

    private BakeModelRenderer getRenderUtil(BlockState state, TntFrameData data) {
        if (data == null) {
            return new BakeModelRenderer(Transformation.identity());
        }
        float d = data.getDeflation();
        float scale = data.size;
        Matrix4f mtx = Matrix4f.createTranslateMatrix(d, d, d);
        mtx.multiply(Matrix4f.createScaleMatrix(scale, scale, scale));
        return new BakeModelRenderer(new Transformation(mtx));
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false; //shade may be weird
    }

    @Override
    public boolean isGui3d() {
        return existingModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return existingModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return getParticleIcon(EmptyModelData.INSTANCE);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(IModelData modelData) {
        return existingModel.getParticleIcon(EmptyModelData.INSTANCE);
    }

    @Override
    public ItemOverrides getOverrides() {
        return existingModel.getOverrides();
    }
}
