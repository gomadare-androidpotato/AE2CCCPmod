package com.gomadare.ae2cccopatch.mixin.accessor;

import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.spatial.CachedPlane$BlockStorageData", remap = false)
public interface BlockStorageDataAccessor {
    @Accessor("state")
    IBlockState getState();

    @Accessor("state")
    void setState(IBlockState state);

    @Accessor("light")
    void setLight(int light);
}