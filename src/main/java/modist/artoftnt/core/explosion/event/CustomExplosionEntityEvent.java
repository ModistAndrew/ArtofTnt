package modist.artoftnt.core.explosion.event;

import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.world.entity.Entity;

public class CustomExplosionEntityEvent extends CustomExplosionEvent{
    public final Entity entity;
    public final float percentage;

    public CustomExplosionEntityEvent(CustomExplosion explosion, Entity entity, float percentage) {
        super(explosion);
        this.entity = entity;
        this.percentage = percentage;
    }

}
