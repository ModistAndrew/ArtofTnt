package modist.artoftnt.core.explosion.manager;

import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.event.CustomExplosionBlockEvent;
import net.minecraft.tags.BlockTags;
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
    public static final ExplosionSpecialDrops<ItemStack> ITEMS = new ExplosionSpecialDrops<ItemStack>();
    public static final ExplosionSpecialDrops<BlockState> BLOCKS = new ExplosionSpecialDrops<BlockState>();

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
            if (delta > 0 && random.nextFloat() < delta * randomMul) {
                return drop;
            }
            return null;
        });
    }

    static {
        ITEMS.registerSingleType(BlockTags.SAND, new ItemStack(Blocks.GLASS), AdditionType.TEMPERATURE, 1, 1);
        ITEMS.registerSingleType(BlockTags.COAL_ORES, new ItemStack(Items.DIAMOND), AdditionType.STRENGTH,1, 1);
        BLOCKS.registerSingleType(BlockTags.BASE_STONE_OVERWORLD, Fluids.LAVA.defaultFluidState().createLegacyBlock(), AdditionType.TEMPERATURE, 1, 1);
    }
}
