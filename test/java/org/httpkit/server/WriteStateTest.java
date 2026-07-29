package org.httpkit.server;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class WriteStateTest {
    @Test
    public void observesPendingEpochsAtomically() {
        ServerAtta atta = new HttpAtta(1024, 1024, ProxyProtocolOption.DISABLED, false);

        WriteState empty = atta.writeState();
        assertEquals(0, empty.getPendingBytes());
        assertTrue(empty.getDrained().isDone());

        atta.toWrites.add(ByteBuffer.wrap(new byte[8]));
        atta.toWrites.getFirst().position(3);
        atta.pendingWritesStarted();

        WriteState pending = atta.writeState();
        assertEquals(5, pending.getPendingBytes());
        assertFalse(pending.getDrained().isDone());
        assertSame(pending.getDrained(), atta.writeState().getDrained());

        atta.toWrites.clear();
        CompletableFuture<Void> drained = atta.pendingWritesDrained();
        WriteState afterDrain = atta.writeState();
        assertEquals(0, afterDrain.getPendingBytes());
        assertTrue(afterDrain.getDrained().isDone());
        assertFalse(pending.getDrained().isDone());
        drained.complete(null);
        assertTrue(pending.getDrained().isDone());

        atta.toWrites.add(ByteBuffer.wrap(new byte[1]));
        atta.pendingWritesStarted();
        assertNotSame(pending.getDrained(), atta.writeState().getDrained());
    }

    @Test
    public void closeSettlesTheObservedPendingEpoch() {
        ServerAtta atta = new HttpAtta(1024, 1024, ProxyProtocolOption.DISABLED, false);
        atta.toWrites.add(ByteBuffer.wrap(new byte[1]));
        atta.pendingWritesStarted();
        WriteState pending = atta.writeState();

        CompletableFuture<Void> closed = atta.pendingWritesClosed();
        assertFalse(pending.getDrained().isDone());
        closed.complete(null);
        assertTrue(pending.getDrained().isDone());
        assertTrue(atta.writeState().getDrained().isDone());
    }
}
