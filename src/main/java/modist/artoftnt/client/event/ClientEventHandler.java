package modist.artoftnt.client.event;

import modist.artoftnt.client.block.entity.RenderUtil;
import modist.artoftnt.client.block.entity.TntTurretRenderer;
import modist.artoftnt.client.block.model.RemoteExploderBlockBakedModel;
import modist.artoftnt.client.block.model.TntFrameBlockBakedModel;
import modist.artoftnt.client.entity.PrimedTntFrameRenderer;
import modist.artoftnt.client.item.model.TntFrameItemBakedModel;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.EntityLoader;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.item.PositionMarkerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Arrays;
import java.util.Map;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void render(RenderLevelLastEvent event) {
        Player player = Minecraft.getInstance().player;
        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() instanceof PositionMarkerItem item) {
            Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            event.getPoseStack().pushPose();
            event.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
            Vec3 pos = item.getPos(Minecraft.getInstance().level, player.position(), stack);
            if (pos != null) {
                RenderUtil.renderLine(Minecraft.getInstance().renderBuffers().bufferSource(), event.getPoseStack(), player.position(), pos);
            }
        }
    }

}