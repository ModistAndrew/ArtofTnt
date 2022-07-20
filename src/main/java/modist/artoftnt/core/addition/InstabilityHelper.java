package modist.artoftnt.core.addition;

public class InstabilityHelper {
    public static float FIRE_MIN_INSTABILITY = 0F;
    public static float EXPLODED_MIN_INSTABILITY = 3F;
    public static float TNT_HIT_ENTITY_MIN_INSTABILITY = 4F;
    public static float TNT_HIT_BLOCK_MIN_INSTABILITY = 6F;
    public static float ENTITY_ON_BLOCK_MIN_INSTABILITY = 8F;
    public static final float PROJECTILE_HIT_BLOCK_MIN_INSTABILITY = 7F;
    public static float BREAK_BLOCK_INSTABILITY = 10F;

    //when signal == 0, may also self-explode
    public static float signalToMinInstability(int signal) {
        return 16-signal;
    }
}
