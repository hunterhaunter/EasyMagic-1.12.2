package com.xy.easymagic.capability;

import com.xy.easymagic.EasyMagic;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = EasyMagic.MODID)
public class CapabilityHandler {

    public static final ResourceLocation CAPABILITY_KEY =
            new ResourceLocation(EasyMagic.MODID, "enchant_inventory");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.tileentity.TileEntity> event) {
        if (event.getObject() instanceof TileEntityEnchantmentTable) {
            event.addCapability(CAPABILITY_KEY, new EasyMagicCapabilityProvider());
        }
    }
}
