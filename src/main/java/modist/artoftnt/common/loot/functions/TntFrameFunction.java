package modist.artoftnt.common.loot.functions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import modist.artoftnt.common.JsonUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TntFrameFunction extends LootItemConditionalFunction {

    public TntFrameFunctionWrapper wrapper = new TntFrameFunctionWrapper();
    public static final TntFrameFunction.Serializer SERIALIZER = new Serializer();
    public static LootItemFunctionType SET_TNT_FRAME_DATA;

    public TntFrameFunction(LootItemCondition[] pConditions) {
        super(pConditions);
    }

    public LootItemFunctionType getType() {
        return SET_TNT_FRAME_DATA;
    }

    @Override
    protected ItemStack run(ItemStack pStack, LootContext pContext) {
        return wrapper.apply(pStack);
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<TntFrameFunction> {

        public void serialize(JsonObject pJson, TntFrameFunction pValue, JsonSerializationContext pSerializationContext) {
            JsonElement json = JsonUtil.GSON.toJsonTree(pValue.wrapper);
            pJson.add("data", json);
            super.serialize(pJson, pValue, pSerializationContext);
        }

        @Override
        public TntFrameFunction deserialize(JsonObject pObject, JsonDeserializationContext pDeserializationContext, LootItemCondition[] pConditions) {
            TntFrameFunction ret = new TntFrameFunction(pConditions);
            ret.wrapper = JsonUtil.GSON.fromJson(pObject, TntFrameFunctionWrapper.class);
            return ret;
        }
    }
}