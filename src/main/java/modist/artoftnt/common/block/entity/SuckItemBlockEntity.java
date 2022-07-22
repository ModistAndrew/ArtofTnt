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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class SuckItemBlockEntity extends BlockEntity {
    //only when <=0(and tnt==null) can new items be stored
    protected int coolDown = 0;
    //suck in and stored when activated, will be cleared when dropped or cleared
    protected final List<ItemStack> stacks = new ArrayList<>();
    //when <= finishTime, dispense() is called
    protected final int finishTime;
    //at least render_tick time should be set for cool down or render can not be completed
    protected static final int RENDER_TICK = 20;

    public SuckItemBlockEntity(int finishTime, BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
        this.finishTime = finishTime;
    }

    public void tick() { //on client and server separately
        if (coolDown > 0) {
            coolDown--;
            if (!level.isClientSide) {
                setChanged();
                if (coolDown <= finishTime && !stacks.isEmpty()) {
                    dispense();
                }
            }
        }
    }

    //called when activated on server, suck in item and set coolDown
    public void activated() {
        if (!level.isClientSide && coolDown <= 0) {
            if (suckItem()) {
                setCoolDown();
                setChangedAndUpdate();
            }
        }
    }

    //called on server when coolDown <= finishTime
    protected void dispense() {
        if (!level.isClientSide) {
            doDispense();
            stacks.clear();
            setChangedAndUpdate();
        }
    }

    protected abstract void doDispense();

    public boolean suckItem() {
        AtomicBoolean ret = new AtomicBoolean(false);
        BlockEntity be = this.level.getBlockEntity(this.getBlockPos().above());
        if (be != null) {
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                ret.set(suckItemFrom(c));
            });
        }
        return ret.get();
    }

    protected abstract boolean suckItemFrom(IItemHandler handler);

    protected abstract void setCoolDown();

    private void setChangedAndUpdate() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    public NonNullList<ItemStack> getDrops() {
        return NonNullList.of(ItemStack.EMPTY, stacks.toArray(new ItemStack[0]));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ListTag list = new ListTag();
        for (int j = 0; j < stacks.size(); j++) {
            list.add(j, stacks.get(j).serializeNBT());
        }
        pTag.put("stacks", list);
        pTag.putInt("coolDown", coolDown);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ListTag list = pTag.getList("stacks", 10);
        for (int j = 0; j < list.size(); j++) {
            stacks.add(ItemStack.of(list.getCompound(j)));
        }
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
