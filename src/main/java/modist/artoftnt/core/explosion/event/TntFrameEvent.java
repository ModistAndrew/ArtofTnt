package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraftforge.eventbus.api.Event;

public class TntFrameEvent extends Event {
    public final AdditionStack data;
    public TntFrameEvent(AdditionStack data) {
        this.data = data;
    }
}
