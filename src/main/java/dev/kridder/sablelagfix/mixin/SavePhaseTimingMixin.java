package dev.kridder.sablelagfix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kridder.sablelagfix.Stats;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.holding.GlobalSavedSubLevelPointer;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunk;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelData;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelStorage;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.UUID;

@Mixin(value = SubLevelHoldingChunkMap.class, remap = false)
public abstract class SavePhaseTimingMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelSerializer;toData(Ldev/ryanhcode/sable/sublevel/ServerSubLevel;Ljava/util/List;)Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelData;"), require = 0, remap = false)
    private SubLevelData sablelagfix$timeSerialize(ServerSubLevel a, List<UUID> b, Operation<SubLevelData> op) {
        long t = System.nanoTime();
        try { return op.call(a, b); } finally { Stats.phase(0, System.nanoTime() - t); }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelStorage;attemptSaveSubLevel(Ldev/ryanhcode/sable/sublevel/storage/holding/GlobalSavedSubLevelPointer;Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelData;)V"), require = 0, remap = false)
    private void sablelagfix$timeWrite1(SubLevelStorage s, GlobalSavedSubLevelPointer p, SubLevelData d, Operation<Void> op) {
        long t = System.nanoTime();
        try { op.call(s, p, d); } finally { Stats.phase(1, System.nanoTime() - t); }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelStorage;attemptSaveSubLevel(Lnet/minecraft/world/level/ChunkPos;Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelData;)Ldev/ryanhcode/sable/sublevel/storage/holding/GlobalSavedSubLevelPointer;"), require = 0, remap = false)
    private GlobalSavedSubLevelPointer sablelagfix$timeWrite2(SubLevelStorage s, ChunkPos p, SubLevelData d, Operation<GlobalSavedSubLevelPointer> op) {
        long t = System.nanoTime();
        try { return op.call(s, p, d); } finally { Stats.phase(1, System.nanoTime() - t); }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelStorage;attemptSaveHoldingChunk(Lnet/minecraft/world/level/ChunkPos;Ldev/ryanhcode/sable/sublevel/storage/holding/SubLevelHoldingChunk;)V"), require = 0, remap = false)
    private void sablelagfix$timeHolding(SubLevelStorage s, ChunkPos p, SubLevelHoldingChunk c, Operation<Void> op) {
        long t = System.nanoTime();
        try { op.call(s, p, c); } finally { Stats.phase(2, System.nanoTime() - t); }
    }

    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/serialization/SubLevelStorage;flush()V"), require = 0, remap = false)
    private void sablelagfix$timeFlush(SubLevelStorage s, Operation<Void> op) {
        long t = System.nanoTime();
        try { op.call(s); } finally { Stats.phase(3, System.nanoTime() - t); }
    }

    @WrapOperation(method = "saveAll", at = @At(value = "INVOKE", target = "Ldev/ryanhcode/sable/sublevel/storage/holding/SubLevelHoldingChunkMap;processChanges()V"), require = 0, remap = false)
    private void sablelagfix$timeChanges(SubLevelHoldingChunkMap s, Operation<Void> op) {
        long t = System.nanoTime();
        try { op.call(s); } finally { Stats.phase(4, System.nanoTime() - t); }
    }
}