package modist.artoftnt.core.addition;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import modist.artoftnt.ArtofTntConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.*;

public class AdditionStack implements INBTSerializable<CompoundTag> {
    @SuppressWarnings("unchecked")
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
    private static final String PREFIX = "reply.artoftnt.";

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

    public record AdditionResult(boolean success, @Nullable Component reply){}

    public AdditionResult add(ItemStack itemStack) {
        if (itemStack.getCount() > 1) { //simply add for several times
            int count = itemStack.getCount();
            boolean ret = false;
            ItemStack itemStack1 = itemStack.copy();
            itemStack1.setCount(1);
            for (int i = 0; i < count; i++) {
                ret |= add(itemStack1).success;
            }
            return new AdditionResult(ret, null);
        }
        Addition addition = Addition.fromItem(itemStack.getItem());
        if (addition == null) {
            return new AdditionResult(false, getComponent("item_not_supported", itemStack.getItem().getDescriptionId()));
        }
        AdditionResult countResult = checkCount(addition);
        if (!countResult.success) {
            return countResult;
        }
        if(addition.minTier > this.tier) {
            return new AdditionResult(false, getComponent("tier_low", null, this.tier+1, addition.minTier+1));
        }
        if(addition.maxTier < this.tier) {
            return new AdditionResult(false, getComponent("tier_high", null, this.tier+1, addition.maxTier+1));
        }
        return new AdditionResult(true, addAndUpdate(getFreeSlot(addition.type.slot), itemStack));
    }

    private AdditionResult checkCount(Addition addition) {
        if(additionCounts.getInt(addition) >= addition.maxCount){
            return new AdditionResult(false, getComponent("addition_full", addition.item.getDescriptionId()));
        }
        if(typeCounts.getInt(addition.type) >= addition.type.maxCount){
            return new AdditionResult(false, getComponent("type_full", "container.artoftnt."+addition.type));
        }
        if(slotCounts.getInt(addition.type.slot) >= addition.type.slot.maxCount){
            return new AdditionResult(false, getComponent("slot_full", null, addition.type.slot.name()));
        }
        if(!hasFreeSlot(addition.type.slot)){
            return new AdditionResult(false, getComponent("full", null));
        }
        return new AdditionResult(true, null);
    }

    private Component getComponent(String key, @Nullable String more, Object... pArgs) {
        MutableComponent mutablecomponent = new TranslatableComponent(PREFIX + key, pArgs);
        if(more!=null){
            mutablecomponent.append(new TranslatableComponent(more));
        }
        return mutablecomponent;
    }

    private Component addAndUpdate(int freeSlot, ItemStack itemStack) {
        Component component = null;
        if (!additions[freeSlot].isEmpty() && ItemEntity.areMergable(additions[freeSlot].peek(), itemStack)) {
            additions[freeSlot].peek().grow(1);
        } else {
            additions[freeSlot].push(itemStack.copy());
        }
        Addition addition = Addition.fromItem(itemStack.getItem());
        AdditionType type = addition.type;
        AdditionSlot slot = type.slot;
        List<AdditionType> loss = new ArrayList<>();
        for(AdditionType requirement : type.requirements){
            if(getValue(requirement) == 0){
                loss.add(requirement);
            }
        }
        if(!loss.isEmpty()){
            MutableComponent mutablecomponent = new TranslatableComponent(PREFIX + "loss");
            for(int i=0; i<loss.size(); i++){
                AdditionType t = loss.get(i);
                mutablecomponent.append(new TranslatableComponent("container.artoftnt."+t.toString()));
                if(i!=loss.size()-1) {
                    mutablecomponent.append(", ");
                }
            }
            component = mutablecomponent;
        }
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
        return component;
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
        if(type == AdditionType.INSTABILITY){
            return r(Math.max(0,
                    type.weightEffect.apply(typeStorage.containsKey(type) ? typeStorage.get(type).value : 0, weight)+instability));
        }
        return r(type.weightEffect.apply(typeStorage.containsKey(type) ? typeStorage.get(type).value : 0, weight));
    }

    public static float r(float f){
        return (float)(Math.round(f*10))/10;
    }

    public Stack<ItemStack> getItems(AdditionType type) {
        return typeStorage.containsKey(type) ? AdditionTypeStorage.getSplit(typeStorage.get(type).itemStacks) : new Stack<>();
    }

    public boolean persistent() {
        return getValue(AdditionType.FUSE) >= ArtofTntConfig.MIN_PERSISTENT_FUSE.get();
    }

    public boolean sound() {
        return getValue(AdditionType.SOUND_TYPE) > 0;
    }

    public boolean globalSound() {
        return getValue(AdditionType.LOUDNESS) >= ArtofTntConfig.MIN_GLOBAL_SOUND_LOUDNESS.get();
    }

    public float getWeight() {
        return Math.max(0, this.weight);
    }

    public int getCoolDown() {
        return (int)Math.max(1, this.weight - this.getValue(AdditionType.QUICK_CHARGE));
    }

    public boolean shouldAttack(@Nullable Entity owner, Entity target){
        float royalty = getValue(AdditionType.LOYALTY);
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
        for (Stack<ItemStack> addition : additions) {
            if (!addition.isEmpty()) {
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

        public static Stack<ItemStack> getSplit(Stack<ItemStack> itemStacks) { //split into 1
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
