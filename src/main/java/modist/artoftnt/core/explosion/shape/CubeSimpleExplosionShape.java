package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CubeSimpleExplosionShape extends AbstractSimpleExplosionShape {
    public CubeSimpleExplosionShape(CustomExplosion explosion) {
        super(explosion);
        this.useDirection = true;
    }

    @Override
    public Set<BlockPos> getBlockPoses() {
        Set<BlockPos> ret = new HashSet<>();
        int xn = -(int)(radius+directionRadii[4]);
        int xp = (int)(radius+directionRadii[5]);
        int yn = -(int)(radius+directionRadii[0]);
        int yp = (int)(radius+directionRadii[1]);
        int zn = -(int)(radius+directionRadii[2]);
        int zp = (int)(radius+directionRadii[3]);
        for(int i = xn; i <= xp; i++) {
            for (int j = yn; j <= yp; j++) {
                for (int k = zn; k <= zp; k++) {
                    ret.add(p(i, j, k));
                }
            }
        }
        return ret;
    }

    @Override
    protected List<Entity> getEntities() {
        int xn = -(int)(radius+directionRadii[4]);
        int xp = (int)(radius+directionRadii[5]);
        int yn = -(int)(radius+directionRadii[0]);
        int yp = (int)(radius+directionRadii[1]);
        int zn = -(int)(radius+directionRadii[2]);
        int zp = (int)(radius+directionRadii[3]);
        return level.getEntities(explosion.getSource(), new AABB(pc(xn, yn, zn), pc(xp, yp, zp)));
    }

    @Override
    protected int getActualRadius() {
        int xn = (int)(radius+directionRadii[4]);
        int xp = (int)(radius+directionRadii[5]);
        int yn = (int)(radius+directionRadii[0]);
        int yp = (int)(radius+directionRadii[1]);
        int zn = (int)(radius+directionRadii[2]);
        int zp = (int)(radius+directionRadii[3]);
        return (int)(Math.ceil(Math.sqrt(Math.max(xn, xp)*Math.max(xn, xp) +
                Math.max(yn, yp)*Math.max(yn, yp) +
                Math.max(zn, zp)*Math.max(zn, zp))));
    }

}
