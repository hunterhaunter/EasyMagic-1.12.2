package com.xy.easymagic.mixin;

import com.xy.easymagic.config.EasyMagicConfig;
import com.xy.easymagic.network.MessageEnchantHints;

import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.inventory.GuiContainer;
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
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Client-side enchantment hint tooltip. Kept separate from MixinGuiEnchantment
 * so it can be disabled alongside the server hints mixin when Apotheosis is
 * present (its rewritten enchanting logic makes vanilla hint computation
 * wrong). See EasyMagicMixinPlugin.
 */
@Mixin(GuiEnchantment.class)
public abstract class MixinGuiEnchantmentHints extends GuiContainer {

    @Unique
    private static final Method easymagic$enchTableThreadLocalSet;

    static {
        Method m = null;
        try {
            Class<?> cls = Class.forName("enchantmentcontrol.util.FromEnchTableThreadLocal");
            m = cls.getMethod("set", boolean.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
        }
        easymagic$enchTableThreadLocalSet = m;
    }

    @Shadow
    @Final
    private ContainerEnchantment container;

    protected MixinGuiEnchantmentHints(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    // require = 0: if another mod transforms drawScreen and this invoke no
    // longer matches, lose the hint tooltip instead of failing the whole mixin
    // (a hard failure here prevents the enchanting GUI from opening at all).
    @Redirect(
        method = "drawScreen",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiEnchantment;drawHoveringText(Ljava/util/List;II)V"
        ),
        require = 0
    )
    private void easymagic$enhanceEnchantTooltip(GuiEnchantment self, List<String> list, int mouseX, int mouseY) {
        if (EasyMagicConfig.showEnchantmentHints && EasyMagicConfig.enchantmentHintCount != 1) {
            for (int slot = 0; slot < 3; slot++) {
                if (this.isPointInRegion(60, 14 + 19 * slot, 108, 17, mouseX, mouseY)
                        && this.container.enchantLevels[slot] > 0) {
                    List<EnchantmentData> fullList = MessageEnchantHints.getHints(this.container.windowId, slot);
                    if (fullList == null) {
                        fullList = easymagic$computeEnchantList(slot);
                    }
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

    @Unique
    private List<EnchantmentData> easymagic$computeEnchantList(int slot) {
        ItemStack stack = this.container.tableInventory.getStackInSlot(0);
        int level = this.container.enchantLevels[slot];
        if (stack.isEmpty() || level <= 0) return Collections.emptyList();
        easymagic$markEnchTableContext();
        Random rand = new Random((long) (this.container.xpSeed + slot));
        List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(rand, stack, level, false);
        if (list == null) return Collections.emptyList();
        if (stack.getItem() == Items.BOOK && list.size() > 1) {
            list.remove(rand.nextInt(list.size()));
        }
        return list;
    }

    @Unique
    private static void easymagic$markEnchTableContext() {
        if (easymagic$enchTableThreadLocalSet != null) {
            try {
                easymagic$enchTableThreadLocalSet.invoke(null, true);
            } catch (Exception ignored) {
            }
        }
    }
}
