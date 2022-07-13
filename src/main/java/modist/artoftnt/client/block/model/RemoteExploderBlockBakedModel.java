package modist.artoftnt.client.block.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.entity.RemoteExploderBlockEntity;
import modist.artoftnt.common.item.PositionMarkerItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class RemoteExploderBlockBakedModel implements IDynamicBakedModel {

    private final BakedModel existingModel;
    private final BakedModel[] markerModels;
    public static final ResourceLocation[] MARKER_MODEL_LOCATIONS = new ResourceLocation[4];
    static {
        for(int i=0; i<4; i++){
            MARKER_MODEL_LOCATIONS[i] = new ResourceLocation(ArtofTnt.MODID, "special/marker_"+i);
        }
    }

    public RemoteExploderBlockBakedModel(BakedModel existingModel, BakedModel[] markerModels) {
        this.existingModel = existingModel;
        this.markerModels = markerModels;
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        ItemStack[] stacks = extraData.getData(RemoteExploderBlockEntity.MARKERS_MODEL_PROPERTY);
        RenderUtil renderer = new RenderUtil(Transformation.identity());
        renderer.transform(existingModel.getQuads(state, side, rand, extraData));
        if(stacks!=null && side==null) {
            for (int i = 0; i < stacks.length; i++) {
                if(stacks[i]!=null && stacks[i].getItem() instanceof PositionMarkerItem positionMarkerItem) {
                    int tier = positionMarkerItem.tier;
                    renderer.setTransform(getTransformation(i));
                    renderer.transform(markerModels[tier].getQuads(null, null, rand, EmptyModelData.INSTANCE));
                }
            }
        }
        return renderer.getQuads();
    }

    private Transformation getTransformation(int i) {
        Matrix4f mtx = Matrix4f.createTranslateMatrix((i/4)/4F, 0, (i%4)/4F);
        return new Transformation(mtx);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return existingModel.useAmbientOcclusion();
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
        return existingModel.getParticleIcon(EmptyModelData.INSTANCE);
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
