package modist.artoftnt.client.block.model;

import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.common.block.entity.RemoteExploderBlockEntity;
import modist.artoftnt.common.item.PositionMarkerItem;
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
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RemoteExploderBlockBakedModel implements IDynamicBakedModel {

    private final BakedModel existingModel;
    private final BakedModel[] markerModels;
    public static final ResourceLocation[] MARKER_MODEL_LOCATIONS = new ResourceLocation[4];
    static {
        for(int i=0; i<4; i++){
            MARKER_MODEL_LOCATIONS[i] = TextureLoader.getLocation("marker_"+i);
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
        BakeModelRenderer renderer = new BakeModelRenderer(Transformation.identity());
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
        int a = i/4;
        int b = i%4;
        Matrix4f mtx = Matrix4f.createTranslateMatrix(a/4F, 0, b/4F);
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
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return existingModel.getParticleIcon(EmptyModelData.INSTANCE);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull IModelData modelData) {
        return existingModel.getParticleIcon(EmptyModelData.INSTANCE);
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return existingModel.getOverrides();
    }
}
