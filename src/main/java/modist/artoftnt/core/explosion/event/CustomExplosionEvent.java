package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class CustomExplosionEvent extends Event {
    public final CustomExplosion explosion;
    public final AdditionStack data;

    public CustomExplosionEvent(CustomExplosion explosion) {
        this.explosion = explosion;
        this.data = explosion.getAdditionStack();
    }

}
