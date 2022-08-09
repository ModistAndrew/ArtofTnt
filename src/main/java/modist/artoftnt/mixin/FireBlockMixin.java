package modist.artoftnt.mixin;

import modist.artoftnt.common.block.TntFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(FireBlock.class)
public abstract class FireBlockMixin {
    @Inject(
            method = {"tryCatchFire"},
            at = {@At(
                    value = "INVOKE",
                    ordinal = 1,
                    target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"
            )}
    )
    public void injectTryCatchFire(Level pLevel, BlockPos pPos, int pChance, Random pRandom, int pAge, Direction face, CallbackInfo ci) {
        BlockState state = pLevel.getBlockState(pPos);
        if(state.getBlock() instanceof TntFrameBlock block){
            block.onCaughtFire(state, pLevel, pPos, face, null);
        }
    }
}