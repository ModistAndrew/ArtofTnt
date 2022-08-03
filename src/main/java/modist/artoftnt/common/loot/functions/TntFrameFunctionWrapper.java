package modist.artoftnt.common.loot.functions;

import modist.artoftnt.core.addition.TntFrameData;
import modist.artoftnt.common.item.TntFrameItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TntFrameFunctionWrapper implements INBTSerializable<CompoundTag> {
    public final List<ItemStack> additions = new ArrayList<>();
    public float size = 1F;
    @Nullable
    public BlockState disguise;
    @Nullable
    public String name;
    public boolean fixed;
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag ret = new CompoundTag();
        ret.putFloat("size", size);
        if(disguise!=null){
            ret.put("disguise", NbtUtils.writeBlockState(disguise));
        }
        ret.putBoolean("fixed", fixed);
        ListTag list = new ListTag();
        for (ItemStack addition : additions) {
            list.add(addition.serializeNBT());
        }
        ret.put("additions", list);
        return ret;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(nbt==null){
            nbt = new CompoundTag();
        }
        this.size = nbt.getFloat("size");
        if(nbt.contains("disguise")){
            disguise = NbtUtils.readBlockState(nbt.getCompound("disguise"));
        } else {
            disguise = null;
        }
        this.fixed = nbt.getBoolean("fixed");
        ListTag list = nbt.getList("additions", 10);
        additions.clear();
        for(int i =0; i<list.size(); i++){
            additions.add(ItemStack.of(list.getCompound(i)));
        }
    }

    public ItemStack apply(ItemStack itemStack) {
        if(itemStack.getItem() instanceof TntFrameItem item){
            TntFrameData data = new TntFrameData(item.tier);
            for(ItemStack stack : additions) {
                data.additions.add(stack);
            }
            data.size = size;
            data.disguise = disguise;
            data.fixed = fixed;
            TntFrameItem.putTntFrameData(itemStack, data);
            if(name!=null){
                itemStack.setHoverName(new TextComponent(name));
            }
        }
        return itemStack;
    }
}