package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

public class TntTurretRenderer implements BlockEntityRenderer<TntTurretBlockEntity> {
    public TntTurretRenderer(BlockEntityRendererProvider.Context pContext){
    }

    @Override
    public void render(TntTurretBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        RenderUtil.renderArrow(pBufferSource, pPoseStack, pBlockEntity.getVec());
    }
}
