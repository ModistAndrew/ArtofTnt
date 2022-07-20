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

public class CubeExplosionShape extends AbstractExplosionShape {

    public CubeExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    public Set<BlockPos> getEdge() {
        Set<BlockPos> ret = new HashSet<>();
        int xn = -(int)(radius+directionRadii[4]);
        int xp = (int)(radius+directionRadii[5]);
        int yn = -(int)(radius+directionRadii[0]);
        int yp = (int)(radius+directionRadii[1]);
        int zn = -(int)(radius+directionRadii[2]);
        int zp = (int)(radius+directionRadii[3]);
        for(int i = xn; i <= xp; i++){
            for(int j = yn; j <= yp; j++){
                ret.add(p(i, j, zn));
                ret.add(p(i, j, zp));
            }
        }
        for(int i = xn; i <= xp; i++){
            for(int j = zn; j <= zp; j++){
                ret.add(p(i, yn, j));
                ret.add(p(i, yp, j));
            }
        }
        for(int i = yn; i <= yp; i++){
            for(int j = zn; j <= zp; j++){
                ret.add(p(xn, i, j));
                ret.add(p(xp, i, j));
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
                float seenPercentage = getSeenPercent(this.centerVec, e);
                ret.put(e, (1-distancePercentage)*toOne(seenPercentage));
        }
        return ret;
    }
}

