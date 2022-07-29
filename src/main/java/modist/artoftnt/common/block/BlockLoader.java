package modist.artoftnt.common.block;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.entity.RemoteExploderBlockEntity;
import modist.artoftnt.common.block.entity.TntClonerBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockLoader {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ArtofTnt.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, ArtofTnt.MODID);

    @SuppressWarnings("unchecked")
    public static final RegistryObject<Block>[] TNT_FRAMES = new RegistryObject[4];
    @SuppressWarnings("unchecked")
    public static final RegistryObject<BlockEntityType<TntFrameBlockEntity>>[] TNT_FRAME_BLOCK_ENTITIES = new RegistryObject[4];
    public static final RegistryObject<Block> REMOTE_EXPLODER = BLOCKS.register("remote_exploder", RemoteExploderBlock::new);
    public static final RegistryObject<BlockEntityType<RemoteExploderBlockEntity>> REMOTE_EXPLODER_BLOCK_ENTITY =
            fromBlock(REMOTE_EXPLODER, RemoteExploderBlockEntity::new);
    public static final RegistryObject<Block> TNT_TURRET = BLOCKS.register("tnt_turret", TntTurretBlock::new);
    public static final RegistryObject<BlockEntityType<TntTurretBlockEntity>> TNT_TURRET_BLOCK_ENTITY =
            fromBlock(TNT_TURRET, TntTurretBlockEntity::new);
    public static final RegistryObject<Block> TNT_CLONER = BLOCKS.register("tnt_cloner", TntClonerBlock::new);
    public static final RegistryObject<BlockEntityType<TntClonerBlockEntity>> TNT_CLONER_BLOCK_ENTITY =
            fromBlock(TNT_CLONER, TntClonerBlockEntity::new);
    public static final RegistryObject<Block> DIMINISHING_LIGHT = BLOCKS.register("diminishing_light",
            DiminishingLightBlock::new);


    static {
        for(int i=0; i<4; i++){
            int finalI = i;
            TNT_FRAMES[i] = BLOCKS.register("tnt_frame_"+i, () -> new TntFrameBlock(finalI));
            TNT_FRAME_BLOCK_ENTITIES[i] = fromBlock(TNT_FRAMES[i], (p, s)->new TntFrameBlockEntity(p, s, finalI));
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> fromBlock(RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<T> pFactory) {
        return BLOCK_ENTITIES.register(block.getId().getPath()+"_block_entity", ()-> BlockEntityType.Builder.of
                (pFactory, block.get()).build(null));
    }

}
