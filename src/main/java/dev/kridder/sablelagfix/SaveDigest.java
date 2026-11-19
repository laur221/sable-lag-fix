package dev.kridder.sablelagfix;

public final class SaveDigest {
    public final int length;
    public final byte[] sha256;

    public SaveDigest(int length, byte[] sha256) {
        this.length = length;
        this.sha256 = sha256;
    }
}