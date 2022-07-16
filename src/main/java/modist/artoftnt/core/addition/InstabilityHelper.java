package modist.artoftnt.core.addition;

public class InstabilityHelper {
    public static float FIRE_MIN_INSTABILITY = 0F;
    public static float EXPLODED_MIN_INSTABILITY = 1F;
    public static float TNT_HIT_ENTITY_MIN_INSTABILITY = 1F;
    public static float TNT_HIT_BLOCK_MIN_INSTABILITY = 1F;
    public static float ENTITY_HIT_BLOCK_MIN_INSTABILITY = 1F;
    public static float BREAK_BLOCK_INSTABILITY = 1F;
    public static float SELF_EXPLOSION_BLOCK_INSTABILITY = 1F;

    public static float signalToMinInstability(int signal) {
        return 16-signal;
    }
}
