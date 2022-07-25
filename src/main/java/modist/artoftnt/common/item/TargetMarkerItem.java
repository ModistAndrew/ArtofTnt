package modist.artoftnt.common.item;

import modist.artoftnt.common.block.entity.TntFrameData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.transformer.ext.IDecompiler;

import javax.annotation.Nullable;
import java.util.List;

public class TargetMarkerItem extends PositionMarkerItem { //see CommonEventHandler

    public TargetMarkerItem(int tier) {
        super(tier, false);
    }

    @Override
    @Nullable
    public Vec3 getPos(@Nullable Level level, Vec3 posFrom, ItemStack stack){
        if(level==null){
            return null;
        }
        CompoundTag tag = stack.getTagElement("position");
        if(tag==null){
            return null;
        }
        if(tag.contains("entityClass")){
            String className = tag.getString("entityClass");
            String uuid = tag.getString("UUID");
            try {
                Class<Entity> clazz = (Class<Entity>)Class.forName(className);
                List<Entity> list = level.getEntitiesOfClass(clazz, super.getRange().move(posFrom), e ->
                        e.getStringUUID().equals(uuid));
                if(list.isEmpty()){
                    return null;
                }
                return list.get(0).position();
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        return super.getPos(level, posFrom, stack);
    }

    public void saveEntity(ItemStack stack, Entity target) {
        CompoundTag tag = new CompoundTag();
        tag.putString("entityClass", target.getClass().getName());
        tag.putString("UUID", target.getStringUUID());
        tag.putString("entityName", target.getDisplayName().getString()); //for tooltip
        stack.addTagElement("position", tag);
    }
}
