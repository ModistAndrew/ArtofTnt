package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;

public class CustomExplosionBlockEvent extends CustomExplosionEvent{
    public final BlockPos pos;
    public final float percentage;

    public CustomExplosionBlockEvent(CustomExplosion explosion, BlockPos pos, float percentage) {
        super(explosion);
        this.pos = pos;
        this.percentage = percentage;
    }
}
