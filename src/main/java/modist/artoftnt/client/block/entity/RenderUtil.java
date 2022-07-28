package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelDataMap;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

@OnlyIn(Dist.CLIENT)
public class RenderUtil {
    public static void renderLine(MultiBufferSource bufferSource, PoseStack poseStack, float x1, float y1, float z1, float x2, float y2, float z2,
                                  float a, float b, float c, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINES);
        buffer.vertex(matrix, x1, y1, z1).color(a, b, c, alpha).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(a, b, c, alpha).normal(1, 0, 0).endVertex();
    }

    public static void renderLine(MultiBufferSource bufferSource, PoseStack poseStack, Vec3 v1, Vec3 v2) { //TODO:more beautiful?
        renderLine(bufferSource, poseStack, (float)v1.x, (float)v1.y, (float)v1.z, (float)v2.x,
                (float)v2.y, (float)v2.z, 1F, 1F, 1F, 1F);
    }

    public static void transform(PoseStack poseStack, Vec3 from, Vec3 to, float offset) {
        poseStack.mulPoseMatrix(getTransform(from, to));
        poseStack.translate(0, offset, 0);
    }
    public static void transform(PoseStack poseStack, Vec3 d, float offset) {
        Vec3 mid = new Vec3(0.5, 0.5, 0.5);
        Vec3 delta = d.normalize().scale(0.5);
        transform(poseStack, mid.subtract(delta), mid.add(delta), offset);
    }

    public static void rotateAndRenderArrow(MultiBufferSource bufferSource, PoseStack poseStack) {
        long l = System.currentTimeMillis() / 10;
        float s = (l % 360);
        poseStack.mulPose(Quaternion.fromYXZ((float)Math.toRadians(s), 0, 0));
        renderLine(bufferSource, poseStack, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1, 0, 0, 15/16F, -1/16F, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1, 0, 0, 15/16F, 1/16F, 1, 0, 0, 1);
    }
    public static void renderArrow(MultiBufferSource bufferSource, PoseStack poseStack, Vec3 from, Vec3 to, float scale) {
        poseStack.pushPose();
        poseStack.mulPoseMatrix(getTransform(from, to));
        long l = System.currentTimeMillis() / 10;
        float s = (l % 360);
        poseStack.mulPose(Quaternion.fromYXZ((float)Math.toRadians(s), 0, 0));
        scale = 1/scale/16F;
        renderLine(bufferSource, poseStack, 0, 0, 0, 0, 1, 0, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1, 0, 0, 1-scale, -scale, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1, 0, 0, 1-scale, scale, 1, 0, 0, 1);
        poseStack.popPose();
    }
    public static Matrix4f getTransform(Vec3 from, Vec3 to){
        Vec3 origin = new Vec3(0, 1, 0);
        Matrix4f ret = Matrix4f.createTranslateMatrix((float) from.x, (float) from.y, (float) from.z);
        Vec3 rotate = to.subtract(from);
        Vec3 normal = origin.cross(rotate).normalize();
        if(normal.equals(Vec3.ZERO)){
            normal = new Vec3(1, 0, 0);
        }
        double degree = Math.acos(origin.dot(rotate)/origin.length()/rotate.length());
        ret.multiply(new Quaternion(new Vector3f(normal), (float) degree, false));
        float mul = (float)from.distanceTo(to);
        ret.multiply(Matrix4f.createScaleMatrix(mul, mul, mul));
        return ret;
    }

    public static void renderCube(VertexConsumer buffer, PoseStack poseStack, float x1, float y1, float z1, float x2, float y2, float z2,
                                  float a, float b, float c, float alpha, ResourceLocation texture, int brightness, Direction... sides) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        Matrix4f matrix = poseStack.last().pose();
        Arrays.stream(sides).forEach(d -> {
            switch (d) {
                case UP -> {
                    buffer.vertex(matrix, x2, y2, z2).color(a, b, c, alpha).uv(sprite.getU1(),sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y2, z1).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y2, z1).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y2, z2).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                }
                case DOWN -> {
                    buffer.vertex(matrix, x2, y1, z1).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y1, z2).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y1, z2).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y1, z1).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                }
                case EAST -> {
                    buffer.vertex(matrix, x2, y2, z1).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y2, z2).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y1, z2).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y1, z1).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                }
                case WEST -> {
                    buffer.vertex(matrix, x1, y1, z1).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y1, z2).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y2, z2).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y2, z1).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                }
                case SOUTH -> {
                    buffer.vertex(matrix, x2, y1, z2).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y2, z2).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y2, z2).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y1, z2).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                }
                case NORTH -> {
                    buffer.vertex(matrix, x1, y1, z1).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x1, y2, z1).color(a, b, c, alpha).uv(sprite.getU0(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y2, z1).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV1()).uv2(brightness).normal(1, 0, 0).endVertex();
                    buffer.vertex(matrix, x2, y1, z1).color(a, b, c, alpha).uv(sprite.getU1(), sprite.getV0()).uv2(brightness).normal(1, 0, 0).endVertex();
                }
            }
        });
    }

    public static void renderCube(VertexConsumer buffer, PoseStack poseStack, float x1, float y1, float z1, float x2, float y2, float z2,
                                  ResourceLocation texture, int brightness, Direction... sides){
        renderCube(buffer, poseStack, x1, y1, z1, x2, y2, z2, 1F,1F, 1F, 1F, texture, brightness, sides);
    }

    public static void renderTnt(TntFrameData data, PoseStack pMatrixStack, MultiBufferSource pRenderTypeBuffer, int pCombinedLight, boolean pDoFullBright) {
        int i; //TODO shine
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