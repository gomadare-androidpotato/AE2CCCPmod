package com.gomadare.ae2cccopatch;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import zone.rong.mixinbooter.IEarlyMixinLoader;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

// このアノテーションはCoremodとしての宣言
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("AE2 Cubic Chunks Patch")
public class MixinLoader implements IFMLLoadingPlugin, IEarlyMixinLoader {

    // --- IEarlyMixinLoader の実装 (MixinBooter への指示) ---
    @Override
    public List<String> getMixinConfigs() {
        // resources 直下にある mixin 設定ファイル名を指定
        return Collections.singletonList("mixins.ae2cccopatch.json");
    }

    // --- IFMLLoadingPlugin の実装 (Coremod として認識されるために空のまま必要) ---
    @Override
    public String[] getASMTransformerClass() { return new String[0]; }

    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() { return null; }
}