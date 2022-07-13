package modist.artoftnt.network;

import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExplodePacket extends ClientboundExplodePacket { //TODO forge
    private final AdditionStack stack;
    private final int tier;

    public ExplodePacket(int tier, double pX, double pY, double pZ, float pPower, List<BlockPos> pToBlow, @Nullable Vec3 pKnockback, AdditionStack stack) {
        super(pX, pY, pZ, pPower, pToBlow, pKnockback);
        this.stack = stack;
        this.tier = tier;
    }

    public ExplodePacket(FriendlyByteBuf pBuffer) {
    super(pBuffer);
    this.tier = pBuffer.readInt();
    this.stack = new AdditionStack(tier, pBuffer.readNbt());
    }

    public void write(FriendlyByteBuf pBuffer) {
        super.write(pBuffer);
        pBuffer.writeInt(tier);
        pBuffer.writeNbt(stack.serializeNBT());
    }

    @Override
    public void handle(ClientGamePacketListener pHandler) {
        Minecraft minecraft = Minecraft.getInstance();
        PacketUtils.ensureRunningOnSameThread(this, pHandler, minecraft);
        CustomExplosion exp = new CustomExplosion(stack, minecraft.level, null, this.getX(), this.getY(), this.getZ(), this.getPower(), this.getToBlow());
        exp.finalizeExplosion(true);
        minecraft.player.setDeltaMovement(minecraft.player.getDeltaMovement().add(this.getKnockbackX(), this.getKnockbackY(), this.getKnockbackZ()));
    }
}
