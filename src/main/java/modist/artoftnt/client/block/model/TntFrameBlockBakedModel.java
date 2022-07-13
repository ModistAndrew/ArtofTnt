package modist.artoftnt.client.block.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.core.addition.Addition;
import modist.artoftnt.core.addition.AdditionHelper;
import modist.artoftnt.core.addition.AdditionSlot;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

//TODO special render when completely empty
public class TntFrameBlockBakedModel implements IDynamicBakedModel {

    private static final float DELTA = 0.001F; //fix z-fighting
    private final BakedModel existingModel;

    public TntFrameBlockBakedModel(BakedModel existingModel) {
        this.existingModel = existingModel;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        TntFrameData data = extraData.getData(TntFrameBlockEntity.ADDITIONS_MODEL_PROPERTY);
        RenderUtil renderer = getRenderUtil(state, data); //initialize
        if (side != null) { //deal with cull face
            if (data == null) { //default
                putDefaultQuads(renderer, side, 0);
                return renderer.getQuads();
            }
            if (data.disguise != null) { //disguise
                if(data.size == 1F) {
                    if (data.disguise.getBlock() == Blocks.AIR||data.disguise.getBlock() instanceof TntFrameBlock) { //no recursion
                        putDefaultQuads(renderer, side, data.tier);
                    } else {
                        renderer.transform(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(data.disguise).
                                getQuads(data.disguise, side, rand, EmptyModelData.INSTANCE), data.disguise);
                    }
                }
                return renderer.getQuads();
            }
            return renderer.getQuads();
        }
        Direction[] directions = new Direction[]{
                Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, null
        };
        if(data==null){ //ignore
            return renderer.getQuads();
        }
        if (data.disguise != null) { //disguise
            if(data.size < 1F) {
                Arrays.stream(directions).forEach(d -> {
                    if (data.disguise.getBlock() == Blocks.AIR) {
                        putDefaultQuads(renderer, d, data.tier);
                    } else {
                        renderer.transform(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(data.disguise).
                                getQuads(data.disguise, d, rand, EmptyModelData.INSTANCE), data.disguise);
                    }
                });
            }
            return renderer.getQuads();
        }
        Arrays.stream(directions).forEach(d -> {
            renderer.transform(existingModel.getQuads(state, d, rand, extraData)); //frame
        });
        for (int i = 0; i < 17; i++) { //16 bottoms and one top
            putItemStackQuads(renderer, data.getItemStacks(i), i);
        }
        putSideQuads(data.fixed, renderer, data.getItems(AdditionType.SHAPE).isEmpty() ? null : Addition.fromItem(data.getItems(AdditionType.SHAPE).peek().getItem()),
                data.getWeight(), data.getValue(AdditionType.INSTABILITY));
        return renderer.getQuads();
    }

    private void putDefaultQuads(RenderUtil renderer, Direction side, int tier) {
        if (side != null) {
            switch (side) {
                case UP -> renderer.putCubeFace16(0, 0, 0, 16, 16, 16, AdditionSpecialRenderer.TNT_FRAME_TOP[tier], side);
                case DOWN -> renderer.putCubeFace16(0, 0, 0, 16, 16, 16, AdditionSpecialRenderer.TNT_FRAME_BOTTOM[tier], side);
                default -> renderer.putCubeFace16(0, 0, 0, 16, 16, 16, AdditionSpecialRenderer.TNT_FRAME_SIDE[tier], side);
            }
        }
    }

    private void putSideQuads(boolean fixed, RenderUtil renderer, Addition shape, float weight, float instability) {
        renderer.putCube16(0, 0, 0, 16, 16, 16, shape == null ?
                AdditionSpecialRenderer.DEFAULT_SHAPE : shape.texture, false, false);
        renderer.putCube16(0, 0, 0, 16, 16, 16, getWeightTexture(weight), false, false);
        renderer.putCube16(0, 0, 0, 16, 16, 16, getInstabilityTexture(instability), false, false);
        renderer.putCubeFace16(0, 16-DELTA, 0, 16, 16-DELTA, 16,
                fixed ? AdditionSpecialRenderer.FIXED_TOP : AdditionSpecialRenderer.TOP, Direction.UP);
    }

    private ResourceLocation getInstabilityTexture(float instability) {
        return AdditionSpecialRenderer.INSTABILITY[AdditionHelper.getInstabilityTextureId(instability)];
    }

    private ResourceLocation getWeightTexture(float weight) {
        return AdditionSpecialRenderer.WEIGHT[AdditionHelper.getWeightTextureId(weight)];
    }

    private void putItemStackQuads(RenderUtil renderer, Stack<ItemStack> stacks, int slot) {
        Stack<ItemStack> stacks1 = new Stack<>();
        for (ItemStack stack : stacks) {
            ItemStack stack1 = stack.copy();
            stack1.setCount(1);
            for (int j = 0; j < stack.getCount(); j++) {
                stacks1.push(stack1);
            }
        }
        for (int i = 0; i < stacks1.size(); i++) {
            putSingleItemStackQuads(renderer, stacks1.get(i), i, slot,
                    i == stacks1.size() - 1 || Addition.fromItem(stacks1.get(i + 1).getItem()).specialRenderer,
                    i != 0 && Addition.fromItem(stacks1.get(i - 1).getItem()).specialRenderer);
        }
    }

    private void putSingleItemStackQuads(RenderUtil renderer, ItemStack stack, int index, int slot, boolean up, boolean down) {
        Addition addition = Addition.fromItem(stack.getItem());
        if (!addition.specialRenderer) {
            renderer.putCube16(AdditionSlot.getU(slot), index * 2, AdditionSlot.getV(slot),
                    AdditionSlot.getU(slot) + 2, index * 2 + 2 - (index == 7 ? 2 * DELTA : 0),AdditionSlot.getV(slot) + 2,
                    addition.texture, up, down);
        } else {
            renderer.putSpecialItemStackQuads(stack, addition, index, slot, up, down);
        }
    }

    private RenderUtil getRenderUtil(BlockState state, TntFrameData data) {
        if (data == null) {
            if (state != null && state.getBlock() instanceof TntFrameBlock tfb) {
                return new RenderUtil(Transformation.identity());
            } else {
                return new RenderUtil(Transformation.identity());
            }
        }
        float d = data.getDeflation();
        float scale = data.size;
        Matrix4f mtx = Matrix4f.createTranslateMatrix(d, d, d);
        mtx.multiply(Matrix4f.createScaleMatrix(scale, scale, scale));
        return new RenderUtil(new Transformation(mtx));
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
