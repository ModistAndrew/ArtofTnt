package modist.artoftnt.common.item;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import modist.artoftnt.common.block.entity.TntTurretBlockEntity;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.core.addition.AdditionType;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.FireworkStarItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TntFireworkStarItem extends FireworkStarItem { //see CommonEventHandler

    public TntFireworkStarItem() {
        super(ItemLoader.getProperty());
    }

    public static void putData(ItemStack stack, int id, CompoundTag data, int tier){
        if(data==null){
            data = new CompoundTag(); //add to allow color
        }
        data.putInt("tier", tier); //have to store
        stack.getOrCreateTagElement("tntFrameData").put("tnt_" + id, data); //null data won't be added
    }

    public static void shoot(Level level, ItemStack stack, CustomExplosion explosion){
        CompoundTag tag = stack.getTagElement("tntFrameData");
        if(tag!=null){
            for(int i=0; i<8; i++){
                if(tag.contains("tnt_"+i)){
                    CompoundTag data = tag.getCompound("tnt_"+i);
                    int tier = data.getInt("tier");
                    data.remove("tier");
                    data.putBoolean("fixed", true); //or tnt may be duplicated
                    PrimedTntFrame entity = new PrimedTntFrame(data,
                            level, explosion.x, explosion.y, explosion.z, explosion.getSourceMob(), tier);
                    float h = entity.data.getValue(AdditionType.PUNCH_HORIZONTAL);
                    float v = entity.data.getValue(AdditionType.PUNCH_VERTICAL);
                    Vec3 explosionVec = explosion.getVec().normalize();
                    Vector3f normal = new Vector3f(TntTurretBlockEntity.getNormal(explosionVec));
                    normal.transform(new Quaternion(new Vector3f(explosionVec), 45 * i, true));
                    normal.mul(h+1); //horizontal should be at least 1
                    Vec3 direction = explosionVec.scale(v).add(new Vec3(normal));
                    entity.shoot(direction.x, direction.y, direction.z, (float) direction.length(), 1.0F);
                    level.addFreshEntity(entity);
                }
            }
        }
    }
}
