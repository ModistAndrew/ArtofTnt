package modist.artoftnt.core.explosion;

import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public abstract class AbstractExplosionShape {
    protected final Level level;
    protected final BlockPos center;
    protected final Vec3 centerVec;
    protected  final int x;
    protected  final int y;
    protected  final int z;
    protected final float radius;
    protected final float[] directionRadii;
    protected final float fluidFactor;
    protected final float piercingFactor;
    protected final float strengthFactor;
    protected final HashMap<BlockPos, ExplosionData> toBlowData = new HashMap<>();
    protected final Set<BlockPos> toBlow = new HashSet<>();
    protected final CustomExplosion explosion;
    protected static final ExplosionDamageCalculator EXPLOSION_DAMAGE_CALCULATOR = new ExplosionDamageCalculator();

    public AbstractExplosionShape(CustomExplosion explosion) {
        this.explosion = explosion;
        this.level = explosion.level;
        this.center = new BlockPos(explosion.x, explosion.y, explosion.z);
        this.centerVec = new Vec3(explosion.x, explosion.y, explosion.z);
        this.x = center.getX();
        this.y = center.getY();
        this.z = center.getZ();
        this.radius = explosion.radius;
        this.directionRadii = explosion.directionRadii;
        this.fluidFactor = explosion.temperature;
        this.piercingFactor = explosion.piercing;
        this.strengthFactor = explosion.strength;
    }

    protected abstract Set<BlockPos> getEdge();

    protected abstract Object2FloatMap<Entity> getEntities();

    public HashMap<BlockPos, ExplosionData> getToBlowData(){
        return toBlowData;
    }

    public Set<BlockPos> getToBlow(){
        return toBlow;
    }

    public void generateData(){
        putData(strengthFactor, center);
        for(BlockPos pos: this.getEdge()){
            this.dfs(pos);
        }
    }

    protected BlockPos p(int x, int y, int z){
        return new BlockPos(this.center.getX()+x,
                this.center.getY()+y,
                this.center.getZ()+z);
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

            for(double d5 = 0.0D; d5 <= 1.0D; d5 += d0) {
                for(double d6 = 0.0D; d6 <= 1.0D; d6 += d1) {
                    for(double d7 = 0.0D; d7 <= 1.0D; d7 += d2) {
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

            return (float)i / (float)j;
        } else {
            return 0.0F;
        }
    }

    protected void dfs(BlockPos from){
        if(toBlowData.containsKey(from)){ //contains, return
            return;
        }
        BlockPos delta = center.subtract(from);
        float d = center.distManhattan(from);
        float px = Math.abs(delta.getX()/d);
        float py = Math.abs(delta.getY()/d);
        float pz = Math.abs(delta.getZ()/d);
        BlockPos neighborX = from.offset(Integer.compare(delta.getX(), 0), 0, 0);
        BlockPos neighborY = from.offset(0, Integer.compare(delta.getY(), 0), 0);
        BlockPos neighborZ = from.offset(0, 0, Integer.compare(delta.getZ(), 0));
        float sx = 0;
        float sy = 0;
        float sz = 0;
        if(px!=0){
            dfs(neighborX);
            sx = px*toBlowData.get(neighborX).strengthNext;
        }
        if(py!=0){
            dfs(neighborY);
            sy = py*toBlowData.get(neighborY).strengthNext;
        }
        if(pz!=0){
            dfs(neighborZ);
            sz = pz*toBlowData.get(neighborZ).strengthNext;
        }
        putData(sx+sy+sz, from);
    }

    protected void putData(float strength, BlockPos pos){ //generate strengthNext and ratioNext and put
        Optional<Float> optional = EXPLOSION_DAMAGE_CALCULATOR.getBlockExplosionResistance(explosion, this.level, pos,
                level.getBlockState(pos), level.getFluidState(pos));

        float resistance = optional.isPresent() ? (optional.get() + 0.3F) * 0.3F : 0.09F; //TODO stone is too high?
        if(!this.level.getFluidState(pos).isEmpty()){
            resistance = interpolate(resistance, 0.09F, fluidFactor/AdditionType.TEMPERATURE.maxValue);
        }
        if(strength>=resistance){
            toBlow.add(pos);
        }
        int sameCount = 0;
        if (pos.getX() == center.getX()){sameCount++;}
        if (pos.getY() == center.getY()){sameCount++;}
        if (pos.getZ() == center.getZ()){sameCount++;}
        float strengthNext = strength - (sameCount >= 2 ? 2 : 1)*Math.min(resistance, toZero(strengthFactor*0.8F) + 0.09F);
        toBlowData.put(pos, new ExplosionData(strength, strengthNext));
    }

    protected float interpolate(float origin, float to, float percentage){
        return origin + percentage * (to-origin);
    }

    protected float toOne(float origin){
        return interpolate(origin, 1, piercingFactor/AdditionType.PIERCING.maxValue);
    }

    protected float toZero(float origin){
        return interpolate(origin, 0, piercingFactor/AdditionType.PIERCING.maxValue);
    }

    public record ExplosionData(float strength, float strengthNext){
    }

}
