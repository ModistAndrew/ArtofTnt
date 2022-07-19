package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.core.BlockPos;

public class CustomExplosionBlockBreakEvent extends CustomExplosionBlockEvent{
    public CustomExplosionBlockBreakEvent(CustomExplosion explosion, BlockPos pos, float percentage) {
        super(explosion, pos, percentage);
    }

    public static class Pre extends CustomExplosionBlockBreakEvent{
        public Pre(CustomExplosion explosion, BlockPos pos, float percentage) {
            super(explosion, pos, percentage);
        }
    }

    public static class Post extends CustomExplosionBlockBreakEvent{

        public Post(CustomExplosion explosion, BlockPos pos, float percentage) {
            super(explosion, pos, percentage);
        }
    }
}
