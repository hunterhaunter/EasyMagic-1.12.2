package com.xy.easymagic.client;

import com.xy.easymagic.config.EasyMagicConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonReroll extends GuiButton {

    private static final ResourceLocation REROLL_TEXTURE =
        new ResourceLocation("easymagic", "textures/gui/enchanting_table_reroll.png");

    private static final int BTN_WIDTH = 38;
    private static final int BTN_HEIGHT = 27;
    private static final int DISABLED_V = 0;
    private static final int NORMAL_V = 27;
    private static final int HOVERED_V = 54;

    private static final int ICON_U = 64;
    private static final int ICON_SIZE = 15;
    private static final int ICON_DISABLED_V = 0;
    private static final int ICON_NORMAL_V = 15;
    private static final int ICON_HOVERED_V = 30;

    private int lapisAvailable;
    private int xpAvailable;
    private boolean itemPresent;
    private boolean itemEnchanted;

    public GuiButtonReroll(int buttonId, int x, int y) {
        super(buttonId, x, y, BTN_WIDTH, BTN_HEIGHT, "");
    }

    public void setLapisAvailable(int count) {
        this.lapisAvailable = count;
    }

    public void setXpAvailable(int xp) {
        this.xpAvailable = xp;
    }

    public void setItemPresent(boolean present) {
        this.itemPresent = present;
    }

    public void setItemEnchanted(boolean enchanted) {
        this.itemEnchanted = enchanted;
    }

    public void updateEnabledState(boolean isCreative) {
        boolean unusable = !this.itemPresent || this.itemEnchanted;
        if (unusable) {
            this.enabled = false;
            return;
        }
        if (isCreative) {
            this.enabled = true;
            return;
        }
        int lapisCost = EasyMagicConfig.rerollLapisCost;
        int xpCost = EasyMagicConfig.rerollXpCost;
        boolean cantAffordLapis = lapisCost > 0 && this.lapisAvailable < lapisCost;
        boolean cantAffordXp = xpCost > 0 && this.xpAvailable < xpCost;
        this.enabled = !cantAffordLapis && !cantAffordXp;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        int xpCost = EasyMagicConfig.rerollXpCost;
        int lapisCost = EasyMagicConfig.rerollLapisCost;
        boolean isCreative = mc.player.capabilities.isCreativeMode;

        boolean unusable = !this.itemPresent || this.itemEnchanted;

        boolean cantAffordLapis = lapisCost > 0 && this.lapisAvailable < lapisCost;
        boolean cantAffordXp = false;
        if (!isCreative) {
            if (xpCost > 0 && mc.player.experienceTotal < xpCost) {
                cantAffordXp = true;
            }
        }
        boolean cantAfford = cantAffordLapis || cantAffordXp;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 200);

        mc.getTextureManager().bindTexture(REROLL_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.hovered = mouseX >= this.x && mouseY >= this.y
            && mouseX < this.x + this.width && mouseY < this.y + this.height;

        int bgV;
        int iconV;
        if (!this.enabled) {
            bgV = DISABLED_V;
            iconV = ICON_DISABLED_V;
        } else if (this.hovered) {
            bgV = HOVERED_V;
            iconV = ICON_HOVERED_V;
        } else {
            bgV = NORMAL_V;
            iconV = ICON_NORMAL_V;
        }

        this.drawTexturedModalRect(this.x, this.y, 0, bgV, this.width, this.height);

        boolean hasCosts = xpCost > 0 || lapisCost > 0;
        int iconX = hasCosts ? this.x + 3 : this.x + 12;
        this.drawTexturedModalRect(iconX, this.y + 6, ICON_U, iconV, ICON_SIZE, ICON_SIZE);

        if (xpCost > 0 && lapisCost > 0) {
            int orbXOffset = lapisCost > 9 ? 17 : 20;
            renderCostOrb(mc, this.x + orbXOffset, this.y + 1, 51, lapisCost,
                cantAffordLapis, 0x5555FF);
            orbXOffset = xpCost > 9 ? 17 : 20;
            renderCostOrb(mc, this.x + orbXOffset, this.y + 13, 38, xpCost,
                cantAffordXp, 0x55FF55);
        } else if (lapisCost > 0) {
            int orbXOffset = lapisCost > 9 ? 17 : 20;
            renderCostOrb(mc, this.x + orbXOffset, this.y + 7, 51, lapisCost,
                cantAffordLapis, 0x5555FF);
        } else if (xpCost > 0) {
            int orbXOffset = xpCost > 9 ? 17 : 20;
            renderCostOrb(mc, this.x + orbXOffset, this.y + 7, 38, xpCost,
                cantAffordXp, 0x55FF55);
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderCostOrb(Minecraft mc, int orbX, int orbY, int texU, int cost, boolean disabled, int color) {
        mc.getTextureManager().bindTexture(REROLL_TEXTURE);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int texV = (disabled ? 39 : 0) + Math.min(2, cost / 5) * 13;
        this.drawTexturedModalRect(orbX, orbY, texU, texV, 13, 13);

        String text = String.valueOf(cost);
        int textColor = disabled ? 0xFF5555 : color;
        int textX = orbX + 8;
        int textY = orbY + 3;
        mc.fontRenderer.drawString(text, textX - 1, textY, 0x000000);
        mc.fontRenderer.drawString(text, textX + 1, textY, 0x000000);
        mc.fontRenderer.drawString(text, textX, textY - 1, 0x000000);
        mc.fontRenderer.drawString(text, textX, textY + 1, 0x000000);
        mc.fontRenderer.drawString(text, textX, textY, textColor);

        mc.getTextureManager().bindTexture(REROLL_TEXTURE);
    }
}
