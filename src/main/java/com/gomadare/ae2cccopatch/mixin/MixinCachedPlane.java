package com.gomadare.ae2cccopatch.mixin;

import appeng.core.worlddata.WorldData;
import appeng.spatial.CachedPlane;
import appeng.util.Platform;
import com.gomadare.ae2cccopatch.mixin.accessor.CachedPlaneAccessor;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        value = CachedPlane.class,
        remap = false
)
public abstract class MixinCachedPlane {

    @Shadow private World world;
    @Shadow private int y_offset;
    @Shadow private int y_size;

    @Final
    @Shadow
    private int cx_size;
    @Final
    @Shadow
    private int cz_size;
    @Final
    @Shadow
    private Chunk[][] myChunks;
    @Shadow
    abstract World getWorld();
    @Inject(method = "updateChunks", at = @At("HEAD"), cancellable = true)
    private void onUpdateChunks(CallbackInfo ci) {
        //手動で記載あるいはこのmixinを精査済み 要検証
        //ただ単にVerticalBitsに-1を代入しているだけのコード

        //ModifyVariableでよりスマートに代用できる可能性あり 要検証
        //↑やっぱローカル変数が対象だからthis.で呼び出されてるし無理だと思う

        // update shit..
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {
                final Chunk c = this.myChunks[x][z];
                c.resetRelightChecks();
                c.generateSkylightMap();
                c.setModified(true);
            }
        }

        // send shit...
        int verticalBits = -1; //全ビットを立てる
        for (int x = 0; x < this.cx_size; x++) {
            for (int z = 0; z < this.cz_size; z++) {

                final Chunk c = this.myChunks[x][z];

                for (int y = this.y_offset; y < (this.y_offset + this.y_size); y += 32) {
                    WorldData.instance().compassData().service().updateArea(this.getWorld(), c.x << 4, y, c.z << 4);
                }

                Platform.sendChunk(c, verticalBits);
            }
        }
        ci.cancel();
    }
}