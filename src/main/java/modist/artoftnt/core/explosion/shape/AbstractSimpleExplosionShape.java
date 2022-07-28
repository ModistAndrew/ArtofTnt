package modist.artoftnt.core.explosion.shape;

import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractSimpleExplosionShape extends AbstractExplosionShape{
    public AbstractSimpleExplosionShape(CustomExplosion explosion) {
        super(explosion);
    }

    @Override
    protected void generateToBlowBlocks() {
        for(BlockPos pos : getBlockPoses()){
            float distancePercentage = toOne((float) (pos.distSqr(this.center)/getActualRadius()));
            Optional<Float> optional = EXPLOSION_DAMAGE_CALCULATOR.getBlockExplosionResistance(explosion, this.level, pos,
                    level.getBlockState(pos), level.getFluidState(pos));
            float resistance = optional.map(aFloat -> (aFloat + 0.3F) * 0.3F).orElse(0.09F);
            if (!this.level.getFluidState(pos).isEmpty()) {
                resistance = interpolate(resistance, 0.09F, fluidFactor / AdditionType.TEMPERATURE.maxValue);
            }
            if(distancePercentage * strengthFactor  > resistance) {
                this.toBlowBlocks.put(pos, distancePercentage * strengthFactor);
            }
        }
    }

    protected abstract Set<BlockPos> getBlockPoses();

    protected abstract int getActualRadius();

    protected abstract List<Entity> getEntities();

    @Override
    protected void generateToBlowEntities() {
        int r = getActualRadius();
        for(Entity e : getEntities()){
            float distancePercentage = toOne(1-(float)(this.centerVec.distanceTo(e.position()) / r));
            if(distancePercentage > 0) {
                this.toBlowEntities.put(e, distancePercentage);
            }
        }
    }
}
