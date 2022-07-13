package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.explosion.AbstractExplosionShape;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
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
}

