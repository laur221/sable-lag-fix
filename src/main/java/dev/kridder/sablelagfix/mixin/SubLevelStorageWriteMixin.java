package dev.kridder.sablelagfix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kridder.sablelagfix.SableLagFixConfig;
import dev.kridder.sablelagfix.SaveDigest;
import dev.kridder.sablelagfix.Stats;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

@Mixin(value = SubLevelStorageFile.class, remap = false)
public abstract class SubLevelStorageWriteMixin {
    @Shadow
    @Final
    private IntBuffer sectorSpans;

    @Shadow
    @Final
    private Path externalFileDir;

    @Unique
    private final Int2ObjectOpenHashMap<SaveDigest> sablelagfix$lastWritten = new Int2ObjectOpenHashMap<>();

    @Unique
    private SaveDigest sablelagfix$pending;

    @Unique
    private boolean sablelagfix$noExternalFiles;

    @Inject(method = "<init>(Ljava/nio/file/Path;Ljava/nio/file/Path;I)V", at = @At("RETURN"), require = 0, remap = false)
    private void sablelagfix$init(CallbackInfo ci) {
        boolean none;
        try {
            if (!Files.isDirectory(this.externalFileDir)) {
                none = true;
            } else {
                try (Stream<Path> s = Files.list(this.externalFileDir)) {
                    none = s.findAny().isEmpty();
                }
            }
        } catch (IOException | RuntimeException e) {
            none = false;
        }
        this.sablelagfix$noExternalFiles = none;
    }

    @Inject(method = "write(ILnet/minecraft/nbt/CompoundTag;)V", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void sablelagfix$skipUnchanged(int index, CompoundTag tag, CallbackInfo ci) {
        this.sablelagfix$pending = null;
        if (tag == null || !SableLagFixConfig.skipUnchangedWrites()) {
            this.sablelagfix$lastWritten.remove(index);
            return;
        }
        SaveDigest digest;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(baos));
            byte[] bytes = baos.toByteArray();
            digest = new SaveDigest(bytes.length, MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception e) {
            this.sablelagfix$lastWritten.remove(index);
            return;
        }
        SaveDigest last = this.sablelagfix$lastWritten.get(index);
        if (last != null && last.length == digest.length
                && MessageDigest.isEqual(last.sha256, digest.sha256)
                && this.sectorSpans.get(index) != 0) {
            Stats.skippedWrites.incrementAndGet();
            ci.cancel();
            return;
        }
        this.sablelagfix$lastWritten.remove(index);
        this.sablelagfix$pending = digest;
    }

    @Inject(method = "write(ILnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"), require = 0, remap = false)
    private void sablelagfix$remember(int index, CompoundTag tag, CallbackInfo ci) {
        if (this.sablelagfix$pending != null) {
            this.sablelagfix$lastWritten.put(index, this.sablelagfix$pending);
            this.sablelagfix$pending = null;
        }
    }

    @WrapOperation(method = "write(ILnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtIo;writeCompressed(Lnet/minecraft/nbt/CompoundTag;Ljava/io/OutputStream;)V"), require = 0, remap = false)
    private void sablelagfix$fastCompress(CompoundTag tag, OutputStream out, Operation<Void> op) throws IOException {
        if (!SableLagFixConfig.fastCompression()) {
            op.call(tag, out);
            return;
        }
        try (DataOutputStream d = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(out) {
            {
                this.def.setLevel(Deflater.BEST_SPEED);
            }
        }))) {
            NbtIo.write(tag, (java.io.DataOutput) d);
        }
    }

    @Inject(method = "clear(I)V", at = @At("HEAD"), require = 0, remap = false)
    private void sablelagfix$forget(int index, CallbackInfo ci) {
        this.sablelagfix$lastWritten.remove(index);
    }

    @Inject(method = "writeToExternalFile(Ljava/nio/ByteBuffer;)Ljava/nio/file/Path;", at = @At("HEAD"), require = 0, remap = false)
    private void sablelagfix$externalUsed(ByteBuffer buffer, CallbackInfoReturnable<Path> cir) {
        this.sablelagfix$noExternalFiles = false;
    }

    @WrapOperation(method = "write(ILjava/nio/ByteBuffer;)V", at = @At(value = "INVOKE", target = "Ljava/nio/file/Files;deleteIfExists(Ljava/nio/file/Path;)Z"), require = 0, remap = false)
    private boolean sablelagfix$skipDelete(Path path, Operation<Boolean> op) {
        if (this.sablelagfix$noExternalFiles && SableLagFixConfig.skipRedundantExternalDelete()) {
            Stats.skippedDeletes.incrementAndGet();
            return false;
        }
        return op.call(path);
    }
}