package com.xy.easymagic.network;

import com.xy.easymagic.EasyMagic;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(EasyMagic.MODID);
        INSTANCE.registerMessage(
            MessageReroll.Handler.class,
            MessageReroll.class,
            0,
            Side.SERVER
        );
        INSTANCE.registerMessage(
            MessageCapabilitySync.Handler.class,
            MessageCapabilitySync.class,
            1,
            Side.CLIENT
        );
        INSTANCE.registerMessage(
            MessageEnchantHints.Handler.class,
            MessageEnchantHints.class,
            2,
            Side.CLIENT
        );
    }
}
