package modist.artoftnt.core.addition;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.BiFunction;

public class AdditionType {
    private static final HashMap<String, AdditionType> TYPES = new HashMap<>();
    public static final AdditionType EMPTY = new Builder("empty", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType LOUDNESS = new Builder("loudness", AdditionSlot.EXPLOSION_SOUND).build();
    public static final AdditionType SOUND_TYPE = new Builder("sound_type", AdditionSlot.EXPLOSION_SOUND).build();
    public static final AdditionType VELOCITY = new Builder("velocity", AdditionSlot.TNT_MOTION).important().withInitialValue(0.2F).build();
    public static final AdditionType ELASTICITY = new Builder("elasticity", AdditionSlot.TNT_MOTION).build();
    public static final AdditionType STICKINESS = new Builder("stickiness", AdditionSlot.TNT_MOTION).build();
    public static final AdditionType LIGHTNESS = new Builder("lightness", AdditionSlot.TNT_MOTION).build();
    public static final AdditionType PUNCH = new Builder("punch", AdditionSlot.ENTITY_IMPACT).important().build();
    public static final AdditionType DRAW = new Builder("draw", AdditionSlot.ENTITY_IMPACT).build();
    public static final AdditionType TNT_PUNCH =new Builder("tnt_punch", AdditionSlot.ENTITY_IMPACT).build();
    public static final AdditionType TNT_DRAW = new Builder("tnt_draw", AdditionSlot.ENTITY_IMPACT).build();
    public static final AdditionType INSTABILITY = new Builder("instability", AdditionSlot.TNT_STABILITY).important().build();
    public static final AdditionType RANGE = new Builder("range", AdditionSlot.EXPLOSION_RANGE).important().build();
    public static final AdditionType DOWN = new Builder("down", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType UP = new Builder("up", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType NORTH = new Builder("north", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType SOUTH = new Builder("south", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType WEST = new Builder("west", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType EAST = new Builder("east", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType PIERCING = new Builder("piercing", AdditionSlot.ENTITY_RANGE).withMaxValue(8F).build();

    public static final AdditionType STRENGTH = new Builder("strength", AdditionSlot.EXPLOSION_STRENGTH).important().build();
    public static final AdditionType DAMAGE = new Builder("damage", AdditionSlot.ENTITY_EFFECT).important().build();
    public static final AdditionType POTION = new Builder("potion", AdditionSlot.ENTITY_EFFECT).build();
    public static final AdditionType DROP = new Builder("drop", AdditionSlot.BLOCK_DROP).build();
    public static final AdditionType CONTAINER = new Builder("container", AdditionSlot.BLOCK_DROP).single().build();
    public static final AdditionType FLAME = new Builder("flame", AdditionSlot.EXPLOSION_FLAME).build();
    public static final AdditionType TEMPERATURE = new Builder("temperature", AdditionSlot.EXPLOSION_FLAME).withMaxValue(8F).build();
    public static final AdditionType LIGHTNING = new Builder("lightning", AdditionSlot.EXPLOSION_FLAME).build();
    public static final AdditionType EXPLOSION_COUNT = new Builder("explosion_count", AdditionSlot.EXPLOSION_DURATION).withInitialValue(1).build();
    public static final AdditionType EXPLOSION_INTERVAL = new Builder("explosion_interval", AdditionSlot.EXPLOSION_DURATION).withInitialValue(1).build();
    public static final AdditionType FIREWORK = new Builder("firework", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType PARTICLE = new Builder("particle", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType TNT_PARTICLE = new Builder("tnt_particle", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType FUSE = new Builder("fuse", AdditionSlot.TNT_FUSE).important().build();
    public static final AdditionType SHAPE = new Builder("explosion_shape", AdditionSlot.EXPLOSION_SHAPE).single().build();

    public final String id;
    public final int maxCount;
    public final AdditionSlot slot;
    public final BiFunction<Float, Float, Float> weightEffect; //how value is affected by weight, also apply to original and max
    public final float maxValue;

    private AdditionType(String id, int maxCount, BiFunction<Float, Float, Float> weightEffect, AdditionSlot slot, float maxValue) {
        this.id = id;
        this.maxCount = maxCount;
        this.slot = slot;
        this.weightEffect = weightEffect;
        this.maxValue = maxValue;
    }

    @Override
    public String toString() {
        return this.id;
    }

    public static AdditionType fromString(String id){
        return TYPES.getOrDefault(id, EMPTY);
    }

    public static Collection<AdditionType> getTypes(){
        return TYPES.values();
    }

    public static class Builder{
        private final String id;
        private final AdditionSlot slot;
        private int maxCount;
        private float initialValue;
        private float maxValue;

        public Builder(String id, AdditionSlot slot) {
            this.id = id;
            this.slot = slot;
            this.maxCount = 8;
            this.maxValue = Float.MAX_VALUE;
        }

        public Builder withInitialValue(float initialValue){
            this.initialValue = initialValue;
            return this;
        }

        public Builder important(){
            return this.withMaxCount(16);
        }

        public Builder single(){
            return this.withMaxCount(1);
        }

        public Builder withMaxValue(float maxValue){
            this.maxValue = maxValue;
            return this;
        }

        public Builder withMaxCount(int maxCount){
            this.maxCount = maxCount;
            return this;
        }

        public AdditionType build(){
            AdditionType ret = new AdditionType(id, maxCount, (value, weight)->Math.min(maxValue, value+initialValue), slot, maxValue);
            TYPES.put(id, ret);
            return ret;
        }
    }
}
