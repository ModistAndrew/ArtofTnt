package modist.artoftnt.core.addition;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.core.addition.data.AdditionManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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
    private static final Map<Item, Addition> ITEM_MAP = new HashMap<>(); //item 2 addition

    private static final String ADDITION = "tnt_frame_additions/";

    public Addition(String name, AdditionType type, float increment, int minTier, int maxCount, float weight, float instability, boolean specialRenderer, Item item){
        this.item = item;
        this.name = name;
        this.type = type;
        this.increment = increment;
        this.minTier = minTier;
        this.maxCount = maxCount;
        this.weight = weight;
        this.instability = instability;
        this.specialRenderer = specialRenderer;
        this.texture = createResourceLocation(name);
    }

    public static void register(String name, AdditionManager.AdditionWrapper wrapper){
        register(name, AdditionType.fromString(wrapper.type), wrapper.increment, wrapper.minTier, wrapper.maxCount, wrapper.weight,
                wrapper.instability, wrapper.specialRenderer, ForgeRegistries.ITEMS.getValue(new ResourceLocation(wrapper.item)));
    }

    public static void register(String name, AdditionType type, float increment, int minTier, int maxCount, float weight, float instability, boolean specialRenderer, Item item){
        Addition addition = new Addition(name, type, increment, minTier, maxCount, weight, instability, specialRenderer, item);
        ITEM_MAP.put(addition.item, addition);
    }

    public static ResourceLocation createResourceLocation(String name) {
        return new ResourceLocation(ArtofTnt.MODID, ADDITION+name);
    }

    public static boolean contains(Item item) {
        return ITEM_MAP.containsKey(item);
    }

    public static Addition fromItem(Item item) {
        return ITEM_MAP.get(item);
    }

}