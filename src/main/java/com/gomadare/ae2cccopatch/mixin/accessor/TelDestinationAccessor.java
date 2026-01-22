package com.gomadare.ae2cccopatch.mixin.accessor;

import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

// ターゲットは内部クラスなので$で指定(今更)
@Mixin(targets = "appeng.spatial.StorageHelper$TelDestination", remap = false)
public interface TelDestinationAccessor {

    // AE2の内部クラスにある double x, y, z と World dim を取得するgetter
    @Accessor("x") double getX();
    @Accessor("y") double getY();
    @Accessor("z") double getZ();
    @Accessor("dim") World getDim();
}