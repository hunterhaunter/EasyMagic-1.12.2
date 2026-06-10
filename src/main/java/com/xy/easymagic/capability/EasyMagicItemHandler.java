package com.xy.easymagic.capability;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

public class EasyMagicItemHandler extends ItemStackHandler {

    public EasyMagicItemHandler() {
        super(2);
    }

    @Override
    public int getSlotLimit(int slot) {
        return slot == 0 ? 1 : 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (slot == 1) {
            return stack.getItem() == Items.DYE && stack.getMetadata() == 4;
        }
        return true;
    }
}
