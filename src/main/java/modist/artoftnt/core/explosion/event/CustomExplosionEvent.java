package modist.artoftnt.core.explosion.event;

import modist.artoftnt.common.block.entity.TntFrameData;
import modist.artoftnt.core.explosion.CustomExplosion;

public class CustomExplosionEvent extends TntFrameEvent{ //TODO cancelable
    public final CustomExplosion explosion;

    public CustomExplosionEvent(CustomExplosion explosion) {
        super(explosion.getAdditionStack());
        this.explosion = explosion;
    }

}
