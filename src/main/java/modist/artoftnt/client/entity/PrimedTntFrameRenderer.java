package modist.artoftnt.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import modist.artoftnt.client.block.entity.RenderUtil;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
@OnlyIn(Dist.CLIENT)
public class PrimedTntFrameRenderer extends EntityRenderer<PrimedTntFrame> {

    public PrimedTntFrameRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(PrimedTntFrame pEntity) {
        return InventoryMenu.BLOCK_ATLAS;
    }

    @Override
    public void render(PrimedTntFrame pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pPackedLight) {
        pMatrixStack.pushPose();
        TntFrameData data = new TntFrameData(pEntity.tier, pEntity.getDataTag());
        float r = data.size / 2;
        float d = data.getDeflation();
        pMatrixStack.translate(0.0D, r, 0.0D);
        int i = pEntity.getFuse();
        if ((float) i - pPartialTicks + 1.0F < 10.0F) {
            float f = 1.0F - ((float) i - pPartialTicks + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float f1 = 1.0F + f * 0.3F;
            pMatrixStack.scale(f1, f1, f1);
        }
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        pMatrixStack.translate(-r, -r, r);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        pMatrixStack.translate(-d, -d, -d);
        RenderUtil.renderTnt(data, pMatrixStack, pBuffer, pPackedLight, i / 5 % 2 == 0);
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

}
