package modist.artoftnt.client.event;

import modist.artoftnt.client.block.entity.RenderUtil;
import modist.artoftnt.common.item.PositionMarkerItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void render(RenderLevelLastEvent event) {
        Player player = Minecraft.getInstance().player;
        if(player!=null) {
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

}