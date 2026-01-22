package com.gomadare.ae2cccopatch.mixin;

import appeng.spatial.StorageHelper;
import com.gomadare.ae2cccopatch.mixin.accessor.TelDestinationAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = StorageHelper.class, remap = false)
//手動でmixinの有効性を閲覧済み 要検証だが今のところ上手く動作していると思われる
//多分必要なチャンクをロードするというコード
public abstract class MixinStorageHelper {

    @Inject(
            method = "teleportEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;changeDimension(ILnet/minecraftforge/common/util/ITeleporter;)Lnet/minecraft/entity/Entity;"
            ),
            remap = false
    )
    private void injectCubicLoad(Entity entity, @Coerce Object link, CallbackInfoReturnable<Entity> cir) {
        // Accessorを用いています
        TelDestinationAccessor acc = (TelDestinationAccessor) link;
        World newWorld = ((TelDestinationAccessor) link).getDim(); // TelDestination内のフィールド
        if (newWorld != null) {
            // 柱のロード
            newWorld.getChunkProvider().provideChunk(
                    MathHelper.floor(((TelDestinationAccessor) link).getX()) >> 4,
                    MathHelper.floor(((TelDestinationAccessor) link).getZ()) >> 4
            );

            // BlockState要求によるCロード
            newWorld.getBlockState(new BlockPos(((TelDestinationAccessor) link).getX(), ((TelDestinationAccessor) link).getY(), ((TelDestinationAccessor) link).getZ()));
        }
    }

}
