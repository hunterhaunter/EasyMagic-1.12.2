package com.xy.easymagic.mixin;

import com.google.common.collect.Lists;
import com.xy.easymagic.LapisUtil;
import com.xy.easymagic.client.GuiButtonReroll;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageReroll;
import com.xy.easymagic.network.PacketHandler;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiEnchantment.class)
public abstract class MixinGuiEnchantment extends GuiContainer {

    @Shadow
    @Final
    private ContainerEnchantment container;

    @Unique
    private GuiButtonReroll easymagic$rerollButton;

    @Unique
    private boolean easymagic$buttonInitialized;

    protected MixinGuiEnchantment(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Unique
    private void easymagic$ensureButtonInit() {
        if (easymagic$buttonInitialized) return;
        easymagic$buttonInitialized = true;
        if (!EasyMagicConfig.rerollEnabled) return;
        this.easymagic$rerollButton = new GuiButtonReroll(100,
            this.guiLeft + EasyMagicConfig.rerollButtonOffsetX,
            this.guiTop + EasyMagicConfig.rerollButtonOffsetY);
        this.buttonList.add(this.easymagic$rerollButton);
    }

    @Inject(method = "drawGuiContainerBackgroundLayer", at = @At("RETURN"))
    private void easymagic$updateRerollButton(float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
        easymagic$ensureButtonInit();
        if (this.easymagic$rerollButton == null) return;
        this.easymagic$rerollButton.x = this.guiLeft + EasyMagicConfig.rerollButtonOffsetX;
        this.easymagic$rerollButton.y = this.guiTop + EasyMagicConfig.rerollButtonOffsetY;
        this.easymagic$rerollButton.setLapisAvailable(
            LapisUtil.getLapisCount(this.container.tableInventory.getStackInSlot(1)));
        this.easymagic$rerollButton.setXpAvailable(this.mc.player.experienceTotal);
        ItemStack tableItem = this.container.tableInventory.getStackInSlot(0);
        this.easymagic$rerollButton.setItemPresent(!tableItem.isEmpty());
        this.easymagic$rerollButton.setItemEnchanted(!tableItem.isEmpty() && tableItem.isItemEnchanted());
        this.easymagic$rerollButton.updateEnabledState(this.mc.player.capabilities.isCreativeMode);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void easymagic$handleRerollClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (this.easymagic$rerollButton == null) return;
        if (mouseButton != 0) return;
        if (this.easymagic$rerollButton.mousePressed(this.mc, mouseX, mouseY)) {
            this.easymagic$rerollButton.playPressSound(this.mc.getSoundHandler());
            PacketHandler.INSTANCE.sendToServer(new MessageReroll());
            ci.cancel();
        }
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void easymagic$drawRerollTooltip(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!EasyMagicConfig.rerollEnabled) return;
        if (this.easymagic$rerollButton == null || !this.easymagic$rerollButton.isMouseOver()) return;

        List<String> tooltip = Lists.newArrayList();
        tooltip.add(TextFormatting.GRAY + I18n.format("easymagic.reroll.tooltip"));

        if (!this.mc.player.capabilities.isCreativeMode) {
            List<String> costLines = Lists.newArrayList();

            int lapisCost = EasyMagicConfig.rerollLapisCost;
            if (lapisCost >= 1) {
                int lapisAvail = LapisUtil.getLapisCount(this.container.tableInventory.getStackInSlot(1));
                TextFormatting color = lapisAvail >= lapisCost ? TextFormatting.GRAY : TextFormatting.RED;
                String text = lapisCost == 1
                        ? I18n.format("easymagic.reroll.lapis.one")
                        : I18n.format("easymagic.reroll.lapis.many", lapisCost);
                costLines.add(color + text);
            }

            int xpCost = EasyMagicConfig.rerollXpCost;
            if (xpCost >= 1) {
                int xpAvail = this.mc.player.experienceTotal;
                TextFormatting color = xpAvail >= xpCost ? TextFormatting.GRAY : TextFormatting.RED;
                String text = xpCost == 1
                        ? I18n.format("easymagic.reroll.xp.one")
                        : I18n.format("easymagic.reroll.xp.many", xpCost);
                costLines.add(color + text);
            }

            if (!costLines.isEmpty()) {
                tooltip.add("");
                tooltip.addAll(costLines);
            }
        }

        this.drawHoveringText(tooltip, mouseX, mouseY);
    }
}
