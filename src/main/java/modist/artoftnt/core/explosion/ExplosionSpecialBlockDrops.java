package modist.artoftnt.core.explosion;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

public class ExplosionSpecialBlockDrops {
    private static final Random RANDOM = new Random();
    private static final HashMap<Block, Function<ExplosionContext, ItemStack>> DROPS = new HashMap<>();


    public static ItemStack getSpecialDrop(Block block, float percentage, float temperature, float strength) {
        if (!DROPS.containsKey(block)) {
            return ItemStack.EMPTY;
        }
        return DROPS.get(block).apply(new ExplosionContext(percentage, temperature, strength));
    }

    private static void registerMelting(Block block, ItemStack item, float minTemperature, float randomMul) {
        DROPS.put(block, context -> {
            float delta = context.temperature * context.percentage - minTemperature;
            if (delta > 0 && RANDOM.nextFloat() < delta * randomMul) {
                return item.copy();
            }
            return ItemStack.EMPTY;
        });
    }

    private static void registerPressure(Block block, ItemStack item, float minPressure, float randomMul) {
        DROPS.put(block, context -> {
            float delta = context.strength * context.percentage - minPressure;
            if (delta > 0 && RANDOM.nextFloat() < delta * randomMul) {
                return item.copy();
            }
            return ItemStack.EMPTY;
        });
    }

    private record ExplosionContext(float percentage, float temperature, float strength) {
    }

    static {
        registerMelting(Blocks.SAND, new ItemStack(Blocks.GLASS), 1, 1);
        registerPressure(Blocks.COAL_BLOCK, new ItemStack(Blocks.DIAMOND_BLOCK), 1, 1);
    }
}
