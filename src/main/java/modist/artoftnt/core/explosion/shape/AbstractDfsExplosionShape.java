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

import java.util.*;

public abstract class AbstractDfsExplosionShape extends AbstractExplosionShape {
    protected final HashMap<BlockPos, ExplosionData> toBlowData = new HashMap<>();
    protected final Set<BlockPos> toBlow = new HashSet<>();


    public AbstractDfsExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    protected abstract Set<BlockPos> getEdge();

    @Override
    public void generateToBlowBlocks() {
        putData(strengthFactor, center);
        for (BlockPos pos : this.getEdge()) {
            this.dfs(pos);
        }
        this.toBlowData.forEach((bp, data) -> {
            if (toBlow.contains(bp)) { //toBlowData may contain for calculation while toBlow does not contain
                this.toBlowBlocks.put(bp, data.strength);
            }
        }); //fill toBlockBlocks
    }

    @Override
    public void generateToBlowEntities() {
        int r = getActualRadius();
        for(Entity e : getEntities()){
            float distancePercentage = (float) (this.centerVec.distanceTo(e.position()) / r);
            float percentage = toOne((1-distancePercentage) * getSeenPercent(this.centerVec, e));
            if(percentage > 0) {
                this.toBlowEntities.put(e, percentage);
            }
        }
    }

    protected abstract List<Entity> getEntities();

    protected abstract int getActualRadius();

    protected void dfs(BlockPos from) {
        if (toBlowData.containsKey(from)) { //contains, return
            return;
        }
        BlockPos delta = center.subtract(from);
        float d = center.distManhattan(from);
        float px = Math.abs(delta.getX() / d);
        float py = Math.abs(delta.getY() / d);
        float pz = Math.abs(delta.getZ() / d);
        BlockPos neighborX = from.offset(Integer.compare(delta.getX(), 0), 0, 0);
        BlockPos neighborY = from.offset(0, Integer.compare(delta.getY(), 0), 0);
        BlockPos neighborZ = from.offset(0, 0, Integer.compare(delta.getZ(), 0));
        float sx = 0;
        float sy = 0;
        float sz = 0;
        if (px != 0) {
            dfs(neighborX);
            sx = px * toBlowData.get(neighborX).strengthNext;
        }
        if (py != 0) {
            dfs(neighborY);
            sy = py * toBlowData.get(neighborY).strengthNext;
        }
        if (pz != 0) {
            dfs(neighborZ);
            sz = pz * toBlowData.get(neighborZ).strengthNext;
        }
        putData(sx + sy + sz, from);
    }

    protected void putData(float strength, BlockPos pos) { //generate strengthNext and ratioNext and put
        Optional<Float> optional = EXPLOSION_DAMAGE_CALCULATOR.getBlockExplosionResistance(explosion, this.level, pos,
                level.getBlockState(pos), level.getFluidState(pos));

        float resistance = optional.isPresent() ? (optional.get() + 0.3F) * 0.3F : 0.09F; //TODO stone is too high?
        if (!this.level.getFluidState(pos).isEmpty()) {
            resistance = interpolate(resistance, 0.09F, fluidFactor / AdditionType.TEMPERATURE.maxValue);
        }
        if (strength >= resistance) {
            toBlow.add(pos);
        }
        int sameCount = 0;
        if (pos.getX() == center.getX()) {
            sameCount++;
        }
        if (pos.getY() == center.getY()) {
            sameCount++;
        }
        if (pos.getZ() == center.getZ()) {
            sameCount++;
        }
        float strengthNext = strength - (sameCount >= 2 ? 2 : 1) * Math.min(resistance, toZero(strengthFactor * 0.8F) + 0.09F);
        toBlowData.put(pos, new ExplosionData(strength, strengthNext));
    }

    public record ExplosionData(float strength, float strengthNext) {
    }

}
