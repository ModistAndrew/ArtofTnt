package modist.artoftnt.data;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.ModBlockTags;
import modist.artoftnt.common.item.ItemLoader;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class RecipeGenerator extends RecipeProvider {
    public RecipeGenerator(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("sound_type_modifier").get(), 8)
                .requires(Blocks.NOTE_BLOCK)
                .group("tnt_frame_addition")
                .unlockedBy("has_note_block", has(Blocks.NOTE_BLOCK))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("tnt_particle_modifier").get(), 8)
                .requires(Items.GLASS_BOTTLE)
                .group("tnt_frame_addition")
                .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("tnt_motion_modifier").get())
                .requires(Items.FIREWORK_ROCKET, 9)
                .group("tnt_frame_addition")
                .unlockedBy("has_firework_rocket", has(Items.FIREWORK_ROCKET))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("tnt_damage_modifier").get())
                .requires(Blocks.QUARTZ_BLOCK, 9)
                .group("tnt_frame_addition")
                .unlockedBy("has_firework_rocket", has(Blocks.QUARTZ_BLOCK))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("punch_modifier").get())
                .requires(Blocks.PISTON, 9)
                .group("tnt_frame_addition")
                .unlockedBy("has_piston", has(Blocks.PISTON))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("horizontal_piston").get(), 6)
                .pattern("xxx")
                .define('x', Blocks.PISTON)
                .group("tnt_frame_addition")
                .unlockedBy("has_piston", has(Blocks.PISTON))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("vertical_piston").get(), 6)
                .pattern("x")
                .pattern("x")
                .pattern("x")
                .define('x', Blocks.PISTON)
                .group("tnt_frame_addition")
                .unlockedBy("has_piston", has(Blocks.PISTON))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("tnt_punch_modifier").get())
                .pattern("xxx")
                .pattern("xWx")
                .pattern("xxx")
                .define('x', Blocks.PISTON)
                .define('W', Blocks.CONDUIT)
                .group("tnt_frame_addition")
                .unlockedBy("has_piston", has(Blocks.PISTON))
                .unlockedBy("has_conduit", has(Blocks.CONDUIT))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("tnt_draw_modifier").get())
                .pattern("xxx")
                .pattern("xWx")
                .pattern("xxx")
                .define('x', Blocks.HOPPER)
                .define('W', Blocks.CONDUIT)
                .group("tnt_frame_addition")
                .unlockedBy("has_hopper", has(Blocks.HOPPER))
                .unlockedBy("has_conduit", has(Blocks.CONDUIT))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("gunpowder_up").get(), 6)
                .pattern("  x")
                .pattern(" x ")
                .pattern("x  ")
                .define('x', Items.GUNPOWDER)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("gunpowder_down").get(), 6)
                .pattern("x  ")
                .pattern(" x ")
                .pattern("  x")
                .define('x', Items.GUNPOWDER)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("gunpowder_east").get(), 6)
                .pattern("  x")
                .pattern("  x")
                .pattern("  x")
                .define('x', Items.GUNPOWDER)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("gunpowder_west").get(), 6)
                .pattern("x  ")
                .pattern("x  ")
                .pattern("x  ")
                .define('x', Items.GUNPOWDER)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("gunpowder_south").get(), 6)
                .pattern("   ")
                .pattern("   ")
                .pattern("xxx")
                .define('x', Items.GUNPOWDER)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("gunpowder_north").get(), 6)
                .pattern("xxx")
                .pattern("   ")
                .pattern("   ")
                .define('x', Items.GUNPOWDER)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("gunpowder_2").get())
                .requires(Items.GUNPOWDER, 9)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("gunpowder_3").get())
                .requires(ItemLoader.SIMPLE_ITEMS.get("gunpowder_2").get(), 9)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder_2", has(ItemLoader.SIMPLE_ITEMS.get("gunpowder_2").get()))
                .save(consumer);
        ShapelessRecipeBuilder.shapeless(ItemLoader.SIMPLE_ITEMS.get("gunpowder_4").get())
                .requires(ItemLoader.SIMPLE_ITEMS.get("gunpowder_3").get(), 9)
                .group("tnt_frame_addition")
                .unlockedBy("has_gunpowder_3", has(ItemLoader.SIMPLE_ITEMS.get("gunpowder_3").get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("piercing_modifier").get())
                .pattern("xxx")
                .pattern("xWx")
                .pattern("xxx")
                .define('x', Items.ENDER_PEARL)
                .define('W', Items.GHAST_TEAR)
                .group("tnt_frame_addition")
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .unlockedBy("has_ghast_tear", has(Items.GHAST_TEAR))
                .save(consumer);
        ShapedRecipeBuilder.shaped(BlockLoader.REINFORCED_GLASS.get())
                .pattern("xxx")
                .pattern("xWx")
                .pattern("xxx")
                .define('x', Blocks.IRON_BLOCK)
                .define('W', Blocks.DIAMOND_BLOCK)
                .group("tnt_frame_addition")
                .unlockedBy("has_iron_block", has(Blocks.IRON_BLOCK))
                .unlockedBy("has_diamond_block", has(Blocks.DIAMOND_BLOCK))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("block_drop_2").get())
                .pattern("xWx")
                .pattern("WxW")
                .pattern("xWx")
                .define('x', Blocks.SAND)
                .define('W', Blocks.RED_SAND)
                .group("tnt_frame_addition")
                .unlockedBy("has_sand", has(Blocks.SAND))
                .unlockedBy("has_red_sand", has(Blocks.RED_SAND))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("block_drop_3").get())
                .pattern("xWx")
                .pattern("WxW")
                .pattern("xWx")
                .define('x', ItemLoader.SIMPLE_ITEMS.get("block_drop_2").get())
                .define('W', Blocks.OBSIDIAN)
                .group("tnt_frame_addition")
                .unlockedBy("has_block_drop_2", has(ItemLoader.SIMPLE_ITEMS.get("block_drop_2").get()))
                .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.SIMPLE_ITEMS.get("block_drop_4").get())
                .pattern("xWx")
                .pattern("WxW")
                .pattern("xWx")
                .define('x', ItemLoader.SIMPLE_ITEMS.get("block_drop_3").get())
                .define('W', Blocks.GOLD_BLOCK)
                .group("tnt_frame_addition")
                .unlockedBy("has_block_drop_3", has(ItemLoader.SIMPLE_ITEMS.get("block_drop_3").get()))
                .unlockedBy("has_gold_block", has(Blocks.GOLD_BLOCK))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.POSITION_CONTAINER_MARKER_2.get())
                .pattern("xxx")
                .pattern("xWx")
                .pattern("xxx")
                .define('x', Blocks.CHEST)
                .define('W', ItemLoader.POSITION_MARKERS[2].get())
                .group("tnt_frame_addition")
                .unlockedBy("has_chest", has(Blocks.CHEST))
                .unlockedBy("has_position_marker_2", has(ItemLoader.POSITION_MARKERS[2].get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(ItemLoader.POSITION_CONTAINER_MARKER_3.get())
                .pattern("xxx")
                .pattern("xWx")
                .pattern("xxx")
                .define('x', Blocks.ENDER_CHEST)
                .define('W', ItemLoader.POSITION_MARKERS[3].get())
                .group("tnt_frame_addition")
                .unlockedBy("has_ender_chest", has(Blocks.ENDER_CHEST))
                .unlockedBy("has_position_marker_3", has(ItemLoader.POSITION_MARKERS[3].get()))
                .save(consumer);
    }
}