package modist.artoftnt.common.advancements.critereon;

import com.google.gson.*;
import modist.artoftnt.common.JsonUtil;
import modist.artoftnt.core.addition.TntFrameData;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;

public class TntFrameDataCriteria {
    public float size; //max size
    public float weight; //min weight
    public int minTier;
    public int maxTier = 3;
    @Nullable
    public BlockState disguise;
    public final HashMap<AdditionType, Float> minAdditions = new HashMap<>();

    public static TntFrameDataCriteria fromJson(JsonElement data) {
        return JsonUtil.GSON.fromJson(data, TntFrameDataCriteria.class);
    }

    public JsonElement serializeToJson() {
        return JsonUtil.GSON.toJsonTree(this);
    }

    public boolean matches(TntFrameData data) {
        for(AdditionType type : minAdditions.keySet()){
            if(data.getValue(type) < minAdditions.get(type)){
                return false;
            }
        }
        if(size!=0 && data.size > size){
            return false;
        }
        if(disguise!=null && !disguise.equals(data.disguise)){
            return false;
        }
        if(weight!=0 && data.getWeight() < weight){
            return false;
        }
        return data.tier >= minTier && data.tier <= maxTier;
    }
}
