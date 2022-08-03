package modist.artoftnt.client.block.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import modist.artoftnt.ArtofTntConfig;
import modist.artoftnt.core.addition.Addition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nullable;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class BakeModelRenderer {
    Transformation globalTransform;
    public final Random random = new Random();
    private final List<BakedQuad> quads = new ArrayList<>();

    public BakeModelRenderer(Transformation transformation) {
        this.globalTransform = transformation;
    }

    public void setTransform(Transformation transform){
        this.globalTransform = transform;
    }

    public void putSpecialItemStackQuads(@Nullable ItemStack stack, @Nullable Addition addition, int index, int slot) {
        AdditionSpecialRenderer.putSpecialItemStackQuads(this, random, stack, addition, index, slot);
    }

    public List<BakedQuad> getQuads() {
        return this.quads;
    }

    private void putVertex(BakedQuadBuilder builder, Vector3f normal, Vector4f pos,
                           float u, float v, TextureAtlasSprite sprite, int color) {
        ImmutableList<VertexFormatElement> elements = builder.getVertexFormat().getElements();
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        for (int i = 0; i < elements.size(); i++) {
            VertexFormatElement e = elements.get(i);
            switch (e.getUsage()) {
                case POSITION -> builder.put(i, pos.x(), pos.y(), pos.z(), 1.0f);
                case COLOR -> builder.put(i, f, f1, f2, 1.0f);
                case UV -> putVertexUV(builder, u, v, sprite, i, e);
                case NORMAL -> builder.put(i, normal.x(), normal.y(), normal.z());
                default -> builder.put(i);
            }
        }
    }

    private void putVertexUV(BakedQuadBuilder builder, float u, float v, TextureAtlasSprite sprite, int j, VertexFormatElement e) {
        switch (e.getIndex()) {
            case 0 -> builder.put(j, sprite.getU(u), sprite.getV(v));
            case 2 -> builder.put(j, (short)0, (short)0);
            default -> builder.put(j);
        }
    }

    //u and v from 0 to the length and height of the quad
    private BakedQuad createQuad(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, ResourceLocation r, int color) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(r);
        Vector3f normal = v3.copy();
        normal.sub(v2);
        Vector3f temp = v1.copy();
        temp.sub(v2);
        normal.cross(temp);
        normal.normalize(); //get normal

        int u = sprite.getWidth();
        int v = sprite.getHeight();

        globalTransform.transformNormal(normal);

        Vector4f vv1 = new Vector4f(v1);
        globalTransform.transformPosition(vv1);
        Vector4f vv2 = new Vector4f(v2);
        globalTransform.transformPosition(vv2);
        Vector4f vv3 = new Vector4f(v3);
        globalTransform.transformPosition(vv3);
        Vector4f vv4 = new Vector4f(v4);
        globalTransform.transformPosition(vv4);

        var builder = new BakedQuadBuilder(sprite);
        builder.setQuadOrientation(Direction.getNearest(normal.x(), normal.y(), normal.z()));
        putVertex(builder, normal, vv1, 0, v, sprite, color);
        putVertex(builder, normal, vv2, u, v, sprite, color);
        putVertex(builder, normal, vv3, u, 0, sprite, color);
        putVertex(builder, normal, vv4, 0, 0, sprite, color); //use the full texture
        return builder.build();
    }

    private BakedQuad createCubeFace(Vector3f f, Vector3f t, ResourceLocation r, Direction direction, int color) {
        return switch (direction) {
            case UP -> createQuad(v(f, t, 0, 1, 0), v(f, t, 0, 1, 1), v(f, t, 1, 1, 1), v(f, t, 1, 1, 0), r, color);
            case DOWN -> createQuad(v(f, t, 0, 0, 0), v(f, t, 1, 0, 0), v(f, t, 1, 0, 1), v(f, t, 0, 0, 1), r, color);
            case SOUTH -> createQuad(v(f, t, 0, 0, 1), v(f, t, 1, 0, 1), v(f, t, 1, 1, 1), v(f, t, 0, 1, 1), r, color);
            case NORTH -> createQuad(v(f, t, 1, 0, 0), v(f, t, 0, 0, 0), v(f, t, 0, 1, 0), v(f, t, 1, 1, 0), r, color);
            case EAST -> createQuad(v(f, t, 1, 0, 1), v(f, t, 1, 0, 0), v(f, t, 1, 1, 0), v(f, t, 1, 1, 1), r, color);
            case WEST -> createQuad(v(f, t, 0, 0, 0), v(f, t, 0, 0, 1), v(f, t, 0, 1, 1), v(f, t, 0, 1, 0), r, color);
        };
    }

    private List<BakedQuad> createCube(Vector3f f, Vector3f t, ResourceLocation r, boolean up, boolean down, int color) {
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction d : Direction.values()) {
            if ((d == Direction.UP && !up) || (d == Direction.DOWN && !down)) {
                continue;
            }
            quads.add(createCubeFace(f, t, r, d, color));
        }
        return quads;
    }

    public void putCubeFace16(float x1, float y1, float z1, float x2, float y2, float z2, ResourceLocation r, Direction direction) {
        quads.add(createCubeFace(v(x1 / 16F, y1 / 16F, z1 / 16F), v(x2 / 16F, y2 / 16F, z2 / 16F), r, direction, -1));
    }

    public void putCube16(float x1, float y1, float z1, float x2, float y2, float z2, ResourceLocation r, boolean up, boolean down) {
        quads.addAll(createCube(v(x1 / 16F, y1 / 16F, z1 / 16F), v(x2 / 16F, y2 / 16F, z2 / 16F), r, up, down, -1));
    }

    public void transform(List<BakedQuad> quads) {
        ColorQuadTransformer transformer = new ColorQuadTransformer(globalTransform);
        this.quads.addAll(transformer.processMany(false, quads, null, null));
    }

    public void transformBlock(List<BakedQuad> quads, BlockState state) {
        ColorQuadTransformer transformer = new ColorQuadTransformer(globalTransform);
        this.quads.addAll(transformer.processMany(true, quads, state, null));
    }
    public void transformItem(List<BakedQuad> quads, ItemStack stack) {
        ColorQuadTransformer transformer = new ColorQuadTransformer(globalTransform);
        this.quads.addAll(transformer.processMany(false, quads, null, stack));
    }

    private Vector3f v(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    private Vector3f v(Vector3f from, Vector3f to, int x, int y, int z) {
        return v(x == 0 ? from.x() : to.x(), y == 0 ? from.y() : to.y(), z == 0 ? from.z() : to.z());
    }

    //see quadTransformer
    private static class ColorQuadTransformer {
        private static final int POSITION = findPositionOffset(DefaultVertexFormat.BLOCK);
        private static final int NORMAL = findNormalOffset(DefaultVertexFormat.BLOCK);
        private static final int COLOR = findColorOffset(DefaultVertexFormat.BLOCK);
        private final Transformation transform;
        private static final float COLOR_R = (ArtofTntConfig.DISGUISE_COLOR_MASK.get() >> 16 & 255)/255F;
        private static final float COLOR_G = (ArtofTntConfig.DISGUISE_COLOR_MASK.get() >> 8 & 255)/255F;
        private static final float COLOR_B = (ArtofTntConfig.DISGUISE_COLOR_MASK.get() & 255)/255F;

        public ColorQuadTransformer(Transformation transform) {
            this.transform = transform;
        }

        private void processVertices(int[] inData, int[] outData, int color, boolean changeColor) {
            int stride = DefaultVertexFormat.BLOCK.getVertexSize();
            int count = (inData.length * 4) / stride;
            for (int i = 0; i < count; i++) {
                int offset = POSITION + i * stride;
                float x = Float.intBitsToFloat(getAtByteOffset(inData, offset));
                float y = Float.intBitsToFloat(getAtByteOffset(inData, offset + 4));
                float z = Float.intBitsToFloat(getAtByteOffset(inData, offset + 8));

                Vector4f pos = new Vector4f(x, y, z, 1);
                transform.transformPosition(pos);
                pos.perspectiveDivide();

                putAtByteOffset(outData, offset, Float.floatToRawIntBits(pos.x()));
                putAtByteOffset(outData, offset + 4, Float.floatToRawIntBits(pos.y()));
                putAtByteOffset(outData, offset + 8, Float.floatToRawIntBits(pos.z()));
            }

            for (int i = 0; i < count; i++) {
                int offset = NORMAL + i * stride;
                int normalIn = getAtByteOffset(inData, offset);
                if (normalIn != 0) {

                    float x = ((byte) ((normalIn) >> 24)) / 127.0f;
                    float y = ((byte) ((normalIn << 8) >> 24)) / 127.0f;
                    float z = ((byte) ((normalIn << 16) >> 24)) / 127.0f;

                    Vector3f pos = new Vector3f(x, y, z);
                    transform.transformNormal(pos);
                    pos.normalize();

                    int normalOut = ((((byte) (x / 127.0f)) & 0xFF) << 24) |
                            ((((byte) (y / 127.0f)) & 0xFF) << 16) |
                            ((((byte) (z / 127.0f)) & 0xFF) << 8) |
                            (normalIn & 0xFF);

                    putAtByteOffset(outData, offset, normalOut);
                }
            }

            float colorR = (color >> 16 & 255)/255F;
            float colorG = (color >> 8 & 255)/255F;
            float colorB = (color & 255)/255F;
            if(changeColor) {
                colorR = COLOR_R * colorR;
                colorG = COLOR_G * colorG;
                colorB = COLOR_B * colorB;
            }
            color = ((int)(colorB*255) << 16) + ((int)(colorG*255) << 8) + (int)(colorR*255);
            for (int i = 0; i < count; i++) {
                int offset = COLOR + i * stride;
                putAtByteOffset(outData, offset, color);
            }

        }

        private static int getAtByteOffset(int[] inData, int offset) {
            int index = offset / 4;
            int lsb = inData[index];

            int shift = (offset % 4) * 8;
            if (shift == 0)
                return inData[index];

            int msb = inData[index + 1];

            return (lsb >>> shift) | (msb << (32 - shift));
        }

        private static void putAtByteOffset(int[] outData, int offset, int value) {
            int index = offset / 4;
            int shift = (offset % 4) * 8;

            if (shift == 0) {
                outData[index] = value;
                return;
            }

            int lsbMask = 0xFFFFFFFF >>> (32 - shift);
            int msbMask = 0xFFFFFFFF << shift;

            outData[index] = (outData[index] & lsbMask) | (value << shift);
            outData[index + 1] = (outData[index + 1] & msbMask) | (value >>> (32 - shift));
        }

        private static int findPositionOffset(VertexFormat fmt) {
            int index;
            VertexFormatElement element = null;
            for (index = 0; index < fmt.getElements().size(); index++) {
                VertexFormatElement el = fmt.getElements().get(index);
                if (el.getUsage() == VertexFormatElement.Usage.POSITION) {
                    element = el;
                    break;
                }
            }
            if (index == fmt.getElements().size() || element == null)
                throw new RuntimeException("Expected vertex format to have a POSITION attribute");
            if (element.getType() != VertexFormatElement.Type.FLOAT)
                throw new RuntimeException("Expected POSITION attribute to have data type FLOAT");
            if (element.getByteSize() < 3)
                throw new RuntimeException("Expected POSITION attribute to have at least 3 dimensions");
            return fmt.getOffset(index);
        }

        private static int findNormalOffset(VertexFormat fmt) {
            int index;
            VertexFormatElement element = null;
            for (index = 0; index < fmt.getElements().size(); index++) {
                VertexFormatElement el = fmt.getElements().get(index);
                if (el.getUsage() == VertexFormatElement.Usage.NORMAL) {
                    element = el;
                    break;
                }
            }
            if (index == fmt.getElements().size() || element == null)
                throw new IllegalStateException("BLOCK format does not have normals?");
            if (element.getType() != VertexFormatElement.Type.BYTE)
                throw new RuntimeException("Expected NORMAL attribute to have data type BYTE");
            if (element.getByteSize() < 3)
                throw new RuntimeException("Expected NORMAL attribute to have at least 3 dimensions");
            return fmt.getOffset(index);
        }

        private static int findColorOffset(VertexFormat fmt) {
            int index;
            VertexFormatElement element = null;
            for (index = 0; index < fmt.getElements().size(); index++) {
                VertexFormatElement el = fmt.getElements().get(index);
                if (el.getUsage() == VertexFormatElement.Usage.COLOR) {
                    element = el;
                    break;
                }
            }
            if (index == fmt.getElements().size() || element == null)
                throw new IllegalStateException("BLOCK format does not have colors?");
            if (element.getType() != VertexFormatElement.Type.UBYTE)
                throw new RuntimeException("Expected COLOR attribute to have data type U_BYTE");
            if (element.getByteSize() < 4)
                throw new RuntimeException("Expected COLOR attribute to have at least 4 dimensions");
            return fmt.getOffset(index);
        }

        /**
         * Processes multiple quads, producing a new array of new quads.
         *
         * @param inputs The list of quads to transform
         * @return A new array of new BakedQuad objects.
         */
        public List<BakedQuad> processMany(boolean changeColor, List<BakedQuad> inputs, @Nullable BlockState state, @Nullable ItemStack stack) {
            if (inputs.size() == 0)
                return Collections.emptyList();

            List<BakedQuad> outputs = Lists.newArrayList();
            for (BakedQuad input : inputs) {
                int color = -1;
                if(input.isTinted()) {
                    if(state !=null) {
                        color = Minecraft.getInstance().getBlockColors().getColor(state, null, null, input.getTintIndex());
                    } else if(stack!=null){
                        color = Minecraft.getInstance().getItemColors().getColor(stack, input.getTintIndex());
                    }
                }
                int[] inData = input.getVertices();
                int[] outData = Arrays.copyOf(inData, inData.length);
                processVertices(inData, outData, color, changeColor);

                outputs.add(new BakedQuad(outData, input.getTintIndex(), input.getDirection(), input.getSprite(), input.isShade()));
            }
            return outputs;
        }

    }

}
