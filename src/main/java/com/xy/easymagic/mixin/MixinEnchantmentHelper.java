package com.xy.easymagic.mixin;

import com.xy.easymagic.config.EasyMagicConfig;

import net.minecraft.enchantment.EnchantmentHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelper {

    @ModifyConstant(method = "calcItemStackEnchantability", constant = @Constant(intValue = 15))
    private static int easymagic$modifyMaxBookshelfPower(int originalValue) {
        return EasyMagicConfig.maxBookshelfCount;
    }
}
