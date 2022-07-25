package modist.artoftnt.common.block.entity;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.TntFrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TntClonerBlockEntity extends CoolDownBlockEntity {
    @Nullable
    private TntFrameData tntFrame;
    protected final List<ItemStack> stacks = new ArrayList<>();

    public TntClonerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
        super(0, BlockLoader.TNT_CLONER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }

    @Override
    protected void doDispense() {
        if (!level.isClientSide && tntFrame != null) {
            ItemStack stack = TntFrameBlock.dropFrame(true, tntFrame);
            Containers.dropItemStack(this.level, this.getBlockPos().getX()
                    , this.getBlockPos().getY(), this.getBlockPos().getZ(), stack);
            this.tntFrame = null;
            stacks.clear();
            setChangedAndUpdate();
        }
    }

    @Override
    public boolean tryActivate(int level) {
        AtomicBoolean ret = new AtomicBoolean(false);
        BlockEntity be = this.level.getBlockEntity(this.getBlockPos().above());
        if (be != null) {
            be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(c -> {
                ret.set(suckItemFrom(c));
            });
        }
        return ret.get();
    }

    private boolean suckItemFrom(IItemHandler handler) {
        if (this.level.getBlockEntity(this.getBlockPos().below()) instanceof TntFrameBlockEntity blockEntity) {
            List<ItemStack> drops = blockEntity.getDrops();
            Object2IntMap<ItemStackWrapper> materials = StackInSlotData.transform(drops);
            HashMap<ItemStackWrapper, StackInSlotData> items = StackInSlotData.transform(handler);
            if (StackInSlotData.containAllAndTransform(materials, items)) {
                HashMap<ItemStackWrapper, StackInSlotData> itemsToSuck = StackInSlotData.transform(materials, items);
                if (StackInSlotData.draw(itemsToSuck, handler)) {
                    this.stacks.addAll(drops);
                    this.tntFrame = blockEntity.getData();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void setCoolDown() {
        if (this.level.getBlockEntity(this.getBlockPos().below()) instanceof TntFrameBlockEntity blockEntity) {
            this.coolDown = blockEntity.getData().getCoolDown() + RENDER_TICK;
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
        if (tntFrame != null) {
            pTag.put("tntFrame", tntFrame.serializeNBT());
            pTag.putInt("tntFrameTier", tntFrame.tier);
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        ListTag list = pTag.getList("stacks", 10);
        for (int j = 0; j < list.size(); j++) {
            stacks.add(ItemStack.of(list.getCompound(j)));
        }
        if (pTag.contains("tntFrame")) {
            tntFrame = new TntFrameData(pTag.getInt("tntFrameTier"), pTag.getCompound("tntFrame"));
        } else {
            tntFrame = null;
        }
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

        public static boolean containAllAndTransform(Object2IntMap<ItemStackWrapper> materials, HashMap<ItemStackWrapper, StackInSlotData> items) {
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

        private void setCount(int count) {
            this.count = count;
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

    private static class ItemStackWrapper {
        private final ItemStack stack;

        private ItemStackWrapper(ItemStack stack1) {
            this.stack = stack1.copy();
            this.stack.setCount(1);
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof ItemStackWrapper is && ItemStack.matches(this.stack, is.stack);
        }

        @Override
        public int hashCode() {
            return stack.getItem().hashCode() + (stack.hasTag() ? stack.getTag().hashCode() : 0);
        }

    }
}
