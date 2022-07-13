package modist.artoftnt.core.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractExplosionShape {
    protected final Level level;
    protected final BlockPos center;
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
        this.radius = explosion.radius;
        this.directionRadii = explosion.directionRadii;
        this.fluidFactor = 0;
        this.piercingFactor = 0;
        this.strengthFactor = explosion.strength;
        generateData();
    }

    protected abstract Set<BlockPos> getEdge();

    public HashMap<BlockPos, ExplosionData> getToBlowData(){
        return toBlowData;
    }

    public Set<BlockPos> getToBlow(){
        return toBlow;
    }

    protected void generateData(){
        putData(strengthFactor, 1, center);
        for(BlockPos pos: this.getEdge()){
            this.dfs(pos);
        }
    }

    protected BlockPos p(int x, int y, int z){
        return new BlockPos(this.center.getX()+x,
                this.center.getY()+y,
                this.center.getZ()+z);
    }

    protected void dfs(BlockPos from){
        if(toBlowData.containsKey(from)){ //contains, return
            return;
        }
        BlockPos neighbor = getNeighbor(from);
        dfs(neighbor);
        putData(toBlowData.get(neighbor).strengthNext, toBlowData.get(neighbor).ratioNext, from);
    }

    protected BlockPos getNeighbor(BlockPos from){
        int dx = Math.abs(center.getX() - from.getX());
        int dy = Math.abs(center.getY() - from.getY());
        int dz = Math.abs(center.getZ() - from.getZ());
        int sx = Integer.compare(center.getX() - from.getX(), 0);
        int sy = Integer.compare(center.getY() - from.getY(), 0);
        int sz = Integer.compare(center.getZ() - from.getZ(), 0);
        return from.offset(dx >= dy && dx >= dz ? sx : 0,
                dy >= dx && dy >= dz ? sy : 0,
                dz >= dx && dz >= dy ? sz : 0);
    }

    protected void putData(float strength, float ratio, BlockPos pos){ //generate strengthNext and ratioNext and put
        Optional<Float> optional = EXPLOSION_DAMAGE_CALCULATOR.getBlockExplosionResistance(explosion, this.level, pos,
                level.getBlockState(pos), level.getFluidState(pos));
        float strengthNext = strength;
        float ratioNext = ratio;
        if (optional.isPresent()) {
            strengthNext -= (optional.get() + 0.3F) * 0.3F;
            ratioNext = strengthNext/strengthFactor;
        }
        toBlowData.put(pos, new ExplosionData(strength, ratio, strengthNext, ratioNext));
        if(strengthNext>=0){
            toBlow.add(pos);
        }
    }

    public record ExplosionData(float strength, float ratio, float strengthNext, float ratioNext){
    }

}
