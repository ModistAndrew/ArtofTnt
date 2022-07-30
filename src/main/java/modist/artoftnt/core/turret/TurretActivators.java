package modist.artoftnt.core.turret;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import modist.artoftnt.common.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

public class TurretActivators {
    private static final HashMap<TagKey<Block>, Function<BlockPos, Vec3>> ACTIVATORS =
            new HashMap<>();
    private static final HashMap<TagKey<Block>, ActivatorData> ACTIVATORS_DATA =
            new HashMap<>();

    public static Vec3 getDirection(BlockState state, BlockPos pos){
        for(TagKey<Block> tag : ACTIVATORS.keySet()){
            if(state.is(tag)){
                return ACTIVATORS.get(tag).apply(pos);
            }
        }
        return Vec3.ZERO;
    }

    @Nullable
    public static ActivatorData getData(BlockState state){
        for(TagKey<Block> tag : ACTIVATORS_DATA.keySet()){
            if(state.is(tag)){
                return ACTIVATORS_DATA.get(tag);
            }
        }
        return null;
    }

    public static void register(TagKey<Block> tag, float angle, float strength, float speed){
        ACTIVATORS.put(tag, p -> new Vec3(p.getX(), p.getY(), p.getZ()).normalize());
        ACTIVATORS_DATA.put(tag, new ActivatorData(angle, strength, speed));
    }

    static{
        register(ModBlockTags.TURRET_NORMAL, 0F, 1, 0);
        register(ModBlockTags.TURRET_ROTATE, 10F, 1, 5);
        register(ModBlockTags.TURRET_MAX_ROTATE, 90F, 1, 10);
        register(ModBlockTags.TURRET_STRENGTH, 0F, 2, 0);
    }

    public record ActivatorData(float angle, float strength, float speed){
        public ActivatorData add(ActivatorData another){
            if(another==null){
                return this;
            }
            return new ActivatorData(Math.max(this.angle, another.angle),
                    Math.max(this.strength, another.strength), Math.max(this.speed, another.speed));
        }
    }
}
