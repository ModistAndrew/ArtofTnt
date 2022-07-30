package modist.artoftnt.core.explosion.shape;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractExplosionShape {
    protected final Level level;
    protected final BlockPos center;
    protected final Vec3 centerVec;
    protected  final int x;
    protected  final int y;
    protected  final int z;
    protected final float radius;
    protected final float[] directionRadii = new float[6];
    protected final float fluidFactor;
    protected final float piercingFactor;
    protected final float strengthFactor;
    protected final CustomExplosion explosion;
    protected Object2FloatMap<Entity> toBlowEntities;
    protected Object2FloatMap<BlockPos> toBlowBlocks;
    protected static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();
    protected boolean useDirection;

    public AbstractExplosionShape(CustomExplosion explosion) {
        this.explosion = explosion;
        this.level = explosion.level;
        this.center = new BlockPos(explosion.x, explosion.y, explosion.z);
        this.centerVec = new Vec3(explosion.x, explosion.y, explosion.z);
        this.x = center.getX();
        this.y = center.getY();
        this.z = center.getZ();
        AdditionStack stack = explosion.getAdditionStack();
        this.radius = stack.getValue(AdditionType.RANGE);
        this.strengthFactor = stack.getValue(AdditionType.STRENGTH);
        this.piercingFactor = stack.getValue(AdditionType.PIERCING);
        this.fluidFactor = stack.getValue(AdditionType.TEMPERATURE);
        this.directionRadii[0] = stack.getValue(AdditionType.DOWN);
        this.directionRadii[1] = stack.getValue(AdditionType.UP);
        this.directionRadii[2] = stack.getValue(AdditionType.NORTH);
        this.directionRadii[3] = stack.getValue(AdditionType.SOUTH);
        this.directionRadii[4] = stack.getValue(AdditionType.WEST);
        this.directionRadii[5] = stack.getValue(AdditionType.EAST);
    }

    public Object2FloatMap<Entity> getToBlowEntities() {
        if(toBlowEntities==null){
            toBlowEntities = new Object2FloatOpenHashMap<>();
            if(hasRadius()) {
                generateToBlowEntities();
            }
        }
        return toBlowEntities;
    }

    protected abstract void generateToBlowEntities();

    public Object2FloatMap<BlockPos> getToBlowBlocks() {
        if(toBlowBlocks==null){
            toBlowBlocks = new Object2FloatOpenHashMap<>();
            if(strengthFactor > 0 && hasRadius()) {
                generateToBlowBlocks();
            }
        }
        return toBlowBlocks;
    }

    private boolean hasRadius(){
        boolean ret = radius > 0;
        if(useDirection) {
            for (int i = 0; i < 6; i++) {
                ret |= directionRadii[i] > 0;
            }
        }
        return ret;
    }

    protected abstract void generateToBlowBlocks();

    protected BlockPos p(int x, int y, int z){
        return new BlockPos(this.center.getX()+x,
                this.center.getY()+y,
                this.center.getZ()+z);
    }

    protected Vec3 pc(int x, int y, int z){
        return Vec3.atCenterOf(p(x, y, z));
    }

    protected float interpolate(float origin, float to, float percentage){
        return origin + percentage * (to-origin);
    }

    protected float interpolate(float origin, float to){
        return interpolate(origin, to, piercingFactor/AdditionType.PIERCING.maxValue);
    }

    protected float toOne(float origin){
        return interpolate(origin, 1, piercingFactor/AdditionType.PIERCING.maxValue);
    }

    protected float toZero(float origin){
        return interpolate(origin, 0, piercingFactor/AdditionType.PIERCING.maxValue);
    }

    public static float getSeenPercent(Vec3 pExplosionVector, Entity pEntity) {
        AABB aabb = pEntity.getBoundingBox();
        double d0 = 1.0D / ((aabb.maxX - aabb.minX) * 2.0D + 1.0D);
        double d1 = 1.0D / ((aabb.maxY - aabb.minY) * 2.0D + 1.0D);
        double d2 = 1.0D / ((aabb.maxZ - aabb.minZ) * 2.0D + 1.0D);
        double d3 = (1.0D - Math.floor(1.0D / d0) * d0) / 2.0D;
        double d4 = (1.0D - Math.floor(1.0D / d2) * d2) / 2.0D;
        if (!(d0 < 0.0D) && !(d1 < 0.0D) && !(d2 < 0.0D)) {
            int i = 0;
            int j = 0;

            for (double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
                for (double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
                    for (double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
                        double d8 = Mth.lerp(d5, aabb.minX, aabb.maxX);
                        double d9 = Mth.lerp(d6, aabb.minY, aabb.maxY);
                        double d10 = Mth.lerp(d7, aabb.minZ, aabb.maxZ);
                        Vec3 vec3 = new Vec3(d8 + d3, d9, d10 + d4);
                        if (pEntity.level.clip(new ClipContext(vec3, pExplosionVector, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pEntity)).getType() == HitResult.Type.MISS) {
                            ++i;
                        }

                        ++j;
                    }
                }
            }

            return (float) i / (float) j;
        } else {
            return 0.0F;
        }
    }
}
