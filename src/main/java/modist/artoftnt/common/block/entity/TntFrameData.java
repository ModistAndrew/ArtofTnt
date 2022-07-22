package modist.artoftnt.common.block.entity;

import com.mojang.blaze3d.platform.InputConstants;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.core.addition.AdditionStack;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyboardHandler;
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
import java.awt.im.InputContext;
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

    public TntFrameData(int tier){
        this.tier = tier;
        this.additions = new AdditionStack(tier);
    }

    public TntFrameData(int tier, @Nullable CompoundTag tag){
        this(tier);
        this.deserializeNBT(tag);
    }

    public boolean isEmpty() {
        return !fixed && size==1F && disguise==null && additions.isEmpty();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("fixed", fixed);
        tag.putFloat("size", size);
        if(disguise!=null) {
            tag.put("disguise", NbtUtils.writeBlockState(disguise));
        }
        tag.put("additions", additions.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(@Nullable CompoundTag tag) {
        if(tag!=null) {
            this.fixed = tag.getBoolean("fixed");
            this.size = tag.getFloat("size");
            if (tag.contains("disguise")) {
                this.disguise = NbtUtils.readBlockState(tag.getCompound("disguise"));
            }
            this.additions.deserializeNBT(tag.getCompound("additions"));
        }
    }

    public float getValue(AdditionType type) {
        return additions.getValue(type);
    }

    public Stack<ItemStack> getItems(AdditionType type) {
        return additions.getItems(type);
    }

    public float getDeflation() { //for creating bb easily
        return (1-size)/2;
    }

    @OnlyIn(Dist.CLIENT)
    public void addText(List<Component> pTooltip) { //TODO shift?
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LSHIFT)) {
            int i = 0;
            int j = 0;
            for (AdditionType type : AdditionType.getTypes()) {
                if (type != AdditionType.INSTABILITY && additions.getValue(type) > 0) { //TODO: what to show
                    ++j;
                    if (i <= 4) {
                        ++i;
                        addTooltip(type.toString(), additions.getValue(type), pTooltip);
                    }
                }
            }
            addTooltip("weight", additions.getWeight(), pTooltip);
            addTooltip(AdditionType.INSTABILITY.toString(), additions.getValue(AdditionType.INSTABILITY), pTooltip);
            if (size < 1F) {
                addTooltip("size", size, pTooltip);
            }
            if (disguise != null) {
                addTooltip("disguise", disguise.getBlock(), pTooltip);
            }
            addTooltip("fixed", fixed, pTooltip);
            if (j - i > 0) {
                pTooltip.add((new TranslatableComponent("container.shulkerBox.more", j - i)).withStyle(ChatFormatting.ITALIC));
            }
        }
    }

    public static void addTooltip(String name, Object value, List<Component> pTooltip){
        MutableComponent mutablecomponent = new TranslatableComponent(PREFIX+name);
        mutablecomponent.append("=").append(String.valueOf(value));
        pTooltip.add(mutablecomponent);
    }

    public Stack<ItemStack> getItemStacks(int i) {
        return additions.getItemStacks(i);
    }

    public float getWeight() {
        return additions.getWeight();
    }

    public int getColorForDisguise(@Nullable BlockAndTintGetter pLevel, @Nullable BlockPos pPos, int pTintIndex) {
        if(disguise!=null){
            return Minecraft.getInstance().getBlockColors().getColor(disguise, pLevel, pPos, pTintIndex);
        }
        return -1;
    }

    public int getCoolDown() {
        return this.additions.getCoolDown();
    }
}