package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;

public class CustomExplosionBlockBreakEvent extends CustomExplosionBlockEvent{
    public CustomExplosionBlockBreakEvent(CustomExplosion explosion, BlockPos pos, float percentage) {
        super(explosion, pos, percentage);
    }
}
