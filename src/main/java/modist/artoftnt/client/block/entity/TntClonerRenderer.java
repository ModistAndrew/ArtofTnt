package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.common.block.TntClonerBlock;
import modist.artoftnt.common.block.entity.TntClonerBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.item.TntFrameItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TntClonerRenderer implements BlockEntityRenderer<TntClonerBlockEntity> {

    public TntClonerRenderer() {
    }

    @Override
    public void render(TntClonerBlockEntity pBlockEntity, float pPartialTick, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Direction d = pBlockEntity.getBlockState().getValue(TntClonerBlock.FACING);
        Vec3 mid = new Vec3(0.5, 0.5, 0.5);
        Vec3 delta = Vec3.atLowerCornerOf(d.getNormal()).scale(0.5);
        RenderUtil.renderArrow(pBufferSource, pPoseStack, mid.subtract(delta), mid.add(delta), 1);
        float rate = pBlockEntity.finishRate();
        RenderUtil.renderArrow(pBufferSource, pPoseStack, new Vec3(0.5, 0, 0.5), new Vec3(0.5,
                3/8F * rate, 0.5), rate);
        RenderUtil.renderArrow(pBufferSource, pPoseStack, new Vec3(0.5, 1, 0.5), new Vec3(0.5,
                1-3/8F * rate, 0.5), rate);
        if (pBlockEntity.tntFrame.getItem() instanceof TntFrameItem item) {
            float offset = pBlockEntity.getOffset(1-pPartialTick);
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0.5, 0.5);
            pPoseStack.translate(delta.x * offset, delta.y * offset, delta.z * offset);
            pPoseStack.scale(1/8F, 1/8F, 1/8F);
            long l = System.currentTimeMillis() / 10;
            float s = (l % 360);
            pPoseStack.mulPose(Quaternion.fromXYZDegrees(new Vector3f(0, s, 0)));
            pPoseStack.translate(-0.5F, -0.5F, -0.5F);
            TntFrameData data = item.getTntFrameData(pBlockEntity.tntFrame);
            if(!pBlockEntity.finished()){
                data = new TntFrameData(data.tier);
            }
            if(pBlockEntity.justFinish()) {
                Vec3 v = Vec3.atCenterOf(pBlockEntity.getBlockPos());
                if(pBlockEntity.getLevel()!=null) {
                    pBlockEntity.getLevel().addParticle(ParticleTypes.COMPOSTER, v.x, v.y, v.z, 0, 0, 0);
                }
            }
            RenderUtil.renderTnt(data, pPoseStack, pBufferSource, pPackedLight, false);
            pPoseStack.popPose();
        }
    }
}
