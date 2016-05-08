/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.eventsourcing;

import org.testng.annotations.Test;

import java.util.concurrent.*;

import static org.testng.Assert.*;

public abstract class LockProviderTest<T extends LockProvider> {

    private final T lockProvider;

    public LockProviderTest(T lockProvider) {
        this.lockProvider = lockProvider;
    }

    @Test
    public void locking() {
        Lock lock = lockProvider.lock("test");
        assertTrue(lock.isLocked());
        lock.unlock();
        assertFalse(lock.isLocked());
    }

    @Test(timeOut = 2000)
    public void waiting() {
        Lock lock = lockProvider.lock("test");
        CompletableFuture<Void> future = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            lockProvider.lock("test");
            future.complete(null);
        });
        try {
            future.get(1, TimeUnit.SECONDS);
            fail("Lock wasn't locked");
            return;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
        }
        lock.unlock();
        future.join();
        assertTrue(future.isDone() && !future.isCancelled());
    }

}