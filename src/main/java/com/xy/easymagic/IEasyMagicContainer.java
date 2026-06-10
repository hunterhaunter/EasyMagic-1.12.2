package com.xy.easymagic;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IEasyMagicContainer {

    BlockPos easymagic$getPosition();

    boolean easymagic$canReroll(boolean isCreative);

    void easymagic$performReroll(EntityPlayerMP player, int newSeed, boolean isCreative);

    ItemStack easymagic$getTableItem();

    ItemStack easymagic$getLapisItem();
}
