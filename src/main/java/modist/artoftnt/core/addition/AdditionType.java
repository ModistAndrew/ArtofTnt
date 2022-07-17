package modist.artoftnt.core.addition;

import java.util.Locale;
import java.util.function.BiFunction;

public enum AdditionType {
    LOUDNESS(8, AdditionSlot.EXPLOSION_SOUND), SOUND_TYPE(8, AdditionSlot.EXPLOSION_SOUND),

    VELOCITY(16, 0.2F, 10F, AdditionSlot.TNT_MOTION), ELASTICITY(8, AdditionSlot.TNT_MOTION),
    STICKINESS(8, AdditionSlot.TNT_MOTION), LIGHTNESS(8, AdditionSlot.TNT_MOTION),

    PUNCH(16, AdditionSlot.ENTITY_IMPACT), DRAW(8, AdditionSlot.ENTITY_IMPACT), TNT_PUNCH(8, AdditionSlot.ENTITY_IMPACT),
    TNT_DRAW(8, AdditionSlot.ENTITY_IMPACT),

    INSTABILITY(16, AdditionSlot.TNT_STABILITY),

    RANGE(16, AdditionSlot.EXPLOSION_RANGE), DOWN(8, AdditionSlot.EXPLOSION_RANGE), UP(8, AdditionSlot.EXPLOSION_RANGE),
    NORTH(8, AdditionSlot.EXPLOSION_RANGE), SOUTH(8, AdditionSlot.EXPLOSION_RANGE), WEST(8, AdditionSlot.EXPLOSION_RANGE),
    EAST(8, AdditionSlot.EXPLOSION_RANGE),

    ENTITY_RANGE(16, AdditionSlot.ENTITY_RANGE), PIERCING(8, 0F, 8F, AdditionSlot.ENTITY_RANGE),

    STRENGTH(16, AdditionSlot.EXPLOSION_STRENGTH),

    DAMAGE(16, AdditionSlot.ENTITY_EFFECT), POTION(8, AdditionSlot.ENTITY_EFFECT),

    DROP(8, AdditionSlot.BLOCK_DROP), CONTAINER(1, AdditionSlot.BLOCK_DROP),

    FLAME(8, AdditionSlot.EXPLOSION_FLAME), TEMPERATURE(8, 0F, 8F, AdditionSlot.EXPLOSION_FLAME), LIGHTNING(8, AdditionSlot.EXPLOSION_FLAME),

    EXPLOSION_COUNT(8, 1, 8, AdditionSlot.EXPLOSION_DURATION), EXPLOSION_INTERVAL(8, 1, 10, AdditionSlot.EXPLOSION_DURATION),

    FIREWORK(8, AdditionSlot.EXPLOSION_EFFECT), PARTICLE(8, AdditionSlot.EXPLOSION_EFFECT),
    TNT_PARTICLE(8, AdditionSlot.ENTITY_EFFECT),

    FUSE(16, AdditionSlot.TNT_FUSE), RANDOMNESS(8, AdditionSlot.TNT_FUSE), EXPLOSION_RANDOMNESS(8, AdditionSlot.TNT_FUSE),

    SHAPE(1, AdditionSlot.EXPLOSION_SHAPE);

    public final int maxCount;
    public final float initialValue;
    public final float maxValue;
    public final AdditionSlot slot;
    public final BiFunction<Float, Float, Float> weightEffect; //how value is affected by weight, also apply to original and max

    AdditionType(int maxCount, AdditionSlot slot) {
        this(maxCount, 0, Float.MAX_VALUE, slot);
    }

    AdditionType(int maxCount, float initialValue, float maxValue, AdditionSlot slot) {
        this.initialValue = initialValue;
        this.maxValue = maxValue;
        this.maxCount = maxCount;
        this.slot = slot;
        weightEffect = (value, weight)->Math.min(maxValue, value+initialValue);
    }

    @Override
    public String toString() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
