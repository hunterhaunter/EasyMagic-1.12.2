package com.xy.easymagic.capability;

import com.xy.easymagic.EasyMagic;

import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

@Mod.EventBusSubscriber(modid = EasyMagic.MODID)
public class BlockBreakHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        World world = event.getWorld();
        if (world.isRemote) return;
        if (!(event.getState().getBlock() instanceof BlockEnchantmentTable)) return;

        BlockPos pos = event.getPos();
        TileEntity te = world.getTileEntity(pos);
        if (te == null) return;
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(cap instanceof EasyMagicItemHandler)) return;
        for (int i = 0; i < cap.getSlots(); i++) {
            ItemStack stack = cap.getStackInSlot(i);
            if (!stack.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
    }
}
