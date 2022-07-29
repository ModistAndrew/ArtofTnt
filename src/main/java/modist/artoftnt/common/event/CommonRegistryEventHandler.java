package modist.artoftnt.common.event;

import modist.artoftnt.ArtofTnt;
import modist.artoftnt.common.advancements.critereon.IgniteTntFrameTrigger;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.PrimedTntFrame;
import modist.artoftnt.common.item.TntFrameItem;
import modist.artoftnt.common.loot.functions.TntFrameFunction;
import modist.artoftnt.network.NetworkHandler;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Position;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
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
                    TntFrameItem item = (TntFrameItem) pStack.getItem();
                    return new PrimedTntFrame(item.getTntFrameDataTag(pStack), level, pos.x() + 0.5D, pos.y(),
                            pos.z() + 0.5D, null, finalI);
                }
            });
        }
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        NetworkHandler.registerMessage();
        IgniteTntFrameTrigger.TRIGGER = CriteriaTriggers.register(new IgniteTntFrameTrigger());
        TntFrameFunction.SET_TNT_FRAME_DATA =
                Registry.register(Registry.LOOT_FUNCTION_TYPE, new ResourceLocation(ArtofTnt.MODID, "set_tnt_frame_data"),
                        new LootItemFunctionType(TntFrameFunction.SERIALIZER));
    }

}
