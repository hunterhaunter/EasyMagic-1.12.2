package com.xy.easymagic.network;

import com.xy.easymagic.EasyMagic;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class PacketHandler {

    public static SimpleNetworkWrapper INSTANCE;

    public static void init() {
        INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(EasyMagic.MODID);
        int id = 0;
        INSTANCE.registerMessage(
            MessageEnchantingData.Handler.class,
            MessageEnchantingData.class,
            id++,
            Side.CLIENT
        );
        INSTANCE.registerMessage(
            MessageReroll.Handler.class,
            MessageReroll.class,
            id++,
            Side.SERVER
        );
        INSTANCE.registerMessage(
            MessageEnchantItem.Handler.class,
            MessageEnchantItem.class,
            id++,
            Side.SERVER
        );
    }
}
