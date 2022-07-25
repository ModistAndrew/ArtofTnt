package modist.artoftnt.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CoolDownBlockEntity extends BlockEntity {
    //only when <=0(and tnt==null) can new items be stored
    protected int coolDown = 0;
    //when <= finishTime, dispense() is called
    protected final int finishTime;
    //at least render_tick time should be set for cool down or render can not be completed
    protected static final int RENDER_TICK = 20;

    public CoolDownBlockEntity(int finishTime, BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.finishTime = finishTime;
    }

    public void tick() { //on client and server separately
        if (coolDown > 0) {
            coolDown--;
            if (!level.isClientSide) {
                setChanged();
                if (coolDown == finishTime) {
                    doDispense();
                }
            }
        }
    }

    //called when activated on server, suck in item and set coolDown
    public void activated(int pLevel) {
        if (!level.isClientSide && coolDown <= 0) {
            if (tryActivate(pLevel)) {
                setCoolDown();
                setChangedAndUpdate();
            }
        }
    }
    protected abstract void doDispense();
    public abstract boolean tryActivate(int level);
    protected abstract void setCoolDown();

    public abstract NonNullList<ItemStack> getDrops();
    protected void setChangedAndUpdate() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("coolDown", coolDown);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        coolDown = pTag.getInt("coolDown");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
        requestModelDataUpdate();
        this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL); //client update model
    }
}
