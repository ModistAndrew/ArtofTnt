package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.core.addition.AdditionSlot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TntTurretRenderer implements BlockEntityRenderer<TntTurretBlockEntity> {

    public TntTurretRenderer() {
    }

    @Override
    public void render(@NotNull TntTurretBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        renderBase(pBlockEntity, pPoseStack, pBufferSource, pPackedLight);
        renderArrow(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedLight);
    }

    public void renderBase(TntTurretBlockEntity pBlockEntity, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight) {
        for (int i = 0; i < 16; i++) {
            pPoseStack.pushPose();
            pPoseStack.scale(1 / 16F, 1 / 16F, 1 / 16F);
            pPoseStack.translate(AdditionSlot.getU(i), 0, AdditionSlot.getV(i));
            VertexConsumer consumer = pBufferSource.getBuffer(RenderType.cutout());
            RenderUtil.renderCube(consumer, pPoseStack, 0, 1, 0, 2, 1, 2, TextureLoader.BASE_TOP[i], pPackedLight, Direction.UP);
            if (pBlockEntity.contains(i)) {
                RenderUtil.renderCube(consumer, pPoseStack, 0, 0.5F, 0, 2, 0.5F, pBlockEntity.getCount(i)/32F,
                        TextureLoader.BASE_TNT[pBlockEntity.getTier(i)], pPackedLight, Direction.UP);
            }
            pPoseStack.popPose();
        }
        pPoseStack.pushPose();
        pPoseStack.scale(0.5F, 0.5F, 0.5F);
        pPoseStack.translate(1, 0, 1);
        RenderUtil.rotateAndRenderArrow(pBufferSource, pPoseStack);
        pPoseStack.popPose();
    }

    public void renderArrow(TntTurretBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight) {
        pPoseStack.pushPose();
        float offset = pBlockEntity.getOffset(1-pPartialTick);
        RenderUtil.transform(pPoseStack, pBlockEntity.getVec().normalize(), -0.2F * offset);
        RenderUtil.rotateAndRenderArrow(pBufferSource, pPoseStack);
        if(pBlockEntity.presentTnt.getItem() instanceof TntFrameItem item){
            pPoseStack.scale(1/8F, 1/8F, 1/8F);
            pPoseStack.translate(-0.5D, -0.5D, -0.5D);
            pPoseStack.translate(0, pBlockEntity.getTntOffset(1-pPartialTick) * 9.6F, 0);
            TntFrameData data = item.getTntFrameData(pBlockEntity.presentTnt);
            data.size = 1F; //too small will be strange
            RenderUtil.renderTnt(data, pPoseStack, pBufferSource, pPackedLight, false);
        }
        pPoseStack.popPose();
    }
}
