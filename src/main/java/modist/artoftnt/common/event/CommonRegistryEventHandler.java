package modist.artoftnt.common.event;

import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.PrimedTntFrame;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonRegistryEventHandler {

    @SubscribeEvent
    public static void registerDispenser(FMLCommonSetupEvent event) {
        for (int i = 0; i < 4; i++) {
            int finalI = i;
            DispenserBlock.registerBehavior(BlockLoader.TNT_FRAMES[i].get(), new AbstractProjectileDispenseBehavior() {
                protected Projectile getProjectile(Level level, Position pos, ItemStack pStack) {
                    return new PrimedTntFrame(pStack.getTagElement("tntFrameData"), level, pos.x() + 0.5D, pos.y(),
                            (double) pos.z() + 0.5D, null, finalI);
                }
            });
        }
    }

    ;
}
