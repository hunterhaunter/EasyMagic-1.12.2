package com.xy.easymagic;

import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.PacketHandler;
import com.xy.easymagic.proxy.CommonProxy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
    }
}
