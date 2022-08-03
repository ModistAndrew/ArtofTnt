package modist.artoftnt.common.loot.functions;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.JsonUtil;
import modist.artoftnt.common.item.ItemLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TntFrameFunctions {
    @SuppressWarnings("unchecked")
    public static final Set<TntFrameFunctionWrapper>[] FUNCTIONS = new Set[]{new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>()};

    static {
        new TntFrameFunctionBuilder(3, "tnt").add(Items.GUNPOWDER, 4).add(Items.IRON_INGOT, 4)
                .add(Items.STRING, 8).disguise(Blocks.TNT).build();
        new TntFrameFunctionBuilder(3, "fire charge").add(Items.GUNPOWDER, 4).add(Items.QUARTZ, 2)
                .add(Blocks.PISTON, 2).size(0.5F).add(ItemLoader.SIMPLE_ITEMS.get("tnt_motion_modifier").get(), 1)
                .add(Items.STRING, 8).add(ItemLoader.SIMPLE_BLOCK_ITEMS.get("blaze_block_2").get(), 1)
                .add(Items.GOLD_INGOT, 7).add(Items.IRON_INGOT, 4).disguise(Blocks.MAGMA_BLOCK).build();
        new TntFrameFunctionBuilder(3, "bullet").add(Items.GUNPOWDER, 4).add(Items.QUARTZ, 2)
                .add(Blocks.PISTON, 2).size(0.25F).add(ItemLoader.SIMPLE_ITEMS.get("tnt_motion_modifier").get(), 2)
                .add(Items.STRING, 8).add(Items.GOLD_INGOT, 7).disguise(Blocks.STONE).build();
        new TntFrameFunctionBuilder(3, "draw bullet").add(Items.GUNPOWDER, 6).add(Items.QUARTZ, 2)
                .add(Blocks.PISTON, 2).size(0.5F).add(Items.FIREWORK_ROCKET, 4)
                .add(ItemLoader.SIMPLE_ITEMS.get("tnt_draw_modifier").get(), 4)
                .add(Items.STRING, 8).disguise(Blocks.DIORITE).build();
        new TntFrameFunctionBuilder(3, "missile").add(Items.GUNPOWDER, 6).add(Items.QUARTZ, 2)
                .add(Blocks.PISTON, 2).size(0.75F).add(Items.FIREWORK_ROCKET, 8)
                .add(ItemLoader.SIMPLE_ITEMS.get("piercing_modifier").get(), 4)
                .add(Items.STRING, 8).add(Items.GOLD_INGOT, 7).disguise(Blocks.IRON_BLOCK).add(Items.REDSTONE_TORCH, 1).build();
        new TntFrameFunctionBuilder(1, "tear down").add(Items.GUNPOWDER, 4).add(Items.QUARTZ, 2)
                .add(Blocks.PISTON, 2).size(0.75F).add(Items.FIREWORK_ROCKET, 8)
                .add(Items.STRING, 8).add(Items.IRON_INGOT, 4).add(Items.IRON_SWORD, 4)
                .add(Items.GOLD_INGOT, 7).disguise(Blocks.OBSIDIAN).build();
        new TntFrameFunctionBuilder(3, "ice hockey").add(Blocks.BLUE_ICE, 3).add(Blocks.SLIME_BLOCK, 1)
                .add(ItemLoader.SIMPLE_ITEMS.get("string_2").get(), 8)
                .disguise(Blocks.BLUE_ICE).build();
        new TntFrameFunctionBuilder(3, "badminton").add(Items.GOLD_INGOT, 7).add(Items.FIREWORK_ROCKET, 2)
                .add(ItemLoader.SIMPLE_ITEMS.get("string_2").get(), 8)
                .disguise(Blocks.WHITE_WOOL).size(0.5F).build();
        new TntFrameFunctionBuilder(3, "soup").add(Items.GUNPOWDER, 4)
                .add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.STRONG_HEALING))
                .add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.STRONG_REGENERATION))
                .add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.STRONG_STRENGTH))
                .add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.STRONG_LEAPING))
                .add(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.STRONG_SWIFTNESS))
                .size(0.5F).disguise(Blocks.GOLD_BLOCK).build();
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
//            ArtofTnt.LOGGER.info(JsonUtil.GSON.toJson(ret));
            return ret;
        }
    }
}

