package modist.artoftnt.data;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.BlockLoader;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BlockStateGenerator extends BlockStateProvider {


    public BlockStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        super(gen, ArtofTnt.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        getVariantBuilder(BlockLoader.DIMINISHING_LIGHT.get()).forAllStates(s ->
                new ConfiguredModel[]{(new ConfiguredModel(new ModelFile.UncheckedModelFile(
                        "artoftnt:block/diminishing_light"
                )))});
        BlockLoader.SIMPLE_BLOCKS.forEach((s, b)->
                simpleBlock(b.get()));
    }
}