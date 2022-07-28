package modist.artoftnt.common.recipe;

import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.item.TntFireworkStarItem;
import modist.artoftnt.common.item.TntFrameItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TntFireworkStarRecipe extends CustomRecipe {
    private final int[] ID_MAP = new int[]{1, 2, 5, 8, 7, 6, 3, 0}; //id is stored in stack, value is the slot id
    public TntFireworkStarRecipe(ResourceLocation pId) {
        super(pId);
    }

    /**
     * Used to check if a recipe matches current crafting inventory
     */
    public boolean matches(CraftingContainer pInv, Level pLevel) {
        if (pInv.getWidth() == 3 && pInv.getHeight() == 3) {
            boolean hasTnt = false;
            for(int i = 0; i < pInv.getWidth(); ++i) {
                for(int j = 0; j < pInv.getHeight(); ++j) {
                    ItemStack itemstack = pInv.getItem(i + j * pInv.getWidth());
                    if (i == 1 && j == 1) {
                        if (!itemstack.is(Items.FIREWORK_STAR)) {
                            return false;
                        }
                    } else if (itemstack.getItem() instanceof TntFrameItem) {
                        hasTnt = true;
                    }
                }
            }
            return hasTnt;
        } else {
            return false;
        }
    }

    /**
     * Returns an Item that is the result of this recipe
     */
    public ItemStack assemble(CraftingContainer pInv) {
        ItemStack fireworkStar = pInv.getItem(1 + pInv.getWidth());
        if (!fireworkStar.is(Items.FIREWORK_STAR)) {
            return ItemStack.EMPTY;
        } else {
            ItemStack ret = new ItemStack(ItemLoader.TNT_FIREWORK_STAR.get());
            if(fireworkStar.getTagElement("Explosion")!=null) {
                ret.addTagElement("Explosion", fireworkStar.getTagElement("Explosion"));
            }
            for(int i=0; i<ID_MAP.length; i++){
                if(pInv.getItem(ID_MAP[i]).getItem() instanceof TntFrameItem item){
                    TntFireworkStarItem.putData(ret, i, item.getTntFrameDataTag(pInv.getItem(ID_MAP[i])), item.tier);
                }
            }
            return ret;
        }
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    public RecipeSerializer<?> getSerializer() {
        return RecipeLoader.TNT_FIREWORK_STAR.get();
    }
}
