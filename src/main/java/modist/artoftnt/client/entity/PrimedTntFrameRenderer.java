package modist.artoftnt.client.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import java.util.*;
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
        float r = data.size/2;
        float d = data.getDeflation();
        pMatrixStack.translate(0.0D, r, 0.0D);
        int i = pEntity.getFuse();
        if ((float)i - pPartialTicks + 1.0F < 10.0F) {
            float f = 1.0F - ((float)i - pPartialTicks + 1.0F) / 10.0F;
            f = Mth.clamp(f, 0.0F, 1.0F);
            f *= f;
            f *= f;
            float f1 = 1.0F + f * 0.3F;
            pMatrixStack.scale(f1, f1, f1);
        }
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(-90.0F));
        pMatrixStack.translate(-r, -r , r);
        pMatrixStack.mulPose(Vector3f.YP.rotationDegrees(90.0F));
        pMatrixStack.translate(-d, -d, -d);
        renderBlock(data, pMatrixStack, pBuffer, pPackedLight, i / 5 % 2 == 0);
        pMatrixStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }

    public static void renderBlock(TntFrameData data, PoseStack pMatrixStack, MultiBufferSource pRenderTypeBuffer, int pCombinedLight, boolean pDoFullBright) {
        int i;
        if (pDoFullBright) {
            i = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        } else {
            i = OverlayTexture.NO_OVERLAY;
        }
        Direction[] directions = new Direction[]{
                Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH, null
        };
        BlockState state = BlockLoader.TNT_FRAMES[data.tier].get().defaultBlockState();
        BakedModel bakedmodel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
        Random random = new Random();
        random.setSeed(42L);
        Arrays.stream(directions).forEach(d-> {
            List<BakedQuad> quads = bakedmodel.getQuads
                    (state, d, random, new ModelDataMap.Builder()
                            .withInitial(TntFrameBlockEntity.ADDITIONS_MODEL_PROPERTY, data)
                            .build());
            quads.forEach(q->pRenderTypeBuffer.getBuffer(RenderType.cutout()).putBulkData(pMatrixStack.last(), q, new float[]{1.0F, 1.0F, 1.0F, 1.0F}, 1.0F, 1.0F, 1.0F,
                    new int[]{pCombinedLight, pCombinedLight, pCombinedLight, pCombinedLight}, i, true));
        });
    }

}
