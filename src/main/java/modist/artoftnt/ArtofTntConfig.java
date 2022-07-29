package modist.artoftnt;

import net.minecraftforge.common.ForgeConfigSpec;

public class ArtofTntConfig {
    public static ForgeConfigSpec COMMON_CONFIG;
    public static ForgeConfigSpec.BooleanValue ENABLE_ADDITION_REPLY;
    public static ForgeConfigSpec.IntValue MIN_GLOBAL_SOUND_LOUDNESS;
    public static ForgeConfigSpec.IntValue MIN_PERSISTENT_FUSE;
    public static ForgeConfigSpec.IntValue MIN_TURRET_TICK;
    public static ForgeConfigSpec.IntValue MIN_CLONER_TICK;
    public static ForgeConfigSpec.IntValue MAX_INFINITY_WEIGHT;
    public static ForgeConfigSpec.IntValue DISGUISE_COLOR_MASK;
    public static ForgeConfigSpec.IntValue FIRE_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue EXPLODED_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue TNT_HIT_ENTITY_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue TNT_HIT_BLOCK_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue ENTITY_HIT_BLOCK_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue PROJECTILE_HIT_BLOCK_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue BREAK_BLOCK_INSTABILITY;
    public static ForgeConfigSpec.IntValue RANDOM_BLOCK_MIN_INSTABILITY;
    public static ForgeConfigSpec.IntValue[] REDSTONE_BLOCK_INSTABILITIES = new ForgeConfigSpec.IntValue[16];

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Art of TNT settings");
        builder.push("general");
        ENABLE_ADDITION_REPLY = builder.comment("whether return reply when addition cannot be put").define("enable_addition_reply", false);
        MIN_GLOBAL_SOUND_LOUDNESS = builder.comment("the least loudness factor to let the sound heard by all players")
                .defineInRange("min_global_sound_loudness", 1000, 0, Integer.MAX_VALUE);
        MIN_PERSISTENT_FUSE = builder.comment("the least fuse factor to let the tnt won't explode")
                .defineInRange("min_persistent_fuse", 1000, 0, Integer.MAX_VALUE);
        MIN_TURRET_TICK = builder.comment("the least tick for turret to shoot")
                .defineInRange("min_turret_tick", 20, 2, Integer.MAX_VALUE);
        MIN_CLONER_TICK = builder.comment("the least tick for cloner to clone")
                .defineInRange("min_cloner_tick", 20, 2, Integer.MAX_VALUE);
        MAX_INFINITY_WEIGHT = builder.comment("the max weight for the tnt to not shrink when there's infinity enchantment")
                .defineInRange("max_infinity_weight", 10, 0, Integer.MAX_VALUE);
        DISGUISE_COLOR_MASK = builder.comment("the color of the disguise")
                .defineInRange("disguise_color_mask", 0xFFEFEF, 0, Integer.MAX_VALUE);
        builder.push("instabilities");
        FIRE_MIN_INSTABILITY = builder.comment("the min instability for the tnt to be primed when lit on")
                .defineInRange("fire_min_instability", 0, 0, Integer.MAX_VALUE);
        EXPLODED_MIN_INSTABILITY = builder.comment("the min instability for the tnt to be primed when exploded")
                .defineInRange("exploded_min_instability", 3, 0, Integer.MAX_VALUE);
        ENTITY_HIT_BLOCK_MIN_INSTABILITY = builder.comment("the min instability for the tnt to be primed when entity walk on or hit block")
                .defineInRange("entity_hit_block_min_instability", 8, 0, Integer.MAX_VALUE);
        PROJECTILE_HIT_BLOCK_MIN_INSTABILITY = builder.comment("the min instability for the tnt to be primed when a projectile hit it")
                .defineInRange("projectile_hit_block_min_instability", 6, 0, Integer.MAX_VALUE);
        BREAK_BLOCK_INSTABILITY = builder.comment("the min instability for the tnt to be primed when it is broken by player")
                .defineInRange("break_block_min_instability", 16, 0, Integer.MAX_VALUE);
        TNT_HIT_ENTITY_MIN_INSTABILITY = builder.comment("the min instability for the primed tnt to explode at once when hit entity")
                .defineInRange("tnt_hit_entity_min_instability", 5, 0, Integer.MAX_VALUE);
        TNT_HIT_BLOCK_MIN_INSTABILITY = builder.comment("the min instability for the primed tnt to explode at once when hit block")
                .defineInRange("tnt_hit_block_min_instability", 7, 0, Integer.MAX_VALUE);
        RANDOM_BLOCK_MIN_INSTABILITY = builder.comment("the min instability for the primed tnt to explode randomly")
                .defineInRange("random_block_min_instability", 15, 0, Integer.MAX_VALUE);
        for(int i=1; i<16; i++){
            REDSTONE_BLOCK_INSTABILITIES[i] = builder.comment("the min instability for the tnt to be primed when receive max redstone signal "+i)
                    .defineInRange("redstone_block_min_instability_"+i, 16-i, 0, Integer.MAX_VALUE);
        }
        REDSTONE_BLOCK_INSTABILITIES[0] = builder.comment("the min instability for the tnt to be primed at once the instability is reached")
                .defineInRange("min_instability_explode_at_once", 17, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.pop();
        COMMON_CONFIG = builder.build();
    }
}
