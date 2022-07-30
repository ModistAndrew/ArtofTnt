package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OctahedronDfsExplosionShape extends AbstractDfsExplosionShape {
    public OctahedronDfsExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    public Set<BlockPos> getEdge() {
        int r = (int) radius;
        return BlockPos.betweenClosedStream(p(-r, -r, -r), p(r, r, r)).filter(bp -> center.distManhattan(bp)==r)
                .map(BlockPos::immutable).collect(Collectors.toSet());
    }

    @Override
    protected List<Entity> getEntities() {
        int r = (int) radius;
        return level.getEntities(explosion.getSource(), new AABB(pc(-r, -r, -r), pc(r, r, r))).stream()
                .filter(e -> center.distManhattan(e.blockPosition()) <= r).collect(Collectors.toList());
    }

    @Override
    protected int getActualRadius() {
        return (int) radius;
    }

}
