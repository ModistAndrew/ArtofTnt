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
import java.util.stream.Collectors;

public class SphereDfsExplosionShape extends AbstractDfsExplosionShape {

    public SphereDfsExplosionShape(CustomExplosion explosion) {
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
    protected List<Entity> getEntities() {
        int r = (int) radius;
        return level.getEntities(explosion.getSource(), new AABB(pc(-r, -r, -r), pc(r, r, r))).stream()
                .filter(e -> centerVec.distanceTo(e.position()) <= r).collect(Collectors.toList());
    }

    @Override
    protected int getActualRadius() {
        return (int) radius;
    }
}