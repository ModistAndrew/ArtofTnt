package modist.artoftnt.core.explosion.manager;

import modist.artoftnt.common.block.ModBlockTags;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.event.CustomExplosionBlockEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

public class ExplosionSpecialDrops<T> {
    private final Random random = new Random();
    private final HashMap<TagKey<Block>, Function<CustomExplosionBlockEvent, T>> drops = new HashMap<>();

    //notice that may be null and should be copied
    public static final ExplosionSpecialDrops<ItemStack> ITEMS = new ExplosionSpecialDrops<>(); //drop item
    public static final ExplosionSpecialDrops<BlockState> BLOCKS = new ExplosionSpecialDrops<>(); //replace block

    @Nullable
    public T getSpecialDrop(BlockState state, CustomExplosionBlockEvent event) {
        for(TagKey<Block> tag : drops.keySet()){
            if(state.is(tag)){
                T ret = drops.get(tag).apply(event);
                if(ret!=null){
                    return ret;
                }
            }
        }
        return null;
    }

    public void registerSingleType(TagKey<Block> tag, T drop, AdditionType type, float min, float randomMul) {
        drops.put(tag, event -> {
            float delta = event.data.getValue(type) * event.percentage - min;
            if (delta >= 0 && random.nextFloat() < randomMul) {
                return drop;
            }
            return null;
        });
    }

    public void registerSimple(TagKey<Block> tag, T drop) {
        drops.put(tag, event -> drop);
    }

    static {
        ITEMS.registerSingleType(ModBlockTags.TO_GLASS, new ItemStack(Blocks.GLASS), AdditionType.TEMPERATURE, 1, 1);
        ITEMS.registerSingleType(ModBlockTags.TO_DIAMOND, new ItemStack(Items.DIAMOND), AdditionType.STRENGTH,1000, 0.05F);
        ITEMS.registerSimple(ModBlockTags.TO_BEDROCK, new ItemStack(Blocks.BEDROCK));
        BLOCKS.registerSingleType(ModBlockTags.TO_LAVA, Fluids.LAVA.defaultFluidState().createLegacyBlock(), AdditionType.TEMPERATURE, 2, 0.1F);
    }
}
