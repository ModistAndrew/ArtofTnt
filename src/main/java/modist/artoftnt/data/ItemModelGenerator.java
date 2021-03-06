package modist.artoftnt.data;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.item.ItemLoader;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ItemModelGenerator extends ItemModelProvider {

    public ItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, ArtofTnt.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ItemLoader.SIMPLE_ITEMS.values().forEach(i -> {
            singleTexture(i.get().getRegistryName().getPath(),
                    mcLoc("item/generated"), "layer0", modLoc("item/"+i.get().getRegistryName().getPath()));
        });
        ItemLoader.SIMPLE_BLOCK_ITEMS.forEach((s, i)->
                withExistingParent(s, modLoc("block/"+s)));
    }
}