package modist.artoftnt.common.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class CoolDownBlockEntity extends BlockEntity {
    //only when <=0(and tnt==null) can new items be stored
    protected int coolDown = 0;
    //at least render_tick time should be set for cool down or render can not be completed
    protected final int minCoolDown;
    //stored for rendering
    protected int maxCoolDown = 0;
    public CoolDownBlockEntity(int minCoolDown, BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.minCoolDown = minCoolDown;
    }

    public void tick() { //on client and server separately
        if(level!=null) {
            if (coolDown > 0) {
                coolDown--;
                if (!level.isClientSide) {
                    setChanged();
                    if (coolDown == 0) {
                        doDispense();
                    }
                }
            } else {
                if (!level.isClientSide) {
                    int pLevel = level.getBestNeighborSignal(this.getBlockPos());
                    if (pLevel > 0) {
                        activated(pLevel);
                    }
                }
            }
        }
    }

    //called when activated on server, suck in item and set coolDown
    public void activated(int pLevel) {
        if (level!=null && !level.isClientSide && coolDown <= 0) {
            if (tryActivate(pLevel)) {
                setCoolDown();
                this.maxCoolDown = coolDown;
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
        if(level!=null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putInt("coolDown", coolDown);
        pTag.putInt("maxCoolDown", maxCoolDown);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        coolDown = pTag.getInt("coolDown");
        maxCoolDown = pTag.getInt("maxCoolDown");
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
    }
}
