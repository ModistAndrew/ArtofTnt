package modist.artoftnt;

import com.mojang.logging.LogUtils;
import modist.artoftnt.common.block.BlockLoader;
import modist.artoftnt.common.entity.EntityLoader;
import modist.artoftnt.common.item.ItemLoader;
import modist.artoftnt.common.recipe.RecipeLoader;
import modist.artoftnt.common.sound.SoundEventRegistry;
import modist.artoftnt.core.addition.AdditionManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ArtofTnt.MODID)
public class ArtofTnt {
    public static final String MODID = "artoftnt";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final IEventBus BUS = FMLJavaModLoadingContext.get().getModEventBus();
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(MODID+"_tools") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemLoader.TNT_SHAPER.get());
        }
    };

    public static final CreativeModeTab ITEM_GROUP_FRAME = new CreativeModeTab(MODID+"_presets") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemLoader.TNT_FRAMES[0].get());
        }
    };
    public ArtofTnt() {
        BlockLoader.BLOCKS.register(ArtofTnt.BUS);
        BlockLoader.BLOCK_ENTITIES.register(ArtofTnt.BUS);
        ItemLoader.ITEMS.register(ArtofTnt.BUS);
        EntityLoader.ENTITY_TYPES.register(ArtofTnt.BUS);
        RecipeLoader.RECIPES.register(ArtofTnt.BUS);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ArtofTntConfig.COMMON_CONFIG);
        AdditionManager.REGISTER.register(ArtofTnt.BUS);
        SoundEventRegistry.SOUNDS.register(ArtofTnt.BUS);
    }
}

