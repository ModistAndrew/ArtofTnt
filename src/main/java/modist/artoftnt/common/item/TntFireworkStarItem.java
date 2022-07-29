package modist.artoftnt.common.item;

import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.core.explosion.CustomExplosion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.FireworkStarItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TntFireworkStarItem extends FireworkStarItem { //see CommonEventHandler
    private static final double[] X_MOTIONS = new double[]{1, 1, 0, -1, -1, -1, 0, 1};
    private static final double[] Z_MOTIONS = new double[]{0, 1, 1, 1, 0, -1, -1, -1};

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
                    PrimedTntFrame entity = new PrimedTntFrame(data,
                            level, explosion.x, explosion.y, explosion.z, explosion.getSourceMob(), tier);
                    entity.shoot(explosion.getVec().x + X_MOTIONS[i], explosion.getVec().y,
                            explosion.getVec().z + Z_MOTIONS[i], 1.0F, 1.0F);
                    level.addFreshEntity(entity);
                }
            }
        }
    }
}
