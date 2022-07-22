package modist.artoftnt.network;

import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class ExplodePacket extends ClientboundExplodePacket {
    private final AdditionStack stack;
    private final int tier;
    private final Vec3 vec;

    public ExplodePacket(Vec3 vec, int tier, double pX, double pY, double pZ, float pPower, List<BlockPos> pToBlow, @Nullable Vec3 pKnockback, AdditionStack stack) {
        super(pX, pY, pZ, pPower, pToBlow, pKnockback);
        this.stack = stack;
        this.tier = tier;
        this.vec = vec;
    }

    public ExplodePacket(FriendlyByteBuf pBuffer) {
        super(pBuffer);
        this.tier = pBuffer.readInt();
        this.stack = new AdditionStack(tier, pBuffer.readNbt());
        this.vec = new Vec3(pBuffer.readDouble(), pBuffer.readDouble(), pBuffer.readDouble());
    }

    public void write(FriendlyByteBuf pBuffer) {
        super.write(pBuffer);
        pBuffer.writeInt(tier);
        pBuffer.writeNbt(stack.serializeNBT());
        pBuffer.writeDouble(vec.x);
        pBuffer.writeDouble(vec.y);
        pBuffer.writeDouble(vec.z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> doExplode()
                ));
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void doExplode(){
        Minecraft minecraft = Minecraft.getInstance();
        CustomExplosion exp = new CustomExplosion(vec, stack, minecraft.level, null, this.getX(), this.getY(), this.getZ(), this.getPower(), this.getToBlow());
        exp.finalizeExplosion(true);
        minecraft.player.setDeltaMovement(minecraft.player.getDeltaMovement().add(this.getKnockbackX(), this.getKnockbackY(), this.getKnockbackZ()));
    }
}
