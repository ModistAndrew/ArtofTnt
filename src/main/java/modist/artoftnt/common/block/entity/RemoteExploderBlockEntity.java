package modist.artoftnt.common.block.entity;

import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.item.PositionMarkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RemoteExploderBlockEntity extends BlockEntity { //TODO: capability
    private final ItemStack[] markers = new ItemStack[16];
    private int top; //the first id that is empty
    public static final ModelProperty<ItemStack[]> MARKERS_MODEL_PROPERTY = new ModelProperty<>();

    public RemoteExploderBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockLoader.REMOTE_EXPLODER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> {
                return new IItemHandler() {
                    @Override
                    public int getSlots() {
                        return 16;
                    }
                    @Override
                    public ItemStack getStackInSlot(int slot) {
                        return slot < top ? markers[slot] : ItemStack.EMPTY;
                    }
                    @Override
                    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                        ItemStack stack1 = stack.copy();
                        if (slot == top && accept(stack1)) {
                            if(!simulate){
                                pushMarker(stack);
                            }
                            stack1.shrink(1);
                        }
                        return stack1;
                    }
                    @Override
                    public ItemStack extractItem(int slot, int amount, boolean simulate) {
                        if (slot == top-1 && amount > 0) {
                            ItemStack ret = markers[top-1];
                            if(!simulate){
                                popMarker();
                            }
                            return ret;
                        }
                        return ItemStack.EMPTY;
                    }
                    @Override
                    public int getSlotLimit(int slot) {
                        return 1;
                    }
                    @Override
                    public boolean isItemValid(int slot, ItemStack stack) {
                        return slot < top && accept(stack);
                    }
                };
            }).cast();
        } else {
            return super.getCapability(cap, side);
        }
    }

    public boolean accept(ItemStack stack) {
        return stack.getItem() instanceof PositionMarkerItem item && !item.isContainer && top < 16;
    }

    public void pushMarker(ItemStack stack) {
            markers[top++] = stack;
            this.setChangedAndUpdate();
    }

    public boolean isEmpty() {
        return top<=0;
    }

    public ItemStack popMarker() {
        ItemStack ret = markers[--top];
        this.setChangedAndUpdate();
        return ret;
    }

    public void explode(float minInstability) {
        if(!this.level.isClientSide) {
            float strength = 0F;
            for (int j = 0; j < top; j++) {
                ItemStack stack = markers[j];
                if(stack.getItem() instanceof PositionMarkerItem item && !item.isContainer) {
                    strength += item.tier+1;
                    BlockPos pos = new BlockPos(item.getPos(Vec3.atCenterOf(this.getBlockPos()), stack));
                    if (pos != null) {
                        BlockState state = this.level.getBlockState(pos);
                        if (state.getBlock() instanceof TntFrameBlock tfb && item.tier >= tfb.tier) { //tier
                            tfb.tryExplode(minInstability, this.level, pos, null);
                        }
                    }
                }
            }
            if(strength>0) {
                this.level.explode(null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(),
                        (float) Math.sqrt(strength), Explosion.BlockInteraction.NONE);
            }
            this.clear();
        }
    }

    private void clear() {
        top=0;
        this.setChangedAndUpdate();
    }

    private void setChangedAndUpdate() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        //this.level.neighborChanged(this.getBlockPos(),
          //      this.getBlockState().getBlock(), this.getBlockPos());
        // only when updated, not like tnt frame
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ListTag list = new ListTag();
        for (int j = 0; j < top; j++) {
            list.add(j, markers[j].serializeNBT());
        }
        pTag.put("markers", list);
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ListTag list = pTag.getList("markers", 10);
        for (int j = 0; j < list.size(); j++) {
            markers[j] = ItemStack.of(list.getCompound(j));
        }
        top = list.size();
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

    @Nonnull
    @Override
    public IModelData getModelData() {
        ItemStack[] stacks = new ItemStack[top];
        for(int i=0; i<top; i++){
            stacks[i] = markers[i];
        }
        return new ModelDataMap.Builder()
                .withInitial(MARKERS_MODEL_PROPERTY, stacks)
                .build();
    }

    public NonNullList<ItemStack> getDrops() {
        ItemStack[] stacks = new ItemStack[top];
        for(int i=0; i<top; i++){
            stacks[i] = markers[i];
        }
        return NonNullList.of(ItemStack.EMPTY, stacks);
    }
}
