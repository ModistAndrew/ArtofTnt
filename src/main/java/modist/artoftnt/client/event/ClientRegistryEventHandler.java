package modist.artoftnt.client.event;

import modist.artoftnt.client.block.model.AdditionSpecialRenderer;
import modist.artoftnt.client.block.model.RemoteExploderBlockBakedModel;
import modist.artoftnt.client.block.model.TntFrameBlockBakedModel;
import modist.artoftnt.client.entity.PrimedTntFrameRenderer;
import modist.artoftnt.client.item.model.TntFrameItemBakedModel;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import modist.artoftnt.common.entity.EntityLoader;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.core.addition.Addition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistryEventHandler {

    @SubscribeEvent
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        Arrays.stream(EntityLoader.PRIMED_TNT_FRAMES).forEach(e ->
                event.registerEntityRenderer(e.get(), PrimedTntFrameRenderer::new));
    }

    @SubscribeEvent
    public static void registerBlocks(FMLClientSetupEvent event) {
        Arrays.stream(BlockLoader.TNT_FRAMES).forEach(b ->
                ItemBlockRenderTypes.setRenderLayer(b.get(), RenderType.cutout()));
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.REMOTE_EXPLODER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.TNT_TURRET.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(BlockLoader.TNT_CLONER.get(), RenderType.cutout());
    }

    @SubscribeEvent
    public static void addMarkerModels(ModelRegistryEvent event) {
        Arrays.stream(RemoteExploderBlockBakedModel.MARKER_MODEL_LOCATIONS).forEach(rl -> ForgeModelBakery.addSpecialModel(rl));
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
                .map(rl -> modelRegistry.get(rl)).toArray(BakedModel[]::new);
        event.getModelRegistry().put(modelResourceLocation, new RemoteExploderBlockBakedModel(existingModel, markerModels));
    }

    @SubscribeEvent
    public static void onItemModelBaked(ModelBakeEvent event) {
        Arrays.stream(ItemLoader.TNT_FRAMES).forEach(i -> {
            ModelResourceLocation modelResourceLocation = new ModelResourceLocation(i.get().getRegistryName(), "inventory");
            Map<ResourceLocation, BakedModel> modelRegistry = event.getModelRegistry();
            BakedModel existingModel = modelRegistry.get(modelResourceLocation);
            event.getModelRegistry().put(modelResourceLocation, new TntFrameItemBakedModel(existingModel));
        });
    }

    @SubscribeEvent
    public static void textureStitch(TextureStitchEvent.Pre event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            Addition.TEXTURES.forEach(event::addSprite);
            AdditionSpecialRenderer.TEXTURES.forEach(event::addSprite);
        }
    }

    /*@SubscribeEvent
    public static void blockColors(ColorHandlerEvent.Block event) {
        Arrays.stream(BlockLoader.TNT_FRAMES).forEach(b -> event.getBlockColors().register((bs, l, p, tint) -> {
            if(l!=null && p!=null && l.getBlockEntity(p) instanceof TntFrameBlockEntity tntFrameBlockEntity
                    && tntFrameBlockEntity.getDisguise() != null){
                return tntFrameBlockEntity.getColorForDisguise(l, p, tint); //return color as disguise
            }
            return -1;
        }, b.get()));
    }*/
}