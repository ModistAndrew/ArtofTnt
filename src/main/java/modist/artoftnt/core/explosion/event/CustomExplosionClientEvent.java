package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;

public class CustomExplosionClientEvent extends CustomExplosionEvent{
    public CustomExplosionClientEvent(CustomExplosion explosion) {
        super(explosion);
    }
}
