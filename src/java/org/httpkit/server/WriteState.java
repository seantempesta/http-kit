package org.httpkit.server;

import java.util.concurrent.CompletableFuture;

public final class WriteState {
    private final long pendingBytes;
    private final CompletableFuture<Void> drained;

    WriteState(long pendingBytes, CompletableFuture<Void> drained) {
        this.pendingBytes = pendingBytes;
        this.drained = drained;
    }

    public long getPendingBytes() {
        return pendingBytes;
    }

    public CompletableFuture<Void> getDrained() {
        return drained;
    }
}
