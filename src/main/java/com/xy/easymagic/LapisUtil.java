package com.xy.easymagic;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Lapis validation for the reroll cost. Some mods relax the enchanting table's
 * lapis slot to accept other catalysts (e.g. prismarine crystals), so counting
 * the raw slot-1 stack size is not enough — the item itself must match the
 * same "gemLapis" ore dictionary check Forge patches into the vanilla slot.
 */
public final class LapisUtil {

    private LapisUtil() {
    }

    public static boolean isLapis(ItemStack stack) {
        return !stack.isEmpty()
            && OreDictionary.containsMatch(false, OreDictionary.getOres("gemLapis"), stack);
    }

    public static int getLapisCount(ItemStack stack) {
        return isLapis(stack) ? stack.getCount() : 0;
    }
}
