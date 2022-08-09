package modist.artoftnt.data;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;

public class LanguageGenerator extends LanguageProvider {


    public LanguageGenerator(DataGenerator gen, String locale) {
        super(gen, ArtofTnt.MODID, locale);
    }

    @Override
    protected void addTranslations() {
//        add("itemGroup." + ArtofTnt.ITEM_GROUP.getRecipeFolderName(), "Art of TNT Tools and Additions");
//        add("itemGroup." + ArtofTnt.ITEM_GROUP_FRAME.getRecipeFolderName(), "Art of TNT Frames and Presets");
//        ItemLoader.SIMPLE_ITEMS.forEach((s, i)->add(i.get(), capital(s)));

//        addBlock(BlockLoader.TNT_CLONER);
//        addBlock(BlockLoader.TNT_TURRET);
//        addBlock(BlockLoader.REMOTE_EXPLODER);
//        addItem(ItemLoader.TARGET_MARKER);
//        Arrays.stream(ItemLoader.POSITION_MARKERS).forEach(this::addItem);
//        addItem(ItemLoader.POSITION_CONTAINER_MARKER_2);
//        addItem(ItemLoader.POSITION_CONTAINER_MARKER_3);
        AdditionType.getTypes().forEach(t ->
                add(t.getTranslation(), capital(t.toString())));
    }

    private void addBlock(RegistryObject<Block> b){
        add(b.get(), capital(b.getId().getPath()));
    }

    private void addItem(RegistryObject<Item> b){
        add(b.get(), capital(b.getId().getPath()));
    }

    private String capital(String s){
        StringBuilder ret = new StringBuilder();
        String[] words = s.split("_");
        for(String word: words){
            ret.append(upperFirstLatter(word));
            ret.append(' ');
        }
        ret.deleteCharAt(ret.length()-1);
        return ret.toString();
    }

    public String upperFirstLatter(String letter){
        return letter.substring(0, 1).toUpperCase()+letter.substring(1);
    }

}