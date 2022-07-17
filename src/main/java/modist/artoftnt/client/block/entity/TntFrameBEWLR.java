package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import modist.artoftnt.client.entity.PrimedTntFrameRenderer;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.item.TntFrameItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TntFrameBEWLR extends BlockEntityWithoutLevelRenderer {
    public static final BlockEntityWithoutLevelRenderer BEWLR_INSTANCE = new TntFrameBEWLR();
    public TntFrameBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }


    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.TransformType pTransformType, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if(pStack.getItem() instanceof TntFrameItem item){
            PrimedTntFrameRenderer.renderBlock(item.getTntFrameData(pStack), pPoseStack, pBuffer, pPackedLight, false);
        }
    } //TODO: remove, use normal(enchanted?)
}