package modist.artoftnt.common.advancements.critereon;

import com.google.gson.JsonObject;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.core.addition.TntFrameData;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class IgniteTntFrameTrigger extends SimpleCriterionTrigger<IgniteTntFrameTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(ArtofTnt.MODID, "ignite_tnt_frame");
    public static IgniteTntFrameTrigger TRIGGER;
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject pJson, EntityPredicate.Composite pPlayer, DeserializationContext pContext) {
        TntFrameDataCriteria criteria = TntFrameDataCriteria.fromJson(pJson);
        return new IgniteTntFrameTrigger.TriggerInstance(pPlayer, criteria);
    }

    public void trigger(ServerPlayer pPlayer, TntFrameData data) {
        this.trigger(pPlayer, (trigger) -> trigger.matches(data));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final TntFrameDataCriteria criteria;

        public TriggerInstance(EntityPredicate.Composite pPlayer, TntFrameDataCriteria criteria) {
            super(IgniteTntFrameTrigger.ID, pPlayer);
            this.criteria = criteria;
        }

        public JsonObject serializeToJson(SerializationContext pConditions) {
            JsonObject jsonobject = GsonHelper.convertToJsonObject(this.criteria.serializeToJson(), "criteria");
            JsonObject jsonObject2 = super.serializeToJson(pConditions);
            jsonobject.add("player", jsonObject2.get("player"));
            return jsonobject;
        }

        public boolean matches(TntFrameData data) {
            return criteria.matches(data);
        }
    }
}
