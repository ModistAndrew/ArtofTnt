package modist.artoftnt.core.addition;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.item.ItemLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

public class Addition {
    public final String name;
    public final AdditionType type;
    public final float increment; //may <0 when type is instability
    public final int minTier;
    public final int maxCount;
    public final float weight;
    public final float instability; //= 0 when type is instability
    public final boolean specialRenderer; //for potion, firework, fuse...will ignore texture
    //not for shape!
    public final Item item;
    public final ResourceLocation texture;
    public static final List<Addition> LIST = new ArrayList<>();
    private static final Map<Item, Addition> ITEM_MAP = new HashMap<>(); //item 2 addition
    public static final Set<ResourceLocation> TEXTURES = new HashSet<>();

    private static final String ADDITION = "addition/";

    private Addition(AdditionType type, float increment, int minTier, int maxCount, float weight, float instability, boolean specialRenderer, Item item){
        this.item = item;
        this.name = this.item.asItem().getRegistryName().getPath();
        this.type = type;
        this.increment = increment;
        this.minTier = minTier;
        this.maxCount = maxCount;
        this.weight = weight;
        this.instability = instability;
        this.specialRenderer = specialRenderer;
        this.texture = createResourceLocation(name);
    }

    public static ResourceLocation createResourceLocation(String name) {
        return new ResourceLocation(ArtofTnt.MODID, ADDITION+name);
    }

    public static void register(AdditionType type, float increment, int minTier, int maxCount, float weight, float instability, boolean specialRenderer, Item item){
        Addition addition = new Addition(type, increment, minTier, maxCount, weight, instability, specialRenderer, item);
        LIST.add(addition);
        ITEM_MAP.put(addition.item, addition);
        if(!specialRenderer) {
            TEXTURES.add(addition.texture);
        }
    }

    public static boolean contains(Item item) {
        return ITEM_MAP.containsKey(item);
    }

    public static Addition fromItem(Item item) {
        return ITEM_MAP.get(item);
    }

    static {
        register(AdditionType.FIREWORK, 1, 1, 8, 0, 0, true, Items.FIREWORK_STAR);
        register(AdditionType.POTION, 1, 1, 8, 0, 0, true, Items.LINGERING_POTION);
        register(AdditionType.FUSE, 100, 1, 8, 0, 0, true, Items.STRING);
        register(AdditionType.RANGE, 1, 1, 16, 10, 1, false, Items.GUNPOWDER);
        register(AdditionType.STRENGTH, 1, 1, 16, 10, 0, false, Items.OBSIDIAN);
        register(AdditionType.PUNCH, 1, 1, 16, 0, 1, false, Items.PISTON);
        register(AdditionType.DAMAGE, 1, 1, 16, 0, 0, false, Items.QUARTZ);
        register(AdditionType.VELOCITY, 1, 1, 16, 0, 0, false, Items.FIREWORK_ROCKET);
        register(AdditionType.SHAPE, 1, 1, 1, 0, 0, false, Items.GOLDEN_APPLE);
        register(AdditionType.PIERCING, 1, 1, 8, 0, 0, false, Items.ENDER_PEARL);
        register(AdditionType.TEMPERATURE, 1, 1, 8, 0, 0, false, Items.LAVA_BUCKET);
        register(AdditionType.LIGHTNING, 1, 1, 8, 0, 0, false, Items.COPPER_BLOCK);
        register(AdditionType.FLAME, 1, 1, 8, 0, 0, false, Items.BLAZE_POWDER);
        register(AdditionType.DROP, 1, 1, 8, 0, 0, false, Items.SAND);
        register(AdditionType.CONTAINER, 1, 1, 8, 0, 0, false, ItemLoader.POSITION_CONTAINER_MARKERS[0].get());
        register(AdditionType.LIGHTNESS, 0.4F, 1, 8, 0, 0, false, Items.FEATHER);
        register(AdditionType.ELASTICITY, 0.4F, 1, 8, 0, 0, true, Items.SLIME_BLOCK);
        register(AdditionType.STICKINESS, 0.4F, 1, 8, 0, 0, true, Items.HONEY_BLOCK);
    }

}
