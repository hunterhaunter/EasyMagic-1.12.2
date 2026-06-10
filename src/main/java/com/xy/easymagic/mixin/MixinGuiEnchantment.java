package com.xy.easymagic.mixin;

import com.google.common.collect.Lists;
import com.xy.easymagic.client.GuiButtonReroll;
import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageReroll;
import com.xy.easymagic.network.PacketHandler;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Items;
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
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

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
        this.easymagic$rerollButton.setLapisAvailable(this.container.getLapisAmount());
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

    @Redirect(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiEnchantment;drawHoveringText(Ljava/util/List;II)V"
        )
    )
    private void easymagic$enhanceEnchantTooltip(GuiEnchantment self, List<String> list, int mouseX, int mouseY) {
        if (EasyMagicConfig.showEnchantmentHints && EasyMagicConfig.enchantmentHintCount != 1) {
            for (int slot = 0; slot < 3; slot++) {
                if (this.isPointInRegion(60, 14 + 19 * slot, 108, 17, mouseX, mouseY)
                        && this.container.enchantLevels[slot] > 0) {
                    List<EnchantmentData> fullList = easymagic$computeEnchantList(slot);
                    int hintCount = EasyMagicConfig.enchantmentHintCount;
                    int show = hintCount < 0 ? fullList.size() : Math.min(hintCount, fullList.size());
                    if (show > 0 && !fullList.isEmpty()) {
                        list.set(0, "" + TextFormatting.WHITE + TextFormatting.ITALIC
                                + fullList.get(0).enchantment.getTranslatedName(fullList.get(0).enchantmentLevel));
                        for (int i = 1; i < show; i++) {
                            EnchantmentData ed = fullList.get(i);
                            list.add(i, "" + TextFormatting.WHITE + TextFormatting.ITALIC
                                    + ed.enchantment.getTranslatedName(ed.enchantmentLevel));
                        }
                    }
                    break;
                }
            }
        }
        self.drawHoveringText(list, mouseX, mouseY);
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
                int lapisAvail = this.container.getLapisAmount();
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

    @Unique
    private List<EnchantmentData> easymagic$computeEnchantList(int slot) {
        ItemStack stack = this.container.tableInventory.getStackInSlot(0);
        int level = this.container.enchantLevels[slot];
        if (stack.isEmpty() || level <= 0) return java.util.Collections.emptyList();
        Random rand = new Random((long) (this.container.xpSeed + slot));
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(rand, stack, level, false);
        if (list == null) return java.util.Collections.emptyList();
        if (stack.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(rand.nextInt(list.size()));
        }
        return list;
    }
}
