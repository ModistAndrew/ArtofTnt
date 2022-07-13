package modist.artoftnt.common.item;

import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.entity.TntFrameData;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemRenderProperties;

import java.util.function.Consumer;

public class TntFrameItem extends BlockItem {
    public final int tier;

    public TntFrameItem(int tier) {
        super(BlockLoader.TNT_FRAMES[tier].get(), ItemLoader.getProperty());
        this.tier = tier;
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        consumer.accept(new IItemRenderProperties() {
            @Override
            public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
                return ItemLoader.BEWLR_INSTANCE;
            }
        });
    }

    public TntFrameData getTntFrameData(ItemStack stack){
        return new TntFrameData(tier, stack.getTagElement("tntFrameData"));
    }
}
