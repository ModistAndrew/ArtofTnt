package modist.artoftnt.common.block;

import modist.artoftnt.ArtofTnt;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModBlockTags {
    public static final TagKey<Block> DISGUISE = create("tnt_frame_disguise");
    public static final TagKey<Block> TO_GLASS = create("to_glass");
    public static final TagKey<Block> TO_DIAMOND = create("to_diamond");
    public static final TagKey<Block> TO_BEDROCK = create("to_bedrock");
    public static final TagKey<Block> TO_LAVA = create("to_lava");
    public static final TagKey<Block> TO_NETHERRACK = create("netherrack");
    public static final TagKey<Block> TURRET_NORMAL = create("turret_normal");


    public static TagKey<Block> create(String name) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(ArtofTnt.MODID, name));
    }
}
