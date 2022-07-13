package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.explosion.AbstractExplosionShape;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import java.util.HashSet;
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
            int yLim = (int) (Math.sqrt(r * r - dx * dx));
            for (int dy = -yLim; dy <= yLim; dy++) {
                int zLim = (int) Math.sqrt(r * r - dx * dx - dy * dy);
                ret.add(p(dx, dy, zLim));
                ret.add(p(dx, dy, -zLim));
            }
        }
        return ret;
    }
}

