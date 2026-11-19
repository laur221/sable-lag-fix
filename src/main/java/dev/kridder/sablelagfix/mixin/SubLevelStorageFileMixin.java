package dev.kridder.sablelagfix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kridder.sablelagfix.SableLagFixConfig;
import dev.kridder.sablelagfix.Stats;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

@Mixin(value = SubLevelStorageFile.class, remap = false)
public abstract class SubLevelStorageFileMixin {
    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/channels/FileChannel;open(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/nio/channels/FileChannel;"
            ),
            require = 0,
            remap = false
    )
    private FileChannel sablelagfix$noDsync(Path path, OpenOption[] options, Operation<FileChannel> original) throws IOException {
        if (SableLagFixConfig.noPerWriteDiskSync()) {
            OpenOption[] filtered = Arrays.stream(options)
                    .filter(o -> o != StandardOpenOption.DSYNC)
                    .toArray(OpenOption[]::new);
            if (filtered.length != options.length) {
                Stats.strippedDsync.incrementAndGet();
            }
            return original.call(path, filtered);
        }
        return original.call(path, options);
    }
}