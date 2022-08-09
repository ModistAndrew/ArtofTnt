package modist.artoftnt.client.event;

import modist.artoftnt.client.block.TextureLoader;
import modist.artoftnt.client.block.entity.RenderUtil;
import modist.artoftnt.client.block.entity.TntClonerRenderer;
import modist.artoftnt.client.block.entity.TntTurretRenderer;
import modist.artoftnt.client.block.model.RemoteExploderBlockBakedModel;
import modist.artoftnt.client.block.model.TntFrameBlockBakedModel;
import modist.artoftnt.client.entity.PrimedTntFrameRenderer;
import modist.artoftnt.client.item.model.TntFrameItemBakedModel;
import modist.artoftnt.client.item.model.TntShaperBakedModel;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.EntityLoader;
import modist.artoftnt.common.item.ItemLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistryEventHandler {

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        Arrays.stream(EntityLoader.PRIMED_TNT_FRAMES).forEach(e ->
                event.registerEntityRenderer(e.get(), PrimedTntFrameRenderer::new));
    }

    @SubscribeEvent
    public static void registerBlocks(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(BlockLoader.TNT_TURRET_BLOCK_ENTITY.get(), pContext -> new TntTurretRenderer());
        BlockEntityRenderers.register(BlockLoader.TNT_CLONER_BLOCK_ENTITY.get(), pContext -> new TntClonerRenderer());
        Arrays.stream(BlockLoader.TNT_FRAMES).forEach(b ->
                ItemBlockRenderTypes.setRenderLayer(b.get(), RenderType.cutout()));
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.REMOTE_EXPLODER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.TNT_TURRET.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.TNT_CLONER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.REINFORCED_GLASS.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void addSpecialModels(ModelRegistryEvent event) {
        Arrays.stream(RemoteExploderBlockBakedModel.MARKER_MODEL_LOCATIONS).forEach(ForgeModelBakery::addSpecialModel);
        ForgeModelBakery.addSpecialModel(RenderUtil.WHITE_OVERLAY);
        Minecraft.getInstance().getResourceManager().listResources("models/tnt_frame_additions",
                (f) -> f.endsWith(".json")).stream().map(r -> new ResourceLocation(r.getNamespace(),
                r.getPath().replace(".json", "").replace("models/", ""))).forEach(ForgeModelBakery::addSpecialModel);
    }

    @SubscribeEvent
    public static void onBlockModelBaked(ModelBakeEvent event) {
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();
        Arrays.stream(BlockLoader.TNT_FRAMES).forEach(b -> {
            ModelResourceLocation modelResourceLocation = BlockModelShaper.stateToModelLocation(b.get().defaultBlockState());
            BakedModel existingModel = modelRegistry.get(modelResourceLocation);
            event.getModelRegistry().put(modelResourceLocation, new TntFrameBlockBakedModel(existingModel));
        });
        ModelResourceLocation modelResourceLocation =
                BlockModelShaper.stateToModelLocation(BlockLoader.REMOTE_EXPLODER.get().defaultBlockState());
        BakedModel existingModel = modelRegistry.get(modelResourceLocation);
        BakedModel[] markerModels = Arrays.stream(RemoteExploderBlockBakedModel.MARKER_MODEL_LOCATIONS)
                .map(modelRegistry::get).toArray(BakedModel[]::new);
        event.getModelRegistry().put(modelResourceLocation, new RemoteExploderBlockBakedModel(existingModel, markerModels));
    }

    @SubscribeEvent
    public static void onItemModelBaked(ModelBakeEvent event) {
        Arrays.stream(ItemLoader.TNT_FRAMES).forEach(i -> {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(Objects.requireNonNull(i.get().getRegistryName()), "inventory");
            Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();
            BakedModel existingModel = modelRegistry.get(modelResourceLocation);
            event.getModelRegistry().put(modelResourceLocation, new TntFrameItemBakedModel(existingModel));
        });
        ModelResourceLocation modelResourceLocation = new ModelResourceLocation(Objects.requireNonNull(ItemLoader.TNT_SHAPER.get().getRegistryName()), "inventory");
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();
        BakedModel existingModel = modelRegistry.get(modelResourceLocation);
        event.getModelRegistry().put(modelResourceLocation, new TntShaperBakedModel(existingModel));
    }

    @SubscribeEvent
    public static void textureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            Minecraft.getInstance().getResourceManager().listResources("textures/tnt_frame_additions",
                    (f) -> f.endsWith(".png")).stream().map(r -> new ResourceLocation(r.getNamespace(),
                    r.getPath().replace(".png", "").replace("textures/", ""))).forEach(event::addSprite); //force add all
            TextureLoader.TEXTURES.forEach(event::addSprite);
        }
    }

    @SubscribeEvent
    public static void itemColors(ColorHandlerEvent.Item event) {
        event.getItemColors().register((stack, tint) -> {
            if (tint == 1) {
                {
                    CompoundTag compoundtag = stack.getTagElement("Explosion");
                    int[] aint = compoundtag != null && compoundtag.contains("Colors", 11) ? compoundtag.getIntArray("Colors") : null;
                    if (aint != null && aint.length != 0) {
                        if (aint.length == 1) {
                            return aint[0];
                        } else {
                            int i = 0;
                            int j = 0;
                            int k = 0;

                            for (int l : aint) {
                                i += (l & 16711680) >> 16;
                                j += (l & '\uff00') >> 8;
                                k += (l & 255);
                            }

                            i /= aint.length;
                            j /= aint.length;
                            k /= aint.length;
                            return i << 16 | j << 8 | k;
                        }
                    } else {
                        return 9079434;
                    }
                }
            } else if (tint == 0) {
                int count =-1;
                CompoundTag tag = stack.getTagElement("tntFrameData");
                if(tag!=null) {
                    for (int i = 0; i < 8; i++) {
                        if (tag.contains("tnt_" + i)) {
                            count+=2;
                        }
                    }
                }
                return RedStoneWireBlock.getColorForPower(count==-1 ? 0 : count);
            }
            return -1;
        }, ItemLoader.TNT_FIREWORK_STAR.get());
    }
}