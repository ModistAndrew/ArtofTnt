package modist.artoftnt.core.explosion;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.network.ExplodePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class ExplosionHelper {

    public static void explode(AdditionStack stack, Level pLevel, @Nullable PrimedTntFrame pEntity, double pX, double pY, double pZ, float pExplosionRadius, boolean pCausesFire, Explosion.BlockInteraction pMode) {
        explode(stack, pLevel, pEntity, null, null, pX, pY, pZ, pExplosionRadius, pCausesFire, pMode);
    }

    public static void explode(AdditionStack stack, Level pLevel, @Nullable PrimedTntFrame pExploder, @Nullable DamageSource pDamageSource, @Nullable ExplosionDamageCalculator pContext,
                               double pX, double pY, double pZ, float pSize, boolean pCausesFire, Explosion.BlockInteraction pMode) {
        CustomExplosion exp = new CustomExplosion(stack, pLevel, pExploder, pDamageSource, pContext, pX, pY, pZ, pSize, pCausesFire, pMode);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(pLevel, exp)) return;
        exp.explode();
        exp.finalizeExplosion(true);
        if(!pLevel.isClientSide){
            if (pMode == Explosion.BlockInteraction.NONE) {
                exp.clearToBlow();
            }
            for(ServerPlayer serverplayer : ((ServerLevel)pLevel).players()) {
                if (serverplayer.distanceToSqr(pX, pY, pZ) < 4096.0D) {
                    serverplayer.connection.send(new ExplodePacket(stack.tier, pX, pY, pZ, pSize, exp.getToBlow(), exp.getHitPlayers().get(serverplayer), stack));
                    //send packet to other players on client side for blow, sound, etc
                }
            }
        }
    }

}
