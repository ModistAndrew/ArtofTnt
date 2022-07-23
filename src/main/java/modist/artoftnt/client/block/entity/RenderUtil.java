package modist.artoftnt.client.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

public class RenderUtil {
    public static void renderLine(MultiBufferSource bufferSource, PoseStack poseStack, float x1, float y1, float z1, float x2, float y2, float z2,
                                  float a, float b, float c, float alpha) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.LINES);
        buffer.vertex(matrix, x1, y1, z1).color(a, b, c, alpha).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(a, b, c, alpha).normal(1, 0, 0).endVertex();
    }

    public static void renderArrow(MultiBufferSource bufferSource, PoseStack poseStack, Vec3 from, Vec3 to) {
        poseStack.pushPose();
        poseStack.mulPoseMatrix(getTransform(from, to));
        renderArrow(bufferSource, poseStack);
        poseStack.popPose();
    }

    public static void renderArrow(MultiBufferSource bufferSource, PoseStack poseStack, Vec3 d) {
        if(d.equals(Vec3.ZERO)){
            float s = (float) Math.toRadians((System.currentTimeMillis() / 4) % 360);
            d = new Vec3(0, 1, 0).xRot(s).yRot(s).zRot(s);
        }
        Vec3 mid = new Vec3(0.5, 0.5, 0.5);
        Vec3 delta = d.normalize().scale(0.5);
        renderArrow(bufferSource, poseStack, mid.subtract(delta), mid.add(delta));
    }

    public static void renderArrow(MultiBufferSource bufferSource, PoseStack poseStack) {
        float s = ((System.currentTimeMillis() / 4) % 360);
        poseStack.mulPose(Quaternion.fromYXZ((float)Math.toRadians(s), 0, 0));
        renderLine(bufferSource, poseStack, 0, 1/16F, 0, 0, 1, 0, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1/16F, 0, 1/16F, 0, 0, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1/16F, 0, -1/16F, 0, 0, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1, 0, 0, 15/16F, -1/16F, 1, 0, 0, 1);
        renderLine(bufferSource, poseStack, 0, 1, 0, 0, 15/16F, 1/16F, 1, 0, 0, 1);
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
        return ret;
    }

}
