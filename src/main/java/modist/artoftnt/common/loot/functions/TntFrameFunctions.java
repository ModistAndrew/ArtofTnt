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

public class TntFrameFunctions{
    @SuppressWarnings("unchecked")
    public static final Set<TntFrameFunctionWrapper>[] FUNCTIONS = new Set[]{new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()};
    public static final TntFrameFunctionWrapper TNT = new TntFrameFunctionBuilder(0).add(Items.GUNPOWDER, 4).add(Blocks.OBSIDIAN, 4).disguise(Blocks.TNT).build();
    public static class TntFrameFunctionBuilder {
        public final int tier;
        public final List<ItemStack> additions = new ArrayList<>();
        public float size = 1F;
        @Nullable
        public BlockState disguise;
        public boolean fixed;

        public TntFrameFunctionBuilder(int tier) {
            this.tier = tier;
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

        public TntFrameFunctionWrapper build(){
            TntFrameFunctionWrapper ret = new TntFrameFunctionWrapper();
            ret.additions.addAll(additions);
            ret.disguise = disguise;
            ret.size = size;
            ret.fixed = fixed;
            FUNCTIONS[tier].add(ret);
            return ret;
        }
    }
}

