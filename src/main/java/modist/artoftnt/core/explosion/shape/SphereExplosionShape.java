package modist.artoftnt.core.explosion.shape;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SphereExplosionShape extends AbstractExplosionShape {

    public SphereExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    public Set<BlockPos> getEdge() {
        Set<BlockPos> ret = new HashSet<>();
        int r = (int) radius;
        for (int dx = -r; dx <= r; dx++) {
            for(int dy = -r; dy <= r; dy++){
                for(int dz = -r; dz <= r; dz++){
                    if(r==(int)Math.sqrt(dx*dx+dy*dy+dz*dz)){
                        ret.add(p(dx, dy, dz));
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public Object2FloatMap<Entity> getEntities() {
        Object2FloatMap<Entity> ret = new Object2FloatOpenHashMap<>();
        int r = (int) radius;
        List<Entity> list = this.level.getEntities(this.explosion.getSource(),
                new AABB(p(-r, -r, -r), p(r, r, r)));
        for(Entity e : list){
            float distancePercentage = (float) (Math.sqrt(e.distanceToSqr(this.centerVec)) / r);
            if(!e.ignoreExplosion() && distancePercentage<=1F){ //in
                float seenPercentage = getSeenPercent(this.centerVec, e);
                ret.put(e, (1-distancePercentage)*toOne(seenPercentage));
            }
        }
        return ret;
    }
}

