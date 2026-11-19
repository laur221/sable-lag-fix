package dev.kridder.sablelagfix.mixin;

import dev.kridder.sablelagfix.Stats;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SubLevelHoldingChunkMap.class, remap = false)
public abstract class SubLevelHoldingChunkMapMixin {
    @Unique
    private long sablelagfix$saveStart;

    @Inject(method = "saveAll", at = @At("HEAD"), require = 0, remap = false)
    private void sablelagfix$saveHead(CallbackInfo ci) {
        this.sablelagfix$saveStart = System.nanoTime();
    }

    @Inject(method = "saveAll", at = @At("RETURN"), require = 0, remap = false)
    private void sablelagfix$saveTail(CallbackInfo ci) {
        long d = System.nanoTime() - this.sablelagfix$saveStart;
        Stats.lastSaveNanos = d;
        Stats.saveNanos.addAndGet(d);
        Stats.saveCalls.incrementAndGet();
    }
}