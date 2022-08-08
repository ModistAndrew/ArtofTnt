package modist.artoftnt.common.integration;

import mcjty.theoneprobe.api.*;
import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.block.entity.TntFrameBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class TOPIntegration implements Function<ITheOneProbe, Void> {

    @Nullable
    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        ArtofTnt.LOGGER.info("Enabled support for The One Probe");
        theOneProbe.registerProvider(new IProbeInfoProvider() {
            @Override
            public ResourceLocation getID() {
                return new ResourceLocation(ArtofTnt.MODID, "tnt_frame_provider");
            }

            @Override
            public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player player, Level level, BlockState blockState, IProbeHitData iProbeHitData) {
                if(level.getBlockEntity(iProbeHitData.getPos()) instanceof TntFrameBlockEntity blockEntity){
                    List<Component> components = new ArrayList<>();
                    blockEntity.getData().addText(components, probeMode!=ProbeMode.NORMAL);
                    components.forEach(iProbeInfo::mcText);
                }
            }
        });
        return null;
    }
}
