package modist.artoftnt.common;

import com.google.gson.*;
import modist.artoftnt.core.addition.AdditionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.lang.reflect.Type;
import java.util.Objects;

public class JsonUtil {
    public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().enableComplexMapKeySerialization().registerTypeAdapter(ItemStack.class,
            new ItemStackSerializer()).registerTypeAdapter(BlockState.class, new BlockStateSerializer())
            .registerTypeAdapter(AdditionType.class, new AdditionTypeSerializer()).create();

    public static class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

        @Override
        public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return CraftingHelper.getItemStack(json.getAsJsonObject(), true);
        }

        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            CompoundTag allTag = src.serializeNBT();

            String item = Objects.requireNonNull(src.getItem().getRegistryName()).toString();
            result.addProperty("item", item);

            CompoundTag tag = allTag.getCompound("tag");
            if (allTag.contains("ForgeCaps")) {
                var fCap = allTag.getCompound("ForgeCaps");
                tag.put("ForgeCaps", fCap);
            }

            if (!tag.isEmpty()) {
                JsonPrimitive nbt = new JsonPrimitive(tag.toString());
                result.add("nbt", nbt);
            }

            result.addProperty("count", src.getCount());

            return result;
        }
    }

    public static class BlockStateSerializer implements JsonSerializer<BlockState>, JsonDeserializer<BlockState> {

        @Override
        public BlockState deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = GsonHelper.convertToJsonObject(json, "blockState");
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", GsonHelper.getAsString(object, "Name"));
            if (object.has("Properties")) {
                CompoundTag propertyTag = new CompoundTag();
                JsonObject properties = GsonHelper.getAsJsonObject(object, "Properties");
                for (String s : properties.keySet()) {
                    propertyTag.putString(s, GsonHelper.getAsString(properties, s));
                }
                tag.put("Properties", propertyTag);
            }
            return NbtUtils.readBlockState(tag);
        }

        @Override
        public JsonElement serialize(BlockState src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            CompoundTag allTag = NbtUtils.writeBlockState(src);

            String block = allTag.getString("Name");
            result.addProperty("Name", block);

            if (allTag.contains("Properties")) {
                JsonObject properties = new JsonObject();
                CompoundTag compoundtag1 = allTag.getCompound("Properties");
                for (String s : compoundtag1.getAllKeys()) {
                    properties.addProperty(s, compoundtag1.getString(s));
                }
                result.add("Properties", properties);
            }
            return result;
        }
    }

    public static class AdditionTypeSerializer implements JsonSerializer<AdditionType>, JsonDeserializer<AdditionType> {

        @Override
        public AdditionType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return AdditionType.fromString(GsonHelper.convertToString(json, "additionType"));
        }

        @Override
        public JsonElement serialize(AdditionType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }

    public class ResourceLocationSerializer implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
        @Override
        public ResourceLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return ResourceLocation.tryParse(json.getAsString());
        }

        @Override
        public JsonElement serialize(ResourceLocation src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }
    }
}