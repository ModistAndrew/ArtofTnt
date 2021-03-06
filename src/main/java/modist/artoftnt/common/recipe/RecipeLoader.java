package modist.artoftnt.common.recipe;

import modist.artoftnt.ArtofTnt;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RecipeLoader {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ArtofTnt.MODID);

    public static final RegistryObject<RecipeSerializer<?>> TNT_FIREWORK_STAR = RECIPES.register
            ("crafting_tnt_firework_star", ()->new SimpleRecipeSerializer<>(TntFireworkStarRecipe::new));

    public static final RegistryObject<RecipeSerializer<?>> DEMO_TNT_FRAME = RECIPES.register
            ("crafting_demo_tnt_frame", DemoTntFrameRecipe.Serializer::new);
}
