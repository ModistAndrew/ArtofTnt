package modist.artoftnt.common.item;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.client.block.entity.TntFrameBEWLR;
import modist.artoftnt.common.block.BlockLoader;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemLoader {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ArtofTnt.MODID);

    public static final RegistryObject<Item>[] TNT_FRAMES = new RegistryObject[4];
    public static final RegistryObject<Item> TNT_DISPENSER = ITEMS.register("tnt_dispenser", TntDispenserItem::new);
    public static final RegistryObject<Item> TNT_SHAPER = ITEMS.register("tnt_shaper", TntShaperItem::new); //TODO block color
    public static final RegistryObject<Item> TNT_RESIZER = ITEMS.register("tnt_resizer", TntResizerItem::new);
    public static final RegistryObject<Item> TNT_DEFUSER = ITEMS.register("tnt_defuser", TntDefuserItem::new); //TODO creative, drop bug, silk touch
    public static final RegistryObject<Item> TNT_FIREWORK_STAR = ITEMS.register("tnt_firework_star", TntFireworkStarItem::new);
    public static final RegistryObject<Item> REMOTE_EXPLODER = fromBlock(BlockLoader.REMOTE_EXPLODER);
    public static final RegistryObject<Item> TNT_TURRET = fromBlock(BlockLoader.TNT_TURRET);
    public static final RegistryObject<Item> TNT_CLONER = fromBlock(BlockLoader.TNT_CLONER);
    public static final RegistryObject<Item>[] POSITION_MARKERS = new RegistryObject[4];
    public static final RegistryObject<Item>[] POSITION_CONTAINER_MARKERS = new RegistryObject[4];
    public static final RegistryObject<Item>[] TARGET_MARKERS = new RegistryObject[4];


    static {
        for(int i=0; i<4; i++){
            int finalI = i;
            TNT_FRAMES[i] = ITEMS.register("tnt_frame_"+i, () -> new TntFrameItem(finalI));
            POSITION_MARKERS[i] = ITEMS.register("position_marker_"+i, () ->new PositionMarkerItem(finalI, false));
            POSITION_CONTAINER_MARKERS[i] = ITEMS.register("position_container_marker_"+i,
                    () -> new PositionMarkerItem(finalI, true));
            TARGET_MARKERS[i] = ITEMS.register("target_marker_"+i,
                    () -> new TargetMarkerItem(finalI));
        }
    }

    public static Item.Properties getProperty(){
       return new Item.Properties().tab(ArtofTnt.ITEM_GROUP);
    }

    private static RegistryObject<Item> fromBlock(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), getProperty()));
    }

}
