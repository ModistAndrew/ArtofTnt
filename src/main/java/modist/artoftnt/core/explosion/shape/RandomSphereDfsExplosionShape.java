package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RandomSphereDfsExplosionShape extends SphereDfsExplosionShape {

    public RandomSphereDfsExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    public Set<BlockPos> getEdge() {
        Set<BlockPos> ret = new HashSet<>();
        int r = (int) radius;
        int rm = r + r/4;
        for (int dx = -rm; dx <= rm; dx++) {
            for(int dy = -rm; dy <= rm; dy++){
                for(int dz = -rm; dz <= rm; dz++){
                    if((int)Math.sqrt(dx*dx+dy*dy+dz*dz)-r<=explosion.random.nextInt(1 + r / 4)){
                        ret.add(p(dx, dy, dz));
                    }
                }
            }
        }
        return ret;
    }
}