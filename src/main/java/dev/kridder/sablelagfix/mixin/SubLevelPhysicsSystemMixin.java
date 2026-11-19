package dev.kridder.sablelagfix.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kridder.sablelagfix.SableLagFixConfig;
import dev.kridder.sablelagfix.Stats;
import dev.ryanhcode.sable.api.physics.PhysicsPipeline;
import dev.ryanhcode.sable.api.physics.object.ArbitraryPhysicsObject;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(value = SubLevelPhysicsSystem.class, remap = false)
public abstract class SubLevelPhysicsSystemMixin {
    @Shadow
    @Final
    private Collection<ArbitraryPhysicsObject> arbitraryObjects;

    @WrapOperation(
            method = "tickPipelinePhysics(Ldev/ryanhcode/sable/api/sublevel/ServerSubLevelContainer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Ldev/ryanhcode/sable/api/physics/PhysicsPipeline;physicsTick(D)V",
                    remap = false
            ),
            require = 0,
            remap = false
    )
    private void sablelagfix$skipIdle(PhysicsPipeline pipeline, double timeStep, Operation<Void> original,
                                      @Local(argsOnly = true) ServerSubLevelContainer container) {
        if (SableLagFixConfig.skipIdlePhysics()
                && container.getAllSubLevels().isEmpty()
                && this.arbitraryObjects.isEmpty()) {
            Stats.skippedPhysicsTicks.incrementAndGet();
            return;
        }
        long t0 = System.nanoTime();
        original.call(pipeline, timeStep);
        Stats.physicsNanos.addAndGet(System.nanoTime() - t0);
        Stats.physicsCalls.incrementAndGet();
    }
}