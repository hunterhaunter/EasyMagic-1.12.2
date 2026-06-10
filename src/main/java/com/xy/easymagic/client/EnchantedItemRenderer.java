package com.xy.easymagic.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public final class EnchantedItemRenderer {

    private EnchantedItemRenderer() {
    }

    public static void renderHoveringItem(ItemStack stack, double x, double y, double z,
            float time, float partialTicks, float bookSpread, float bookSpreadPrev) {
        float openState = bookSpreadPrev + (bookSpread - bookSpreadPrev) * partialTicks;
        if (openState <= 0.0F && bookSpreadPrev <= 0.0F) return;

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

    public static void renderOrbitingLapis(ItemStack stack, double x, double y, double z,
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
