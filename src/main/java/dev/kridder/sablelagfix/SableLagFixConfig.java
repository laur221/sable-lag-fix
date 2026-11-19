package dev.kridder.sablelagfix;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class SableLagFixConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue SKIP_IDLE_PHYSICS;
    private static final ModConfigSpec.BooleanValue NO_PER_WRITE_DISK_SYNC;
    private static final ModConfigSpec.BooleanValue SKIP_UNCHANGED_WRITES;
    private static final ModConfigSpec.BooleanValue SKIP_REDUNDANT_EXTERNAL_DELETE;
    private static final ModConfigSpec.BooleanValue FAST_COMPRESSION;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        SKIP_IDLE_PHYSICS = builder
                .comment("Skip Sable's physics step for a dimension when it has no sub-levels and no arbitrary physics objects.")
                .define("skipIdlePhysics", true);
        NO_PER_WRITE_DISK_SYNC = builder
                .comment("Open sub-level storage files without DSYNC. Faster saves, but a power loss or OS crash during a save can corrupt sub-levels written in that save (a normal game crash is safe). Off by default; with skipUnchangedWrites most writes are already avoided.")
                .define("noPerWriteDiskSync", false);
        SKIP_UNCHANGED_WRITES = builder
                .comment("Skip rewriting a sub-level to disk when its saved bytes are identical to the last write (exact NBT comparison, checked before compression). Parked ships no longer cost anything on autosave.")
                .define("skipUnchangedWrites", true);
        SKIP_REDUNDANT_EXTERNAL_DELETE = builder
                .comment("Skip the per-write delete of a non-existent external overflow file when the storage file has no external files.")
                .define("skipRedundantExternalDelete", true);
        FAST_COMPRESSION = builder
                .comment("Compress changed sub-levels with the fastest GZIP level. The file format is unchanged and fully readable by Sable without this mod.")
                .define("fastCompression", true);
        SPEC = builder.build();
    }

    private SableLagFixConfig() {
    }

    public static boolean skipIdlePhysics() {
        try {
            return SKIP_IDLE_PHYSICS.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static boolean skipUnchangedWrites() {
        try {
            return SKIP_UNCHANGED_WRITES.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static boolean skipRedundantExternalDelete() {
        try {
            return SKIP_REDUNDANT_EXTERNAL_DELETE.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static boolean fastCompression() {
        try {
            return FAST_COMPRESSION.get();
        } catch (IllegalStateException e) {
            return true;
        }
    }

    public static boolean noPerWriteDiskSync() {
        try {
            return NO_PER_WRITE_DISK_SYNC.get();
        } catch (IllegalStateException e) {
            return false;
        }
    }
}