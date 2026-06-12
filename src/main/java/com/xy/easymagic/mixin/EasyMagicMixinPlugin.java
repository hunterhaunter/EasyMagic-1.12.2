package com.xy.easymagic.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class EasyMagicMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LogManager.getLogger("easymagic-mixin");

    private static final boolean REAGENCHANT_LOADED =
            isClassPresent("logictechcorp.reagenchant.Reagenchant");

    // Checked lazily: shouldApplyMixin runs when the target class first loads,
    // by which point all mod jars are on the classpath.
    private static Boolean apotheosisLoaded;

    private static boolean isApotheosisLoaded() {
        if (apotheosisLoaded == null) {
            apotheosisLoaded = isClassPresent("shadows.Apotheosis")
                    || isClassPresent("shadows.ApotheosisCore");
            if (apotheosisLoaded) {
                LOGGER.info("Apotheosis detected - disabling EasyMagic enchantment hints "
                        + "(Apotheosis rewrites ContainerEnchantment#onCraftMatrixChanged)");
            }
        }
        return apotheosisLoaded;
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, EasyMagicMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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
            return !isClassPresent("enchantmentcontrol.core.EnchantmentControlPlugin");
        }

        if (mixinClassName.endsWith("MixinContainerEnchantmentHints")
                || mixinClassName.endsWith("MixinGuiEnchantmentHints")) {
            return !isApotheosisLoaded();
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
