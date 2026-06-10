package com.xy.easymagic.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class EasyMagicMixinPlugin implements IMixinConfigPlugin {

    private static final boolean REAGENCHANT_LOADED;

    static {
        boolean found;
        try {
            Class.forName("logictechcorp.reagenchant.Reagenchant", false,
                    EasyMagicMixinPlugin.class.getClassLoader());
            found = true;
        } catch (ClassNotFoundException e) {
            found = false;
        }
        REAGENCHANT_LOADED = found;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("MixinEnchantmentHelper")) {
            try {
                Class.forName("enchantmentcontrol.core.EnchantmentControlPlugin", false,
                        getClass().getClassLoader());
                return false;
            } catch (ClassNotFoundException e) {
                return true;
            }
        }

        if (mixinClassName.endsWith("MixinContainerReagentTable")
                || mixinClassName.endsWith("MixinGuiReagentTable")
                || mixinClassName.endsWith("MixinTESRReagentTable")) {
            return REAGENCHANT_LOADED;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
