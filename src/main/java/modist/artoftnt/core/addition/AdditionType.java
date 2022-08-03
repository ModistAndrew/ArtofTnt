package modist.artoftnt.core.addition;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.core.addition.AdditionSlot;

import java.util.*;
import java.util.function.BiFunction;

public class AdditionType {
    private static final HashMap<String, AdditionType> TYPES = new HashMap<>();
    public static final AdditionType RANGE = new Builder("range", AdditionSlot.EXPLOSION_RANGE).important().build();
    //affect both entity and block, except special effects (mostly client)
    public static final AdditionType DOWN = new Builder("down", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType UP = new Builder("up", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType NORTH = new Builder("north", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType SOUTH = new Builder("south", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType WEST = new Builder("west", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType EAST = new Builder("east", AdditionSlot.EXPLOSION_RANGE).build();
    public static final AdditionType FUSE = new Builder("fuse", AdditionSlot.TNT_FUSE).build();
    public static final AdditionType EMPTY = new Builder("empty", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType LOUDNESS = new Builder("loudness", AdditionSlot.EXPLOSION_SOUND).build();
    public static final AdditionType SOUND_TYPE = new Builder("sound_type", AdditionSlot.EXPLOSION_SOUND).require(AdditionType.LOUDNESS).build();
    public static final AdditionType ELASTICITY = new Builder("elasticity", AdditionSlot.TNT_PROPERTIES).fuse().build();
    public static final AdditionType SLIPPERINESS = new Builder("slipperiness", AdditionSlot.TNT_PROPERTIES).fuse().withMaxValue(0.3F).build();
    public static final AdditionType LIGHTNESS = new Builder("lightness", AdditionSlot.TNT_PROPERTIES).fuse().build();
    public static final AdditionType PUNCH = new Builder("punch", AdditionSlot.ENTITY_IMPACT).range().build();
    public static final AdditionType PUNCH_HORIZONTAL = new Builder("punch_horizontal", AdditionSlot.ENTITY_IMPACT).range().build();
    public static final AdditionType PUNCH_VERTICAL = new Builder("punch_vertical", AdditionSlot.ENTITY_IMPACT).range().build();

    public static final AdditionType DRAW = new Builder("draw", AdditionSlot.ENTITY_IMPACT).range().build();
    public static final AdditionType TNT_PUNCH =new Builder("tnt_punch", AdditionSlot.ENTITY_IMPACT).range().fuse().build();
    public static final AdditionType TNT_DRAW = new Builder("tnt_draw", AdditionSlot.ENTITY_IMPACT).range().fuse().build();
    public static final AdditionType INSTABILITY = new Builder("instability", AdditionSlot.TNT_STABILITY).important().build();
    public static final AdditionType RANDOM = new Builder("random", AdditionSlot.TNT_STABILITY).build();
    public static final AdditionType QUICK_CHARGE = new Builder("quick_charge", AdditionSlot.TNT_STABILITY).build();
    public static final AdditionType TARGET = new Builder("target", AdditionSlot.TNT_MOTION).fuse().build();
    public static final AdditionType MOB_TARGET = new Builder("mob_target", AdditionSlot.TNT_MOTION).fuse().build();
    public static final AdditionType LOYALTY = new Builder("loyalty", AdditionSlot.TNT_PROPERTIES).single().build();
    public static final AdditionType VELOCITY = new Builder("velocity", AdditionSlot.TNT_MOTION).important().fuse().withInitialValue(0.2F).build();
    public static final AdditionType NO_PHYSICS = new Builder("no_physics", AdditionSlot.TNT_MOTION).single().fuse().build();
    public static final AdditionType PIERCING = new Builder("piercing", AdditionSlot.EXPLOSION_STRENGTH).withMaxValue(8F).range().build();
    public static final AdditionType STRENGTH = new Builder("strength", AdditionSlot.EXPLOSION_STRENGTH).important().range().build();
    public static final AdditionType DAMAGE = new Builder("damage", AdditionSlot.ENTITY_EFFECT).range().important().build();
    public static final AdditionType POTION = new Builder("potion", AdditionSlot.ENTITY_EFFECT).range().build();
    public static final AdditionType TELEPORT = new Builder("teleport", AdditionSlot.ENTITY_EFFECT).range().build();
    public static final AdditionType DROP = new Builder("drop", AdditionSlot.BLOCK_DROP).block().withMaxValue(1F).build();
    public static final AdditionType CONTAINER = new Builder("container", AdditionSlot.BLOCK_DROP).single().block().require(AdditionType.DROP).build();
    public static final AdditionType BLOW_UP = new Builder("blow_up", AdditionSlot.BLOCK_DROP).build();
    public static final AdditionType FLAME = new Builder("flame", AdditionSlot.EXPLOSION_FLAME).range().build();
    public static final AdditionType TEMPERATURE = new Builder("temperature", AdditionSlot.EXPLOSION_FLAME).block().withMaxValue(8F).build();
    public static final AdditionType LIGHTNING = new Builder("lightning", AdditionSlot.EXPLOSION_FLAME).build();
    public static final AdditionType EXPLOSION_COUNT = new Builder("explosion_count", AdditionSlot.EXPLOSION_DURATION).withInitialValue(1).build();
    public static final AdditionType EXPLOSION_INTERVAL = new Builder("explosion_interval", AdditionSlot.EXPLOSION_DURATION).withInitialValue(5).build();
    public static final AdditionType FIREWORK = new Builder("firework", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType TNT_PARTICLE = new Builder("tnt_particle", AdditionSlot.EXPLOSION_EFFECT).withMaxCount(4).fuse().build();
    public static final AdditionType LIGHT = new Builder("light", AdditionSlot.EXPLOSION_EFFECT).build();
    public static final AdditionType SHAPE = new Builder("shape", AdditionSlot.EXPLOSION_SHAPE).single().build();

    public final String id;
    public final int maxCount;
    public final AdditionSlot slot;
    public final BiFunction<Float, Float, Float> weightEffect; //how value is affected by weight, also apply to original and max
    public final float maxValue;
    public final float initialValue;
    public final Set<AdditionType> requirements = new HashSet<>();

    private AdditionType(String id, int maxCount, BiFunction<Float, Float, Float> weightEffect, AdditionSlot slot,
                         float maxValue, float initialValue, Set<AdditionType> requirements) {
        this.id = id;
        this.maxCount = maxCount;
        this.slot = slot;
        this.weightEffect = weightEffect;
        this.maxValue = maxValue;
        this.initialValue = initialValue;
        this.requirements.addAll(requirements);
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
        public final Set<AdditionType> requirements = new HashSet<>();

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

        public Builder require(AdditionType type){
            this.requirements.add(type);
            return this;
        }
        public Builder block(){
            return this.require(AdditionType.RANGE).require(AdditionType.STRENGTH);
        }

        public Builder range(){
            return this.require(AdditionType.RANGE);
        }

        public Builder fuse(){
            return this.require(AdditionType.FUSE);
        }

        public AdditionType build(){
            AdditionType ret = new AdditionType(id, maxCount, (value, weight)->Math.min(maxValue, value+initialValue),
                    slot, maxValue, initialValue, requirements);
            if(!TYPES.containsKey(id)) {
                TYPES.put(id, ret);
            } else {
                ArtofTnt.LOGGER.error("duplicate tnt frame addition type id {}", id);
            }
            return ret;
        }
    }
}