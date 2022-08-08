package modist.artoftnt.common.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.TntClonerBlock;
import modist.artoftnt.common.block.TntFrameBlock;
import modist.artoftnt.common.item.TntFrameItem;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TntClonerBlockEntity extends CoolDownBlockEntity {
    @NotNull
    public ItemStack tntFrame = ItemStack.EMPTY;
    protected final List<ItemStack> stacks = new ArrayList<>();
    private final int transferTick;

    public TntClonerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(20, BlockLoader.TNT_CLONER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
        transferTick = Math.min(8, this.minCoolDown/2);
    }

    //-1 to 1
    public float getOffset(float pPartialTick) {
        if(coolDown <= transferTick){
            return 1 - (coolDown+pPartialTick) / transferTick;
        }
        if(coolDown >= maxCoolDown - transferTick){
            return (maxCoolDown-coolDown-pPartialTick)/transferTick - 1;
        }
        return 0;
    }

    public boolean finished() {
        return coolDown <= transferTick;
    }

    public boolean justFinish() {
        return coolDown == transferTick;
    }
    public float finishRate() {
        if(coolDown==0){
            return 1/16F;
        }
        if(coolDown <= transferTick){
            return 1;
        }
        if(coolDown >= maxCoolDown - transferTick){
            return 1/16F;
        }
        return 1/16F + 15/16F * (maxCoolDown - transferTick - coolDown) / (maxCoolDown - 2*transferTick);
    }
    @Override
    protected void doDispense() {
        if (level!=null && !level.isClientSide && tntFrame.getItem() instanceof TntFrameItem) {
            BlockEntity be = this.level.getBlockEntity(this.getBlockPos().relative(this.getBlockState()
                    .getValue(TntClonerBlock.FACING)));
            AtomicBoolean flag = new AtomicBoolean(false);
            if(be!=null){
                be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                    for(int i=0; i < c.getSlots(); i++){
                        if(c.insertItem(i, tntFrame, true).isEmpty()){
                            c.insertItem(i, tntFrame, false);
                            flag.set(true);
                            break;
                        }
                    }
                });
            }
            if(!flag.get()) {
                Direction d = this.getBlockState().getValue(TntClonerBlock.FACING);
                double d0 = this.getBlockPos().getX() + 0.7D * (double)d.getStepX();
                double d1 = this.getBlockPos().getY() + 0.7D * (double)d.getStepY();
                double d2 = this.getBlockPos().getZ() + 0.7D * (double)d.getStepZ();
                Position pos = new PositionImpl(d0, d1, d2);
                DefaultDispenseItemBehavior.spawnItem(this.level, tntFrame, 6, d, pos);
            }
            this.tntFrame = ItemStack.EMPTY;
            stacks.clear();
            setChangedAndUpdate();
        }
    }

    @Override
    public boolean tryActivate(int level) {
        if(this.level!=null) {
            AtomicBoolean ret = new AtomicBoolean(false);
            BlockEntity be = this.level.getBlockEntity(this.getBlockPos().above());
            BlockEntity beFrame = this.level.getBlockEntity(this.getBlockPos().relative(this.getBlockState()
                    .getValue(TntClonerBlock.FACING).getOpposite()));
            if (be != null && beFrame != null) {
                be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                        .ifPresent(c -> beFrame.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                                .ifPresent(c1 -> ret.set(suckItemFrom(c, c1))));
            }
            return ret.get();
        }
        return false;
    }
    private boolean suckItemFrom(IItemHandler handler1, IItemHandler handler2) {
        if (this.level!=null && this.level.getBlockEntity(this.getBlockPos().below()) instanceof TntFrameBlockEntity blockEntity) {
            List<ItemStack> drops = blockEntity.getAdditions();
            Object2IntMap<ItemStackWrapper> materials = StackInSlotData.transform(drops);
            HashMap<ItemStackWrapper, StackInSlotData> items = StackInSlotData.transform(handler1);
            if (StackInSlotData.containAll(materials, items)) {
                HashMap<ItemStackWrapper, StackInSlotData> itemsToSuck = StackInSlotData.transform(materials, items);
                ItemStack frame = blockEntity.getFrame();
                for(int i=0; i<handler2.getSlots(); i++){
                    if(ItemStack.matches(frame, handler2.extractItem(i, 1, true))){
                        if (StackInSlotData.draw(itemsToSuck, handler1)) { //do
                            handler2.extractItem(i, 1, false);
                            this.stacks.add(frame);
                            this.stacks.addAll(drops);
                            this.tntFrame = TntFrameBlock.dropFrame(true, blockEntity.getData());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void setCoolDown() {
        if (this.level!=null && this.level.getBlockEntity(this.getBlockPos().below()) instanceof TntFrameBlockEntity blockEntity) {
            this.coolDown = blockEntity.getData().getCoolDown() + minCoolDown;
        }
    }

    @Override
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
        pTag.put("tntFrame", tntFrame.serializeNBT());
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ListTag list = pTag.getList("stacks", 10);
        for (int j = 0; j < list.size(); j++) {
            stacks.add(ItemStack.of(list.getCompound(j)));
        }
        tntFrame = ItemStack.of(pTag.getCompound("tntFrame"));
    }

    private static class StackInSlotData {
        private int count; //items needed
        private final IntList slotList = new IntArrayList(); //slots that can provide

        public void add(int count, int slot) {
            this.count += count;
            slotList.add(slot);
        }

        public StackInSlotData(int count, IntList list) {
            this.count = count;
            this.slotList.addAll(list);
        }

        public StackInSlotData() {
        }

        public static Object2IntMap<ItemStackWrapper> transform(List<ItemStack> list) {
            Object2IntMap<ItemStackWrapper> ret = new Object2IntOpenHashMap<>();
            for (ItemStack stack : list) {
                ItemStackWrapper key = new ItemStackWrapper(stack);
                ret.put(key, ret.getInt(key) + stack.getCount());
            }
            return ret;
        }

        public static HashMap<ItemStackWrapper, StackInSlotData> transform(IItemHandler handler) {
            HashMap<ItemStackWrapper, StackInSlotData> ret = new HashMap<>();
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                ItemStackWrapper key = new ItemStackWrapper(stack);
                if (!ret.containsKey(key)) {
                    ret.put(key, new StackInSlotData());
                }
                ret.get(key).add(stack.getCount(), i);
            }
            return ret;
        }

        public static boolean containAll(Object2IntMap<ItemStackWrapper> materials, HashMap<ItemStackWrapper, StackInSlotData> items) {
            for (ItemStackWrapper key : materials.keySet()) {
                if (!items.containsKey(key)) {
                    return false;
                }
                if (items.get(key).count < materials.getInt(key)) {
                    return false;
                }
            }
            return true;
        }

        public static HashMap<ItemStackWrapper, StackInSlotData> transform(Object2IntMap<ItemStackWrapper> materials, HashMap<ItemStackWrapper, StackInSlotData> items) {
            HashMap<ItemStackWrapper, StackInSlotData> ret = new HashMap<>();
            for (ItemStackWrapper key : materials.keySet()) {
                ret.put(key, new StackInSlotData(materials.getInt(key), items.get(key).slotList)); //use slots as items but change count into materials and remove abundant keys
            }
            return ret;
        }

        public static boolean draw(HashMap<ItemStackWrapper, StackInSlotData> items, IItemHandler handler) {
            //simulate
            for (ItemStackWrapper key : items.keySet()) {
                StackInSlotData data = items.get(key);
                int count = data.count;
                boolean success = false;
                for (int i = 0; i < data.slotList.size(); i++) {
                    count -= handler.extractItem(data.slotList.getInt(i), count, true).getCount();
                    if (count <= 0) {
                        success = true;
                    }
                }
                if (!success) {
                    return false;
                }
            }
            //do
            for (ItemStackWrapper key : items.keySet()) {
                StackInSlotData data = items.get(key);
                int count = data.count;
                for (int i = 0; i < data.slotList.size(); i++) {
                    count -= handler.extractItem(data.slotList.getInt(i), count, false).getCount();
                    if (count <= 0) {
                        break;
                    }
                }
            }
            return true;
        }
    }

    public static class ItemStackWrapper {
        public final ItemStack stack;

        public ItemStackWrapper(ItemStack stack1) {
            this.stack = stack1.copy();
            this.stack.setCount(1);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ItemStackWrapper is && ItemStack.matches(this.stack, is.stack);
        }

        @Override
        public int hashCode() {
            return stack.getItem().hashCode() + (stack.getTag()!=null ? stack.getTag().hashCode() : 0);
        }

    }
}
