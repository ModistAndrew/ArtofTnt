package modist.artoftnt.core.addition;

public enum AdditionSlot {
    EXPLOSION_SOUND(0), TNT_MOTION(1), ENTITY_IMPACT(2), TNT_STABILITY(3),
    EXPLOSION_RANGE(4), ENTITY_RANGE(7),
    EXPLOSION_STRENGTH(8), ENTITY_EFFECT(11),
    BLOCK_DROP(12), EXPLOSION_FLAME(13), EXPLOSION_DURATION(14), EXPLOSION_EFFECT(15),
    TNT_FUSE(16, 8), EXPLOSION_SHAPE(17, 1);

    public final int index;
    public final int u1;
    public final int v1;
    public final int maxCount;

    AdditionSlot(int index){
        this.index = index;
        this.u1 = getU(index);
        this.v1 = getV(index);
        this.maxCount = 16;
    }

    AdditionSlot(int index, int maxCount){
        this.index = index;
        this.u1 = getU(index);
        this.v1 = getV(index);
        this.maxCount = maxCount;
    }

    public static int getU(int index) {
        return 1+4*(index%4);
    }

    public static int getV(int index) {
        return 1+4*(index/4);
    }

}
