package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class CustomExplosionEvent extends TntFrameEvent {
    public final CustomExplosion explosion;

    public CustomExplosionEvent(CustomExplosion explosion){
        super(explosion.getAdditionStack());
        this.explosion = explosion;
    }

}
