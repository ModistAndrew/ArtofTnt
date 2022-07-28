package modist.artoftnt.common.recipe;

import com.google.gson.JsonObject;
import modist.artoftnt.common.JsonUtil;
import modist.artoftnt.common.loot.functions.TntFrameFunctionWrapper;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DemoTntFrameRecipe extends ShapedRecipe {
    final TntFrameFunctionWrapper function;

    public DemoTntFrameRecipe(ResourceLocation pId, String pGroup, int pWidth, int pHeight, NonNullList<Ingredient> pRecipeItems, ItemStack pResult,
                              TntFrameFunctionWrapper function) {
        super(pId, pGroup, pWidth, pHeight, pRecipeItems, pResult);
        this.function = function;
    }

    public DemoTntFrameRecipe(ShapedRecipe recipe, TntFrameFunctionWrapper function) {
        this(recipe.getId(), recipe.getGroup(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getResultItem(), function);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer pInv) {
        return function.apply(super.assemble(pInv));
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return RecipeLoader.DEMO_TNT_FRAME.get();
    }

    public static class Serializer extends ShapedRecipe.Serializer {
        @Override
        public DemoTntFrameRecipe fromJson(ResourceLocation pRecipeId, JsonObject pJson) {
            ShapedRecipe recipe = super.fromJson(pRecipeId, pJson);
            TntFrameFunctionWrapper wrapper = JsonUtil.GSON.fromJson(pJson.get("data"), TntFrameFunctionWrapper.class);
            return new DemoTntFrameRecipe(recipe, wrapper);
        }

        @Override
        public DemoTntFrameRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ShapedRecipe recipe = super.fromNetwork(pRecipeId, pBuffer);
            TntFrameFunctionWrapper function = new TntFrameFunctionWrapper();
            function.deserializeNBT(pBuffer.readNbt());
            return new DemoTntFrameRecipe(Objects.requireNonNull(recipe), function);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, ShapedRecipe pRecipe) {
            super.toNetwork(pBuffer, pRecipe);
            if(pRecipe instanceof DemoTntFrameRecipe recipe) {
                pBuffer.writeNbt(recipe.function.serializeNBT());
            }
        }
    }
}
