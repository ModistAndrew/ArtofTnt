package modist.artoftnt.core.turret;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

//TODO:direction not correct (or see: TileEntity?)
public class TurretActivators {
    private static final HashMap<TagKey<Block>, Function<BlockPos, Vec3>> ACTIVATORS =
            new HashMap<>();
    private static final Object2FloatMap<TagKey<Block>> ACTIVATORS_ROTATION =
            new Object2FloatOpenHashMap<>();
    private static final Random RANDOM = new Random();

    public static Vec3 getDirection(BlockState state, BlockPos pos){
        for(TagKey<Block> tag : ACTIVATORS.keySet()){
            if(state.is(tag)){
                return ACTIVATORS.get(tag).apply(pos);
            }
        }
        return Vec3.ZERO;
    }

    public static float getRotation(BlockState state){
        for(TagKey<Block> tag : ACTIVATORS_ROTATION.keySet()){
            if(state.is(tag)){
                return ACTIVATORS_ROTATION.getFloat(tag);
            }
        }
        return 0F;
    }

    public static void register(TagKey<Block> tag, Function<BlockPos, Vec3> function){
        ACTIVATORS.put(tag, function);
    }

    public static void registerRotation(TagKey<Block> tag, float rotation){
        ACTIVATORS_ROTATION.put(tag, rotation);
    }
    
    private static Function<BlockPos, Vec3> create(float strength){
        return p -> new Vec3(p.getX(), p.getY(), p.getZ()).normalize();
    }

    static{
        register(BlockTags.FIRE, create(1));
        registerRotation(BlockTags.FIRE, 90F);
    }
}
