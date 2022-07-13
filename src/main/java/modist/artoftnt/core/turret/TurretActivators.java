package modist.artoftnt.core.turret;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

//TODO:direction not correct (or see: TileEntity?)
public class TurretActivators {
    private static final HashMap<Block, Function<BlockPos, Vec3>> ACTIVATORS =
            new HashMap<>();
    private static final Random RANDOM = new Random();
    
    public static boolean accept(Block block){
        return ACTIVATORS.containsKey(block);
    }

    public static Vec3 getDirection(Block block, BlockPos pos){
        return accept(block) ? ACTIVATORS.get(block).apply(pos) : Vec3.ZERO;
    }

    public static void register(Block block, Function<BlockPos, Vec3> function){
        ACTIVATORS.put(block, function);
    }
    
    private static Function<BlockPos, Vec3> create(float strength, float inAccuracy){
        return p -> new Vec3(p.getX(), p.getY(), p.getZ()).normalize().add
                (RANDOM.nextGaussian() * (double)0.0075F * (double)inAccuracy,
                        RANDOM.nextGaussian() * (double)0.0075F * (double)inAccuracy,
                        RANDOM.nextGaussian() * (double)0.0075F * (double)inAccuracy).scale((double)strength);
    }

    static{
        register(Blocks.MAGMA_BLOCK, create(1, 1));
    }
}
