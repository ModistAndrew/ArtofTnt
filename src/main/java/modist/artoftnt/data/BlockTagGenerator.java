package modist.artoftnt.data;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.ModBlockTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockTagGenerator extends BlockTagsProvider {
    public BlockTagGenerator(DataGenerator pGenerator, ExistingFileHelper helper) {
        super(pGenerator, ArtofTnt.MODID, helper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags() {
        ForgeRegistries.BLOCKS.getValues().stream().filter(b ->
                b.defaultBlockState().getMaterial().isSolidBlocking() &&
                !ItemBlockRenderTypes.canRenderInLayer(b.defaultBlockState(), RenderType.translucent())).forEach(b ->
        this.tag(ModBlockTags.DISGUISE).add(b));
        this.tag(ModBlockTags.TO_BEDROCK).add(Blocks.BEDROCK);
        this.tag(ModBlockTags.TO_DIAMOND).add(Blocks.COAL_ORE);
        this.tag(ModBlockTags.TO_GLASS).addTags(BlockTags.SAND);
        this.tag(ModBlockTags.TO_LAVA).addTags(BlockTags.BASE_STONE_OVERWORLD);
        this.tag(ModBlockTags.TO_NETHERRACK).addTags(BlockTags.BASE_STONE_OVERWORLD);
        this.tag(ModBlockTags.TURRET_NORMAL).addTags(BlockTags.BASE_STONE_OVERWORLD);
        this.tag(ModBlockTags.TURRET_ROTATE).addTags(BlockTags.FIRE);
        this.tag(ModBlockTags.TURRET_MAX_ROTATE).addTags(BlockTags.CAMPFIRES);
        this.tag(ModBlockTags.TURRET_STRENGTH).add(Blocks.MAGMA_BLOCK);
    }
}
