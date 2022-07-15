package modist.artoftnt.core.explosion.shape;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import modist.artoftnt.core.explosion.AbstractExplosionShape;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CubeExplosionShape extends AbstractExplosionShape {

    public CubeExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    public Set<BlockPos> getEdge() {
        Set<BlockPos> ret = new HashSet<>();
        int r = (int) radius;
        for(int i = -r; i <= r; i++){
            for(int j = -r; j <= r; j++){
                ret.add(p(i, j, r));
                ret.add(p(i, j, -r));
                ret.add(p(i, r, j));
                ret.add(p(i, -r, j));
                ret.add(p(r, i, j));
                ret.add(p(-r, i, j));
            }
        }
        return ret;
    }

    @Override
    protected Object2FloatMap<Entity> getEntities() {
        Object2FloatMap<Entity> ret = new Object2FloatOpenHashMap<>();
        int r = (int) radius;
        List<Entity> list = this.level.getEntities(this.explosion.getSource(),
                new AABB(p(-r, -r, -r), p(r, r, r)));
        for(Entity e : list){
            float distancePercentage = (float) (Math.sqrt(e.distanceToSqr(this.centerVec)) / r);
                float seenPercentage = getSeenPercent(this.centerVec, e);
                ret.put(e, (1-distancePercentage)*toOne(seenPercentage));
        }
        return ret;
    }
}

