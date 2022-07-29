package modist.artoftnt.core.addition;

import modist.artoftnt.ArtofTnt;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class Addition {
    public final ResourceLocation name;
    public final AdditionType type;
    public final float increment; //may <0 when type is instability
    public final int minTier;
    public final int maxCount;
    public final float weight;
    public final float instability; //= 0 when type is instability
    public final boolean specialRenderer; //for potion, firework, fuse...will ignore texture
    //not for shape!
    public final Item item;
    public final ResourceLocation resourceLocation;
    private static final Map<Item, Addition> ITEM_MAP = new HashMap<>(); //item 2 addition
    private static final String ADDITION = "tnt_frame_additions/";
    private static Addition EMPTY;

    public Addition(ResourceLocation name, AdditionType type, float increment, int minTier, int maxCount, float weight, float instability, boolean specialRenderer, Item item){
        this.item = item;
        this.name = name;
        this.type = type;
        this.increment = increment;
        this.minTier = minTier;
        this.maxCount = maxCount;
        this.weight = weight;
        this.instability = instability;
        this.specialRenderer = specialRenderer;
        this.resourceLocation = new ResourceLocation(name.getNamespace(), ADDITION+name.getPath());
    }

    public ResourceLocation appendIndex(int index){
        return new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath()+"_"+index);
    }

    public static void register(ResourceLocation name, AdditionManager.AdditionWrapper wrapper){
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(wrapper.item));
        if(item==null || item== Items.AIR){
            ArtofTnt.LOGGER.warn("can't find item with id {} for tnt frame addition json {}",
                    wrapper.item, name);
            return;
        }
        register(name, AdditionType.fromString(wrapper.type), wrapper.increment, wrapper.minTier, wrapper.maxCount, wrapper.weight,
                wrapper.instability, wrapper.specialRenderer, item);
    }

    public static void register(ResourceLocation name, AdditionType type, float increment, int minTier, int maxCount, float weight, float instability, boolean specialRenderer, Item item){
        Addition addition = new Addition(name, type, increment, minTier, maxCount, weight, instability, specialRenderer, item);
        if(addition.name.equals(new ResourceLocation(ArtofTnt.MODID, "empty"))){
            EMPTY = addition;
        }
        if(!ITEM_MAP.containsKey(addition.item)) {
            ITEM_MAP.put(addition.item, addition);
        } else {
            ArtofTnt.LOGGER.warn("duplicate item {} for tnt frame addition json {} and {}, you may want to overwrite by creating a same file",
                    addition.item.getRegistryName(), ITEM_MAP.get(addition.item).name, name);
            ITEM_MAP.put(addition.item, addition);
        }
    }

    public static boolean contains(Item item) {
        return ITEM_MAP.containsKey(item);
    }

    public static Addition fromItem(Item item) {
        return ITEM_MAP.getOrDefault(item, EMPTY);
    }

    public static void clear(){
        ITEM_MAP.clear();
    }

}