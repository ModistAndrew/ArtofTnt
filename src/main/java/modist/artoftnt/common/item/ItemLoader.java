package modist.artoftnt.common.item;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.BlockLoader;
import net.minecraft.ResourceLocationException;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unchecked")
public class ItemLoader {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ArtofTnt.MODID);

    public static final RegistryObject<Item>[] TNT_FRAMES = new RegistryObject[4];
    public static final RegistryObject<Item> TNT_DISPENSER = ITEMS.register("tnt_dispenser", TntDispenserItem::new);
    public static final RegistryObject<Item> TNT_SHAPER = ITEMS.register("tnt_shaper", TntShaperItem::new);
    public static final RegistryObject<Item> TNT_RESIZER = ITEMS.register("tnt_resizer", TntResizerItem::new);
    public static final RegistryObject<Item> TNT_DEFUSER = ITEMS.register("tnt_defuser", TntDefuserItem::new);
    public static final RegistryObject<Item> TNT_FIREWORK_STAR = ITEMS.register("tnt_firework_star", TntFireworkStarItem::new);
    public static final RegistryObject<Item> REMOTE_EXPLODER = fromBlock(BlockLoader.REMOTE_EXPLODER);
    public static final RegistryObject<Item> TNT_TURRET = fromBlock(BlockLoader.TNT_TURRET);
    public static final RegistryObject<Item> TNT_CLONER = fromBlock(BlockLoader.TNT_CLONER);
    public static final RegistryObject<Item>[] POSITION_MARKERS = new RegistryObject[4];
    public static final RegistryObject<Item> POSITION_CONTAINER_MARKER_2 = ITEMS.register("position_container_marker_2",
            () -> new PositionMarkerItem(2, true));
    public static final RegistryObject<Item> POSITION_CONTAINER_MARKER_3 = ITEMS.register("position_container_marker_3",
            () -> new PositionMarkerItem(3, true));

    public static final RegistryObject<Item> TARGET_MARKER =ITEMS.register("target_marker", () -> new TargetMarkerItem(2));
    public static final HashMap<String, RegistryObject<Item>> SIMPLE_ITEMS = new HashMap<>();
    public static final HashMap<String, RegistryObject<Item>> SIMPLE_BLOCK_ITEMS = new HashMap<>();
    static{
        simple("sound_type_modifier");
        simple("tnt_particle_modifier");
        simple("tnt_motion_modifier");
        simple("tnt_damage_modifier");
        simple("punch_modifier");
        simple("horizontal_piston");
        simple("vertical_piston");
        simple("tnt_punch_modifier");
        simple("tnt_draw_modifier");
        simple("gunpowder_up");
        simple("gunpowder_down");
        simple("gunpowder_north");
        simple("gunpowder_south");
        simple("gunpowder_east");
        simple("gunpowder_west");
        simple("gunpowder_2");
        simple("gunpowder_3");
        simple("gunpowder_4");
        simple("piercing_modifier");
        simple("block_drop_2");
        simple("block_drop_3");
        simple("block_drop_4");
        simple("lightning_modifier");
        simple("string_1");
        simple("string_2");
        BlockLoader.SIMPLE_BLOCKS.forEach((s, b)->
                SIMPLE_BLOCK_ITEMS.put(s, ITEMS.register(s, () -> new BlockItem(b.get(), getProperty()))));
    }

    static {
        for(int i=0; i<4; i++){
            int finalI = i;
            TNT_FRAMES[i] = ITEMS.register("tnt_frame_"+i, () -> new TntFrameItem(finalI));
            POSITION_MARKERS[i] = ITEMS.register("position_marker_"+i, () ->new PositionMarkerItem(finalI, false));
        }
    }

    public static Item.Properties getProperty(){
       return new Item.Properties().tab(ArtofTnt.ITEM_GROUP);
    }

    private static RegistryObject<Item> fromBlock(RegistryObject<Block> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), getProperty()));
    }

    private static RegistryObject<Item> simple(String name) {
        RegistryObject<Item> ret = ITEMS.register(name, () -> new Item(getProperty()));
        SIMPLE_ITEMS.put(name, ret);
        return ret;
    }

}
