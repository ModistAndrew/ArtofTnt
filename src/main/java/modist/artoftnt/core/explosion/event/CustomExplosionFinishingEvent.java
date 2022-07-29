package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;

public class CustomExplosionFinishingEvent extends CustomExplosionEvent{
    public CustomExplosionFinishingEvent(CustomExplosion explosion) {
        super(explosion);
    }
}
