package com.gomadare.ae2cccopatch.mixin;

import appeng.spatial.CachedPlane;
import com.gomadare.ae2cccopatch.mixin.accessor.BlockStorageDataAccessor;
import com.gomadare.ae2cccopatch.mixin.accessor.CachedPlaneAccessor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

//地形及びEntityの転送、cubicchunks環境下でのクラッシュ全てを修正する

@Mixin(targets = "appeng.spatial.CachedPlane$Column", remap = false)
public abstract class MixinCachedPlaneColumn {

    @Mutable
    @Shadow @Final private int x;
    @Mutable
    @Shadow @Final private int z;
    @Mutable
    @Shadow @Final private Chunk c;
    @Shadow(aliases = "this$0") @Final private CachedPlane field_this$0;

    @Shadow private List<Integer> skipThese;

    /**
     * setBlockIDWithMetadata: ブロックの書き込み
     * 元の storage[y >> 4].set(...) を完全にバイパスします
     */
    @Inject(
            method = "setBlockIDWithMetadata(ILappeng/spatial/CachedPlane$BlockStorageData;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetBlockIDWithMetadata(int y, @Coerce Object dataObj, CallbackInfo ci) {
        //手動でこのmixinを精査済み 完璧に動作するはずです いかなる状況でも消す必要はありません
        //この修正によりブロックの書き込みがcubicchunks環境下でも成功します

        // Accessorを使用
        BlockStorageDataAccessor data = (BlockStorageDataAccessor) dataObj;
        CachedPlaneAccessor parent = (CachedPlaneAccessor) this.field_this$0;

        // Accessorを用いて数値をゲット
        IBlockState state = data.getState();
        IBlockState matrixState = parent.getMatrixBlockState();

        // もしmatrixBlock(StorageDimension内の見えない壁)ならAIR(空気つまり無)に入れ替える
        if (state == matrixState) {
            state = net.minecraft.init.Blocks.AIR.getDefaultState();
        }
        // getBlockStorageArray()を用いて配列を取得しそこに.set()していたのがcubicchunksと相性が悪かったのが原因なので
        // getWorld及びsetBlockState()を用いる事で配列を利用せずアクセス及びセットしている
        // 負の配列などが無いというのもあるのでこれは重要である
        BlockPos pos = new BlockPos(this.x + (this.c.x << 4), y, this.z + (this.c.z << 4));
        this.c.getWorld().setBlockState(pos, state, 2);

        ci.cancel();
    }

    /**
     * fillData: ブロック情報の読み取り
     * 元の storage[y >> 4].get(...) を完全にバイパスします
     */
    @Inject(method = "fillData(ILappeng/spatial/CachedPlane$BlockStorageData;)V", at = @At("HEAD"), cancellable = true)
    private void onFillData(int y, @Coerce Object dataObj, CallbackInfo ci) {
        //この修正によりブロックの読み込みがcubicchunks環境下でも成功します

        //onFillDataの第二引数であるdataObjをBlockStorageDataAccessorに渡してdataというフィールドとして貰う
        BlockStorageDataAccessor data = (BlockStorageDataAccessor) dataObj;

        BlockPos pos = new BlockPos(this.x + (this.c.x << 4), y, this.z + (this.c.z << 4));
        IBlockState state = this.c.getWorld().getBlockState(pos);

        //Accessorを用いてセット
        data.setState(state);
        data.setLight(15);

        ci.cancel();
    }

    /**
     * doNotSkip: ブラックリスト判定
     * storage配列を使わず判定します
     */
    @Inject(method = "doNotSkip(I)Z", at = @At("HEAD"), cancellable = true)
    private void onDoNotSkip(int y, CallbackInfoReturnable<Boolean> cir) {
        // 親のAccessorを取得
        CachedPlaneAccessor parent = (CachedPlaneAccessor) this.field_this$0;

        // 座標と状態を取得
        BlockPos pos = new BlockPos(this.x + (this.c.x << 4), y, this.z + (this.c.z << 4));
        IBlockState state = this.c.getWorld().getBlockState(pos);

        // リフレクションなしで reg にアクセス
        if (parent.getReg().isBlacklisted(state.getBlock())) {
            cir.setReturnValue(false);
        } else {
            cir.setReturnValue(this.skipThese == null || !this.skipThese.contains(y));
        }

        // 本来の処理をキャンセル
        cir.cancel();
    }
    //検証済みmixin
    //元のstorage[]配列を上記の修正により使わずに処理できるようになったので
    //cachedplane$columnのコンストラクタでの処理を、無効化し、負の数値の配列を参照しようとして発生するエラー及びクラッシュを防ぐ
    //カウンターは0から始まり1回のforに付きインクリメントされているのだが
    //カウンター < chunkHeightとなっているので
    //0 < 0はfalseとなり1度も実行されない マイナスでも恐らく同様
        @ModifyVariable(
                method = "<init>(Lappeng/spatial/CachedPlane;Lnet/minecraft/world/chunk/Chunk;IIII)V",
                at = @At("HEAD"),
                ordinal = 3, // 4番目のint引数 (chunkHeight) HEADの場合引数だけがカウント対象
                argsOnly = true
        )
        private static int chunkHeight(int value) {
            return 0;
        }
}