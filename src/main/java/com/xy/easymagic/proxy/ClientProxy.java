package com.xy.easymagic.proxy;

import com.xy.easymagic.client.RenderTileEntityEasyEnchantmentTable;
import com.xy.easymagic.tileentity.TileEntityEasyEnchantmentTable;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityEasyEnchantmentTable.class,
            new RenderTileEntityEasyEnchantmentTable()
        );
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }
}
