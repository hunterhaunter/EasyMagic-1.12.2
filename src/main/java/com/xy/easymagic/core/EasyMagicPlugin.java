package com.xy.easymagic.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class EasyMagicPlugin implements IFMLLoadingPlugin, IEarlyMixinLoader {

    // Do NOT call MixinBootstrap.init() / Mixins.addConfiguration() here.
    // MixinBooter owns Mixin (loaded in the LaunchClassLoader); touching it from
    // this coremod constructor either runs too early ("you didn't call
    // MixinBootstrap.init()") or causes a loader-constraint LinkageError on
    // GlobalProperties$Keys. Instead implement IEarlyMixinLoader and let
    // MixinBooter queue the config with its own Mixin instance at the right time.

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.easymagic.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
