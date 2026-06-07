package com.xy.easymagic;

import com.xy.easymagic.block.BlockEasyEnchantmentTable;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.handler.ModGuiHandler;
import com.xy.easymagic.network.PacketHandler;
import com.xy.easymagic.proxy.CommonProxy;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = EasyMagic.MODID, name = EasyMagic.NAME, version = EasyMagic.VERSION)
public class EasyMagic {

    public static final String MODID = "easymagic";
    public static final String NAME = "Easy Magic";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static EasyMagic instance;

    @SidedProxy(
        clientSide = "com.xy.easymagic.proxy.ClientProxy",
        serverSide = "com.xy.easymagic.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        EasyMagicConfig.init(event.getSuggestedConfigurationFile());
        PacketHandler.init();
        GameRegistry.registerTileEntity(
            TileEntityEasyEnchantmentTable.class,
            new ResourceLocation(MODID, "easy_enchantment_table")
        );
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new ModGuiHandler());
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventBusSubscriber(modid = EasyMagic.MODID)
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
            event.getRegistry().register(
                new BlockEasyEnchantmentTable()
                    .setRegistryName("minecraft", "enchanting_table")
                    .setTranslationKey("enchantmentTable")
            );
        }
    }
}
