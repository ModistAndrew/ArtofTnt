package modist.artoftnt.core.addition;

public class AdditionHelper {
    public static float FIRE_MIN_INSTABILITY = 0F;
    public static float EXPLOSION_MIN_INSTABILITY = 1F;

    public static int getInstabilityTextureId(float instability) {
        return instability > 7F ? 7 : (int)instability;
    }

    public static int getWeightTextureId(float weight) {
        return weight > 7F ? 7 : (int)weight;
    }

    public static float signalToMinInstability(int signal) {
        return 16-signal;
    }
}
