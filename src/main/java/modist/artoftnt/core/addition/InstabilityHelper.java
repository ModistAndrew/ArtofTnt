package modist.artoftnt.core.addition;

import modist.artoftnt.ArtofTntConfig;

public class InstabilityHelper {
    public static final float FIRE_MIN_INSTABILITY = ArtofTntConfig.FIRE_MIN_INSTABILITY.get();
    public static final float EXPLODED_MIN_INSTABILITY = ArtofTntConfig.EXPLODED_MIN_INSTABILITY.get();
    public static final float TNT_HIT_ENTITY_MIN_INSTABILITY = ArtofTntConfig.TNT_HIT_ENTITY_MIN_INSTABILITY.get();
    public static final float TNT_HIT_BLOCK_MIN_INSTABILITY = ArtofTntConfig.TNT_HIT_BLOCK_MIN_INSTABILITY.get();
    public static final float ENTITY_HIT_BLOCK_MIN_INSTABILITY = ArtofTntConfig.ENTITY_HIT_BLOCK_MIN_INSTABILITY.get();
    public static final float PROJECTILE_HIT_BLOCK_MIN_INSTABILITY = ArtofTntConfig.PROJECTILE_HIT_BLOCK_MIN_INSTABILITY.get();
    public static final float BREAK_BLOCK_INSTABILITY = ArtofTntConfig.BREAK_BLOCK_INSTABILITY.get();
    public static final float RANDOM_BLOCK_MIN_INSTABILITY = ArtofTntConfig.RANDOM_BLOCK_MIN_INSTABILITY.get();
    private static final float[] REDSTONE_MIN_INSTABILITIES = new float[16];
    static{
        for(int i=0; i<16; i++){
            REDSTONE_MIN_INSTABILITIES[i] = ArtofTntConfig.REDSTONE_BLOCK_INSTABILITIES[i].get();
        }
    }

    //when signal == 0, may also self-explode
    public static float signalToMinInstability(int signal) {
        return REDSTONE_MIN_INSTABILITIES[signal];
    }
}
