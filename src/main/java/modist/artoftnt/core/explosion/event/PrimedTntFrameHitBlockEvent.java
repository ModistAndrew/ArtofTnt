package modist.artoftnt.core.explosion.event;

import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.world.phys.BlockHitResult;

public class PrimedTntFrameHitBlockEvent extends PrimedTntFrameEvent {
    public final BlockHitResult result;

    public PrimedTntFrameHitBlockEvent(PrimedTntFrame tnt, BlockHitResult result) {
        super(tnt);
        this.result = result;
    }

    public static class Pre extends PrimedTntFrameHitBlockEvent {
        public Pre(PrimedTntFrame tnt, BlockHitResult result) {
            super(tnt, result);
        }
    }

    public static class Post extends PrimedTntFrameHitBlockEvent {
        public Post(PrimedTntFrame tnt, BlockHitResult result) {
            super(tnt, result);
        }
    }
}
