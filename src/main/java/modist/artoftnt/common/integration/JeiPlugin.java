package modist.artoftnt.common.integration;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.common.util.ImmutableRect2i;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.core.addition.Addition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    private static final String PREFIX = "jei.artoftnt.";
    public static final RecipeType<Addition> ADDITION =
            RecipeType.create(ArtofTnt.MODID, "tnt_frame_addition",
                    Addition.class);

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new IRecipeCategory<Addition>(){
            private final ImmutableRect2i textArea =
                    new ImmutableRect2i(20, 0, 30, 34);

            @Override
            public Component getTitle() {
                return new TranslatableComponent(PREFIX+"tnt_frame_additions");
            }

            @Override
            public IDrawable getBackground() {
                return registry.getJeiHelpers().getGuiHelper().createBlankDrawable(162, 54);
            }

            @Override
            public IDrawable getIcon() {
                return registry.getJeiHelpers().getGuiHelper().createDrawableIngredient
                        (VanillaTypes.ITEM_STACK, new ItemStack(ItemLoader.TNT_FRAMES[3].get()));
            }

            @SuppressWarnings("removal")
            @Override
            public ResourceLocation getUid() {
                return ADDITION.getUid();
            }

            @SuppressWarnings("removal")
            @Override
            public Class<? extends Addition> getRecipeClass() {
                return Addition.class;
            }

            @Override
            public void setRecipe(IRecipeLayoutBuilder builder, Addition recipe, IFocusGroup focuses) {
                builder.addSlot(RecipeIngredientRole.INPUT, 1, 17).addItemStack(new ItemStack(recipe.item));
            }

            @Override
            public void draw(Addition recipe, IRecipeSlotsView recipeSlotsView, PoseStack poseStack, double mouseX, double mouseY) {
                Minecraft minecraft = Minecraft.getInstance();
                Font font = minecraft.font;
                drawText(font, poseStack, "type", I18n.get(recipe.type.getTranslation()), 0, 0);
                drawText(font, poseStack, "increment", recipe.increment, 0, 1);
                drawText(font, poseStack, "minTier", recipe.minTier, 0, 2);
                drawText(font, poseStack, "maxTier", recipe.maxTier, 1, 2);
                drawText(font, poseStack, "max_count", recipe.maxCount, 0, 3);
                drawText(font, poseStack, "weight", recipe.weight, 0, 4);
                drawText(font, poseStack, "instability", recipe.instability, 1, 4);
            }

            private void drawText(Font font, PoseStack poseStack, String name, Object value, int x, int y){
                font.draw(poseStack, getComponent(name, value), 25 + 62 * x, -1+12*y, 0xFF808080);
            }

            private Component getComponent(String name, Object value){
                return new TranslatableComponent(PREFIX+name, value);
            }
        });
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ADDITION, Addition.getAll());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        Arrays.stream(ItemLoader.TNT_FRAMES).forEach(i ->
                registration.addRecipeCatalyst(new ItemStack(i.get()), ADDITION));
    }

    public static final ResourceLocation UID = new ResourceLocation(ArtofTnt.MODID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
}
