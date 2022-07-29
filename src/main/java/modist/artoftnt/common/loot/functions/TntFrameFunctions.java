package modist.artoftnt.common.loot.functions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//TODO: data gen
public class TntFrameFunctions {
    @SuppressWarnings("unchecked")
    public static final Set<TntFrameFunctionWrapper>[] FUNCTIONS = new Set[]{new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()};

    static {
        new TntFrameFunctionBuilder(1, "tnt").add(Items.GUNPOWDER, 4).add(Items.IRON_INGOT, 4)
                .add(Items.STRING, 8).disguise(Blocks.TNT).build();
        new TntFrameFunctionBuilder(1, "fire charge").add(Items.GUNPOWDER, 4).add(Items.IRON_INGOT, 4)
                .add(Items.BLAZE_POWDER, 4).size(0.25F).disguise(Blocks.TNT).build();
        new TntFrameFunctionBuilder(1, "bullet").add(Items.GUNPOWDER, 4).add(Items.QUARTZ, 4)
                .add(Blocks.PISTON, 4).size(0.25F).add(Items.FIREWORK_ROCKET, 4)
                .add(Items.STRING, 8).add(Items.GOLD_INGOT, 6).disguise(Blocks.STONE).build();
    }

    public static class TntFrameFunctionBuilder {
        public final int tier;
        public final String name;
        public final List<ItemStack> additions = new ArrayList<>();
        public float size = 1F;
        @Nullable
        public BlockState disguise;
        public boolean fixed;

        public TntFrameFunctionBuilder(int tier, String name) {
            this.tier = tier;
            this.name = name;
        }

        public TntFrameFunctionBuilder add(ItemStack itemStack) {
            additions.add(itemStack);
            return this;
        }

        public TntFrameFunctionBuilder add(ItemLike item, int count) {
            return add(new ItemStack(item, count));
        }

        public TntFrameFunctionBuilder size(float size) {
            this.size = size;
            return this;
        }

        public TntFrameFunctionBuilder disguise(BlockState disguise) {
            this.disguise = disguise;
            return this;
        }

        public TntFrameFunctionBuilder disguise(Block disguise) {
            return this.disguise(disguise.defaultBlockState());
        }

        public TntFrameFunctionBuilder fixed() {
            this.fixed = true;
            return this;
        }

        public TntFrameFunctionWrapper build() {
            TntFrameFunctionWrapper ret = new TntFrameFunctionWrapper();
            ret.additions.addAll(additions);
            ret.disguise = disguise;
            ret.size = size;
            ret.fixed = fixed;
            ret.name = name;
            FUNCTIONS[tier].add(ret);
            return ret;
        }
    }
}

