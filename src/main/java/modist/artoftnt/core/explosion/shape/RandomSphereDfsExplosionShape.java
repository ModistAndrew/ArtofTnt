package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;

import java.util.Set;
import java.util.stream.Collectors;

public class RandomSphereDfsExplosionShape extends SphereDfsExplosionShape {

    public RandomSphereDfsExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    public Set<BlockPos> getEdge() {
        int r = (int) radius;
        int rm = r + r/4;
        return BlockPos.betweenClosedStream(p(-rm, -rm, -rm), p(rm, rm, rm))
                .filter(bp -> r<=(int)Math.sqrt(center.distSqr(bp)) &&
                        (int)Math.sqrt(center.distSqr(bp))<=r+explosion.random.nextInt(1 + r / 4))
                .map(BlockPos::immutable).collect(Collectors.toSet());
    }
}