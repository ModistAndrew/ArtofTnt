package modist.artoftnt.common.block;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.entity.RemoteExploderBlockEntity;
import modist.artoftnt.common.block.entity.TntClonerBlockEntity;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.function.Supplier;

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
    public static final HashMap<String, RegistryObject<Block>> SIMPLE_BLOCKS = new HashMap<>();
    public static final RegistryObject<Block> BLAZE_BLOCK = simple("blaze_block",
            () -> new MagmaBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.NETHER).requiresCorrectToolForDrops().lightLevel((p_152684_) -> {
                return 7;
            }).randomTicks().strength(1F).isValidSpawn((p_187421_, p_187422_, p_187423_, p_187424_) -> {
                return p_187424_.fireImmune();
            }).hasPostProcess((a, b, c)->true).emissiveRendering((a, b, c)->true)){
                public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
                    if (!pEntity.fireImmune() && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity)) {
                        pEntity.hurt(DamageSource.HOT_FLOOR, 2.0F);
                    }

                    super.stepOn(pLevel, pPos, pState, pEntity);
                }
            });
    public static final RegistryObject<Block> BLAZE_BLOCK_2 = simple("blaze_block_2",
            () -> new MagmaBlock(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.NETHER).requiresCorrectToolForDrops().lightLevel((p_152684_) -> 15)
                    .randomTicks().strength(2F).isValidSpawn((p_187421_, p_187422_, p_187423_, p_187424_) -> p_187424_.fireImmune())
                    .hasPostProcess((a, b, c)->true).emissiveRendering((a, b, c)->true)){
                public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
                    if (!pEntity.fireImmune() && pEntity instanceof LivingEntity && !EnchantmentHelper.hasFrostWalker((LivingEntity)pEntity)) {
                        pEntity.hurt(DamageSource.HOT_FLOOR, 4.0F);
                    }

                    super.stepOn(pLevel, pPos, pState, pEntity);
                }
            });
    public static final RegistryObject<Block> REINFORCED_GLASS = simple("reinforced_glass",
            ()->new GlassBlock(BlockBehaviour.Properties.of(Material.GLASS).strength(0.3F, Float.MAX_VALUE/2F)
                    .sound(SoundType.GLASS).noOcclusion().isValidSpawn((a, b, c, d)->false)
                    .isRedstoneConductor((a, b, c)->false).isSuffocating((a, b, c)->false).isViewBlocking((a, b, c)->false)));


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

    private static RegistryObject<Block> simple(String name, Supplier<? extends Block> sup) {
        RegistryObject<Block> ret = BLOCKS.register(name, sup);
        SIMPLE_BLOCKS.put(name, ret);
        return ret;
    }


}
