package modist.artoftnt.common.block.entity;

import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TntFrameBlockEntity extends BlockEntity {
    public final int tier;
    private final TntFrameData data;
    public static final ModelProperty<TntFrameData> ADDITIONS_MODEL_PROPERTY = new ModelProperty<>();

    public TntFrameBlockEntity(BlockPos pWorldPosition, BlockState pBlockState, int tier) {
        super(BlockLoader.TNT_FRAME_BLOCK_ENTITIES[tier].get(), pWorldPosition, pBlockState);
        this.tier = tier;
        this.data = new TntFrameData(tier);
    }

    public boolean add(ItemStack itemStack) {
        if (!data.additions.add(itemStack)) {
            return false;
        }
        update();
        return true;
    }

    public void toggleDisguise(BlockState blockState) {
        if(data.disguise==null || !data.disguise.equals(blockState)) { //set or change
            data.disguise = blockState;
        } else { //reset
            data.disguise = null;
        }
        update();
    }

    public void cycleSize() {
        if(data.size<=0.25F){
            data.size=1F;
        } else {
            data.size -= 0.25F;
        }
        update();
    }

    private void update() {
        if(this.getLevel().isClientSide) {
            requestModelDataUpdate();
            this.getLevel().sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        } else {
            this.setChanged();
            this.level.neighborChanged(this.getBlockPos(),
                    this.getBlockState().getBlock(), this.getBlockPos()); //update for signal
        }
    }

    public BlockState getDisguise() {
        return data.disguise;
    }

    public boolean fixed() {
        return data.fixed;
    }

    public int getColorForDisguise(@Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex) {
        return data.getColorForDisguise(pLevel, pPos, pTintIndex);
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(ADDITIONS_MODEL_PROPERTY, data)
                .build();
    }

    public CompoundTag getDataTag() {
        return data.serializeNBT();
    }

    public TntFrameData getData() {
        return data;
    }

    public void readDataTag(CompoundTag compoundtag) {
        data.deserializeNBT(compoundtag);
    }

    public float getDeflation() {
        return data.getDeflation();
    }

    public float getInstability() {
        return data.additions.getValue(AdditionType.INSTABILITY);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.put("tntFrameData", data.serializeNBT());
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        data.deserializeNBT(pTag.getCompound("tntFrameData"));
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
        update();
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> ret = new ArrayList<>();
        for(int i=0; i<18; i++){
            ret.addAll(data.additions.getItemStacks(i));
        }
        ret.add(new ItemStack(ItemLoader.TNT_FRAMES[tier].get())); //don't forget tnt frame
        return ret;
    }
}
