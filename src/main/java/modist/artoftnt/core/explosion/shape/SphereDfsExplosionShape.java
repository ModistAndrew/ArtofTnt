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
        int r = (int) radius;
        return BlockPos.betweenClosedStream(p(-r, -r, -r), p(r, r, r))
                .filter(bp -> (int)Math.sqrt(center.distSqr(bp)) == r)
                .map(bp -> bp.immutable()).collect(Collectors.toSet());
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