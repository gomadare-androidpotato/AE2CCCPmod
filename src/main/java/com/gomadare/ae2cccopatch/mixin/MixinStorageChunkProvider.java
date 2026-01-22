package com.gomadare.ae2cccopatch.mixin;

import appeng.spatial.StorageChunkProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = StorageChunkProvider.class)
public abstract class MixinStorageChunkProvider {

    @Inject(
            method = "fillChunk",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onFillChunk(Chunk chunk, IBlockState defaultState, CallbackInfo ci) {
        // fillchunkというメソッドはstorageディメンションの作成に用いられているメソッドであり
        // storageディメンションをmatrixブロックという壁で満たすために存在しているのがfillchunkというメソッドだ
        // この処理は1チャンクにY座標が0~256まで存在していることが前提となっているためcubicchunksの16×16×16というチャンク方式に対応
        // あと多分少し処理速くなってるかも知れないしなってないかもしれない 分からん
            ExtendedBlockStorage[] storageArrays = chunk.getBlockStorageArray();
            for (int ySection = 0; ySection < 16; ySection++) {
                ExtendedBlockStorage storage = new ExtendedBlockStorage(ySection << 4, true);
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < 16; y++) {
                            storage.set(x, y, z, defaultState);
                        }
                    }
                }
                storageArrays[ySection] = storage;
            }


        chunk.markDirty();
        ci.cancel();
    }
}