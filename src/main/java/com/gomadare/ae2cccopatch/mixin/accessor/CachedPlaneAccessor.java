package com.gomadare.ae2cccopatch.mixin.accessor;

import appeng.api.movable.IMovableRegistry;
import appeng.spatial.CachedPlane;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CachedPlane.class, remap = false)
public interface CachedPlaneAccessor {
    @Accessor("matrixBlockState")
    IBlockState getMatrixBlockState();

    @Accessor("reg")
    IMovableRegistry getReg();

    // CachedPlaneAccessor.java
    @Accessor("verticalBits")
    void setVerticalBits(int bits);
}