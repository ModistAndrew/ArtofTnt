package modist.artoftnt.core.addition;

import com.mojang.blaze3d.platform.InputConstants;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Stack;

public class TntFrameData implements INBTSerializable<CompoundTag> {
    public final int tier;
    public boolean fixed;
    public float size = 1F;
    @Nullable
    //null for no disguise, air for special
    public BlockState disguise;
    public final AdditionStack additions;
    private static final String PREFIX = "container.artoftnt.";

    public TntFrameData(int tier) {
        this.tier = tier;
        this.additions = new AdditionStack(tier);
    }

    public TntFrameData(int tier, @Nullable CompoundTag tag) {
        this(tier);
        this.deserializeNBT(tag);
    }

    public boolean isEmpty() {
        return !fixed && size == 1F && disguise == null && additions.isEmpty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("fixed", fixed);
        tag.putFloat("size", size);
        if (disguise != null) {
            tag.put("disguise", NbtUtils.writeBlockState(disguise));
        }
        tag.put("additions", additions.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag) {
        if (tag != null) {
            if(tag.contains("fixed")) {
                this.fixed = tag.getBoolean("fixed");
            }
            if(tag.contains("size")) {
                this.size = tag.getFloat("size");
            }
            if (tag.contains("disguise")) {
                this.disguise = NbtUtils.readBlockState(tag.getCompound("disguise"));
            }
            if(tag.contains("additions")) {
                this.additions.deserializeNBT(tag.getCompound("additions"));
            }
        }
    }

    public float getValue(AdditionType type) {
        return additions.getValue(type);
    }

    public Stack<ItemStack> getItems(AdditionType type) {
        return additions.getItems(type);
    }

    public float getDeflation() { //for creating bb easily
        return (1 - size) / 2;
    }

    @OnlyIn(Dist.CLIENT)
    public void addText(List<Component> pTooltip) {
        if(this.isEmpty()){
            addTooltip("empty_frame", null, pTooltip, ChatFormatting.GREEN);
            return;
        }
        addTooltip("weight", additions.getWeight(), pTooltip, ChatFormatting.AQUA);
        addTooltip(AdditionType.INSTABILITY.toString(), additions.getValue(AdditionType.INSTABILITY), pTooltip, ChatFormatting.AQUA);
        addTooltip("fixed", fixed, pTooltip, ChatFormatting.AQUA);
        if (size < 1F) {
            addTooltip("size", size, pTooltip, ChatFormatting.AQUA);
        }
        if (disguise != null) {
            addTooltip("disguise", new TranslatableComponent(disguise.getBlock().getDescriptionId()), pTooltip, ChatFormatting.AQUA);
        }
        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT)) {
            addTooltip("type_values", null, pTooltip, ChatFormatting.BLACK);
            for (AdditionType type : AdditionType.getTypes()) {
                if (type != AdditionType.INSTABILITY && additions.getValue(type) != type.initialValue) {
                    addTooltip(type.toString(), additions.getValue(type), pTooltip);
                }
            }
        } else {
            addTooltip("press_shift", null, pTooltip, ChatFormatting.ITALIC,ChatFormatting.RED);
            for(int i=0; i<18; i++){
                for(ItemStack stack :additions.getItemStacks(i)){
                    if(!stack.isEmpty()){
                        MutableComponent mutablecomponent = stack.getHoverName().copy();
                        mutablecomponent.append(" x").append(String.valueOf(stack.getCount()));
                        pTooltip.add(mutablecomponent);
                    }
                }
            }
        }
    }

    public static void addTooltip(String name, @Nullable Object value, List<Component> pTooltip, ChatFormatting... pFormats) {
        MutableComponent mutablecomponent = new TranslatableComponent(PREFIX + name);
        if(value!=null) {
            if(value instanceof Float f){
                mutablecomponent.append(": ").append(String.valueOf(AdditionStack.r(f)));
            } else if(value instanceof TranslatableComponent component){
                mutablecomponent.append(": ").append(component);
            } else {
                mutablecomponent.append(": ").append(String.valueOf(value));
            }
        }
        pTooltip.add(mutablecomponent.withStyle(pFormats));
    }
    public Stack<ItemStack> getItemStacks(int i) {
        return additions.getItemStacks(i);
    }

    public float getWeight() {
        return additions.getWeight();
    }

    public int getColorForDisguise(@Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex) {
        if (disguise != null) {
            return Minecraft.getInstance().getBlockColors().getColor(disguise, pLevel, pPos, pTintIndex);
        }
        return -1;
    }

    public int getCoolDown() {
        return this.additions.getCoolDown();
    }
}