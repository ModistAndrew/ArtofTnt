package modist.artoftnt.common.item;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.client.block.entity.TntFrameBEWLR;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.TntFrameBlock;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

public class ItemLoader {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ArtofTnt.MODID);

    public static final RegistryObject<Item>[] TNT_FRAMES = new RegistryObject[4];
    public static final RegistryObject<Item> TNT_DISPENSER = ITEMS.register("tnt_dispenser", TntDispenserItem::new);
    public static final RegistryObject<Item> TNT_SHAPER = ITEMS.register("tnt_shaper", TntShaperItem::new);
    public static final RegistryObject<Item> TNT_RESIZER = ITEMS.register("tnt_resizer", TntResizerItem::new);
    public static final RegistryObject<Item> TNT_DEFUSER = ITEMS.register("tnt_defuser", TntDefuserItem::new);
    public static final RegistryObject<Item> REMOTE_EXPLODER = fromBlock(BlockLoader.REMOTE_EXPLODER);
    public static final RegistryObject<Item> TNT_TURRET = fromBlock(BlockLoader.TNT_TURRET);
    public static final RegistryObject<Item> TNT_CLONER = fromBlock(BlockLoader.TNT_CLONER);
    public static final RegistryObject<Item>[] POSITION_MARKERS = new RegistryObject[4];
    public static final RegistryObject<Item>[] POSITION_CONTAINER_MARKERS = new RegistryObject[4];


    static {
        for(int i=0; i<4; i++){
            int finalI = i;
            TNT_FRAMES[i] = ITEMS.register("tnt_frame_"+i, () -> new TntFrameItem(finalI));
            POSITION_MARKERS[i] = ITEMS.register("position_marker_"+i, () ->new PositionMarkerItem(finalI, false));
            POSITION_CONTAINER_MARKERS[i] = ITEMS.register("position_container_marker_"+i,
                    () ->new PositionMarkerItem(finalI, true));
        }
    }

    public static Item.Properties getProperty(){
       return new Item.Properties().tab(ArtofTnt.ITEM_GROUP);
    }

    public static final BlockEntityWithoutLevelRenderer BEWLR_INSTANCE = new TntFrameBEWLR();

    private static RegistryObject<Item> fromBlock(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), getProperty()));
    }

}
