package modist.artoftnt.core.explosion.event;

import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class PrimedTntFrameHitEntityEvent extends PrimedTntFrameEvent {
    public final EntityHitResult result;
    public PrimedTntFrameHitEntityEvent(PrimedTntFrame tnt, EntityHitResult result) {
        super(tnt);
        this.result = result;
    }
}
