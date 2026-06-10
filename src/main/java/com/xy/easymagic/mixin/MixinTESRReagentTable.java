package com.xy.easymagic.mixin;

import com.xy.easymagic.IEasyMagicContainer;
import com.xy.easymagic.client.EnchantedItemRenderer;

import logictechcorp.reagenchant.client.renderer.tileentity.TileEntityReagentTableRenderer;
import logictechcorp.reagenchant.tileentity.TileEntityReagentTable;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityReagentTableRenderer.class)
public abstract class MixinTESRReagentTable {

    @Inject(method = "render", at = @At("RETURN"), remap = false)
    private void easymagic$renderFloatingItems(
            TileEntityReagentTable reagentTable, double x, double y, double z,
            float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        float time = (float) reagentTable.getTickCounter() + partialTicks;

        ItemStack itemToEnchant = ItemStack.EMPTY;
        ItemStack lapisStack = ItemStack.EMPTY;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && mc.player.openContainer instanceof IEasyMagicContainer) {
            IEasyMagicContainer emc = (IEasyMagicContainer) mc.player.openContainer;
            if (reagentTable.getPos().equals(emc.easymagic$getPosition())) {
                itemToEnchant = emc.easymagic$getTableItem();
                lapisStack = emc.easymagic$getLapisItem();
            }
        }

        if (itemToEnchant.isEmpty()) {
            itemToEnchant = reagentTable.getInventory().getStackInSlot(0);
        }
        if (lapisStack.isEmpty()) {
            lapisStack = reagentTable.getInventory().getStackInSlot(1);
        }

        if (!itemToEnchant.isEmpty()) {
            EnchantedItemRenderer.renderHoveringItem(itemToEnchant, x, y, z, time,
                    partialTicks, reagentTable.getBookSpread(), reagentTable.getBookSpreadPrev());
        }

        if (!lapisStack.isEmpty()) {
            int count = Math.min(lapisStack.getCount(), 4);
            EnchantedItemRenderer.renderOrbitingLapis(lapisStack, x, y, z, time, count);
        }
    }
}
