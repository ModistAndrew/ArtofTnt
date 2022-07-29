package modist.artoftnt.core.explosion.event;

import modist.artoftnt.common.entity.PrimedTntFrame;

public class PrimedTntFrameEvent extends TntFrameEvent{
    public final PrimedTntFrame tnt;

    public PrimedTntFrameEvent(PrimedTntFrame tnt) {
        super(tnt.data.additions);
        this.tnt = tnt;
    }
}
