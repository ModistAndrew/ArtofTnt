package modist.artoftnt.core.addition;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.*;

public class AdditionStack implements INBTSerializable<CompoundTag> {
    private final Stack<ItemStack>[] additions = new Stack[18]; //stored
    public final int tier; //immutable
    //following should be initialized after additions is loaded; all should be updated after an item is added
    //update both on client and on server side
    private final HashMap<AdditionType, AdditionTypeStorage> typeStorage = new HashMap<>(); //watch out null pointer
    private final int[] counts = new int[18]; //5, 6, 9, 10 for free slots; 16 for fuse, 17 for shape; max 8
    private final Object2IntMap<Addition> additionCounts = new Object2IntOpenHashMap<>();
    private final Object2IntMap<AdditionType> typeCounts = new Object2IntOpenHashMap<>();
    private final Object2IntMap<AdditionSlot> slotCounts = new Object2IntOpenHashMap<>();
    private int usedFreeSlotCounts;
    private float weight;
    private float instability;

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        for (int i = 0; i < 18; i++) {
            ListTag list = new ListTag();
            for (int j = 0; j < additions[i].size(); j++) {
                list.add(j, additions[i].get(j).serializeNBT());
            }
            tag.put("stack" + i, list);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        typeStorage.clear();
        additionCounts.clear();
        typeCounts.clear();
        slotCounts.clear();
        usedFreeSlotCounts = 0;
        weight = 0;
        instability = 0;
        for (int i = 0; i < 18; i++) {
            additions[i].clear();
            counts[i] = 0;
        }
        for (int i = 0; i < 18; i++) {
            ListTag list = tag.getList("stack" + i, 10);
            for (int j = 0; j < list.size(); j++) {
                add(ItemStack.of(list.getCompound(j))); //update
            }
        }
    }

    public AdditionStack(int tier) {
        this.tier = tier;
        for (int i = 0; i < 18; i++) {
            additions[i] = new Stack<>();
        }
    }

    public Stack<ItemStack> getItemStacks(int i) {
        return additions[i];
    }

    public AdditionStack(int tier, CompoundTag tag) {
        this(tier);
        this.deserializeNBT(tag);
    }

    public boolean add(ItemStack itemStack) {
        if (itemStack.getCount() > 1) { //simply add for several times
            int count = itemStack.getCount();
            boolean ret = false;
            ItemStack itemStack1 = itemStack.copy();
            itemStack1.setCount(1);
            for (int i = 0; i < count; i++) {
                ret |= add(itemStack1);
            }
            return ret;
        }
        Addition addition = Addition.fromItem(itemStack.getItem());
        if (addition == null) {
            return false;
        }
        if (!checkCount(addition)) {
            return false;
        }
        if(addition.minTier > this.tier) {
            return false;
        }
        addAndUpdate(getFreeSlot(addition.type.slot), itemStack);
        return true;
    }

    private void addAndUpdate(int freeSlot, ItemStack itemStack) {
        if (!additions[freeSlot].isEmpty() && ItemEntity.areMergable(additions[freeSlot].peek(), itemStack)) {
            additions[freeSlot].peek().grow(1);
        } else {
            additions[freeSlot].push(itemStack.copy());
        }
        Addition addition = Addition.fromItem(itemStack.getItem());
        AdditionType type = addition.type;
        AdditionSlot slot = type.slot;
        if (!typeStorage.containsKey(type)) {
            typeStorage.put(type, new AdditionTypeStorage()); //initialize
        }
        typeStorage.get(type).update(itemStack);
        counts[freeSlot]++; //free slot may not be slot index!
        additionCounts.put(addition, additionCounts.getInt(addition) + 1);
        typeCounts.put(type, typeCounts.getInt(type) + 1);
        slotCounts.put(slot, slotCounts.getInt(slot) + 1);
        if (freeSlot != slot.index) {
            usedFreeSlotCounts++;
        }
        weight += addition.weight;
        instability += addition.instability;
    }

    private boolean checkCount(Addition addition) {
        return additionCounts.getInt(addition) < addition.maxCount &&
                typeCounts.getInt(addition.type) < addition.type.maxCount &&
                slotCounts.getInt(addition.type.slot) < addition.type.slot.maxCount &&
                hasFreeSlot(addition.type.slot);
    }

    private boolean hasFreeSlot(AdditionSlot slot) {
        return counts[slot.index] < 8 || usedFreeSlotCounts < 32;
    }

    private int getFreeSlot(AdditionSlot slot) {
        return counts[slot.index] < 8 ? slot.index : getFreeSlotIndex();
    }

    private int getFreeSlotIndex() {
        return switch (usedFreeSlotCounts / 8) {
            case 0 -> 5;
            case 1 -> 6;
            case 2 -> 9;
            case 3 -> 10;
            default -> -1;
        };
    }

    public float getValue(AdditionType type) {
        float f = type == AdditionType.INSTABILITY ? instability : 0; //specially consider instability
        return type.weightEffect.apply(typeStorage.containsKey(type) ? typeStorage.get(type).value : 0, weight) + f;
    }

    public Stack<ItemStack> getItems(AdditionType type) {
        return typeStorage.containsKey(type) ? typeStorage.get(type).getSplit() : new Stack<>();
    }

    public float getWeight() {
        return this.weight;
    }

    public int getCoolDown() {
        return (int)Math.max(1, this.weight - this.getValue(AdditionType.QUICK_CHARGE));
    }

    public boolean shouldAttack(@Nullable Entity owner, Entity target){
        float royalty = getValue(AdditionType.ROYALTY);
        if(owner==null){
            return royalty >= 0;
        }
        if(royalty<0){
            return owner==target || owner.isAlliedTo(target);
        } else if (royalty > 0) {
            return owner!=target && !owner.isAlliedTo(target);
        }
        return true;
    }
    public boolean isEmpty() {
        for(int i=0; i<additions.length; i++){
            if(!additions[i].isEmpty()){
                return false;
            }
        }
        return true;
    }

    public static class AdditionTypeStorage {
        private float value;
        private final Stack<ItemStack> itemStacks = new Stack<>();

        public void update(ItemStack stack) {
            if (!itemStacks.isEmpty() && ItemEntity.areMergable(itemStacks.peek(), stack)) {
                itemStacks.peek().grow(1);
            } else {
                itemStacks.push(stack.copy());
            }
            value += Addition.fromItem(stack.getItem()).increment;
        }

        public Stack<ItemStack> getSplit() { //split into 1
            Stack<ItemStack> ret = new Stack<>();
            for(ItemStack stack : itemStacks){
                for(int i=0; i<stack.getCount(); i++){
                    ItemStack stack1 = stack.copy();
                    stack1.setCount(1);
                    ret.push(stack1);
                }
            }
            return ret;
        }
    }
}
