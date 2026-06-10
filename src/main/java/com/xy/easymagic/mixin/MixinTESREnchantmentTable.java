package com.xy.easymagic.mixin;

import com.xy.easymagic.capability.EasyMagicItemHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityEnchantmentTableRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityEnchantmentTableRenderer.class)
public abstract class MixinTESREnchantmentTable {

    @Inject(method = "render", at = @At("RETURN"))
    private void easymagic$renderCapabilityItems(
            TileEntityEnchantmentTable te, double x, double y, double z,
            float partialTicks, int destroyStage, float alpha, CallbackInfo ci) {
        IItemHandler cap = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (!(cap instanceof EasyMagicItemHandler)) return;
        EasyMagicItemHandler handler = (EasyMagicItemHandler) cap;

        float time = (float) te.tickCount + partialTicks;

        ItemStack itemToEnchant = handler.getStackInSlot(0);
        if (!itemToEnchant.isEmpty()) {
            easymagic$renderHoveringItem(itemToEnchant, x, y, z, time, te);
        }

        ItemStack lapisStack = handler.getStackInSlot(1);
        if (!lapisStack.isEmpty()) {
            int count = Math.min(lapisStack.getCount(), 4);
            easymagic$renderOrbitingLapis(lapisStack, x, y, z, time, count);
        }
    }

    @Unique
    private void easymagic$renderHoveringItem(
            ItemStack stack, double x, double y, double z,
            float time, TileEntityEnchantmentTable te) {
        float openState = te.bookSpreadPrev + (te.bookSpread - te.bookSpreadPrev) * (time - (float) te.tickCount);
        if (openState <= 0.0F && te.bookSpreadPrev <= 0.0F) return;

        float bobY = MathHelper.sin(time / 10.0F) * 0.1F + 0.1F;
        float hoverY = 0.25F * openState;
        float sinkOffset = 0.15F * (1.0F - openState);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 1.0 + bobY + hoverY - sinkOffset, z + 0.5);
        GlStateManager.rotate((time / 20.0F) * 57.29578F, 0.0F, 1.0F, 0.0F);
        float scale = openState * 0.8F + 0.2F;
        GlStateManager.scale(scale, scale, scale);

        RenderHelper.enableStandardItemLighting();
        Minecraft.getMinecraft().getRenderItem().renderItem(
                stack, ItemCameraTransforms.TransformType.GROUND);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Unique
    private void easymagic$renderOrbitingLapis(
            ItemStack stack, double x, double y, double z,
            float time, int count) {
        float angleStep = 360.0F / count;
        for (int i = 0; i < count; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 0.5, y + 1.0, z + 0.5);
            GlStateManager.rotate(i * angleStep + time, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.75, 0.0, 0.25);
            GlStateManager.rotate(time % 360.0F, 0.0F, 1.0F, 0.0F);
            double bobY = 0.075 * Math.sin((time + i * 10.0) / 5.0);
            GlStateManager.translate(0.0, bobY, 0.0);

            RenderHelper.enableStandardItemLighting();
            Minecraft.getMinecraft().getRenderItem().renderItem(
                    stack, ItemCameraTransforms.TransformType.GROUND);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
    }
}
