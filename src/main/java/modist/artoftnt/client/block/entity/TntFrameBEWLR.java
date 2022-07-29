package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.common.item.TntShaperItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class TntFrameBEWLR extends BlockEntityWithoutLevelRenderer {
    public static final BlockEntityWithoutLevelRenderer BEWLR_INSTANCE = new TntFrameBEWLR();

    public TntFrameBEWLR() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack pStack, ItemTransforms.@NotNull TransformType pTransformType, @NotNull PoseStack pPoseStack, @NotNull MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (pStack.getItem() instanceof TntFrameItem item) {
            RenderUtil.renderTnt(item.getTntFrameData(pStack), pPoseStack, pBuffer, pPackedLight, false);
        }
        if (pStack.getItem() instanceof TntShaperItem item) {
            RenderUtil.renderCube(pBuffer.getBuffer(RenderType.cutout()), pPoseStack, 0, 0, 0, 1, 1, 1,
                    TextureLoader.TNT_SHAPER, pPackedLight, Direction.values());
            BlockState state = item.getState(pStack);
            if (state != null) {
                pPoseStack.pushPose();
                pPoseStack.translate(0.5F, 0.5F, 0.5F);
                long l = System.currentTimeMillis() / 10;
                float s = (l % 360);
                pPoseStack.mulPose(Quaternion.fromXYZDegrees(new Vector3f(0, s, 0)));
                pPoseStack.scale(0.5F, 0.5F, 0.5F);
                pPoseStack.translate(-0.5F, -0.5F, -0.5F);
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(state, pPoseStack, pBuffer, pPackedLight, pPackedOverlay,
                        EmptyModelData.INSTANCE);
                pPoseStack.popPose();
            }
        }
    }
}