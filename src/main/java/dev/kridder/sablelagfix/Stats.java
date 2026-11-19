package dev.kridder.sablelagfix;

import java.util.concurrent.atomic.AtomicLong;

public final class Stats {
    public static final AtomicLong skippedPhysicsTicks = new AtomicLong();
    public static final AtomicLong strippedDsync = new AtomicLong();
    public static final AtomicLong skippedWrites = new AtomicLong();
    public static final AtomicLong skippedDeletes = new AtomicLong();
    public static final AtomicLong physicsNanos = new AtomicLong();
    public static final AtomicLong physicsCalls = new AtomicLong();
    public static final AtomicLong saveNanos = new AtomicLong();
    public static final AtomicLong saveCalls = new AtomicLong();
    public static volatile long lastSaveNanos;
    public static final String[] PHASES = {"serialize", "write", "holdingChunk", "flush", "processChanges"};
    public static final java.util.concurrent.atomic.AtomicLongArray phaseNanos = new java.util.concurrent.atomic.AtomicLongArray(5);
    public static final java.util.concurrent.atomic.AtomicLongArray phaseCalls = new java.util.concurrent.atomic.AtomicLongArray(5);

    public static void phase(int i, long nanos) {
        phaseNanos.addAndGet(i, nanos);
        phaseCalls.incrementAndGet(i);
    }

    private Stats() {
    }

    public static String summary() {
        long pc = physicsCalls.get();
        long sc = saveCalls.get();
        return "skippedPhysicsSteps=" + skippedPhysicsTicks.get()
                + ", physicsSteps=" + pc
                + ", avgStep=" + (pc == 0 ? 0 : physicsNanos.get() / pc / 1000) + "us"
                + ", strippedDsync=" + strippedDsync.get()
                + ", skippedWrites=" + skippedWrites.get()
                + ", skippedDeletes=" + skippedDeletes.get()
                + ", saves=" + sc
                + ", avgSave=" + (sc == 0 ? 0 : saveNanos.get() / sc / 1000) + "us"
                + ", lastSave=" + (lastSaveNanos / 1000) + "us" + phases();
    }

    private static String phases() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PHASES.length; i++) {
            sb.append(", ").append(PHASES[i]).append("=").append(phaseNanos.get(i) / 1000000).append("ms/").append(phaseCalls.get(i));
        }
        return sb.toString();
    }

    public static void reset() {
        skippedPhysicsTicks.set(0);
        physicsNanos.set(0);
        physicsCalls.set(0);
        saveNanos.set(0);
        saveCalls.set(0);
        lastSaveNanos = 0;
        skippedWrites.set(0);
        skippedDeletes.set(0);
        for (int i = 0; i < PHASES.length; i++) {
            phaseNanos.set(i, 0);
            phaseCalls.set(i, 0);
        }
    }
}