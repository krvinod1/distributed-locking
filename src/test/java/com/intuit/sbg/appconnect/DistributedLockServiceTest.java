package com.intuit.sbg.appconnect;

import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Created by vkumar21 on 3/15/18.
 */
public class DistributedLockServiceTest {

    @Test
    public void multipleReadLock() {
        DistributedLock readLock = new DistributedLock("multipleReadLock");
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertTrue(readLock.lockForRead());
    }

    @Test
    public void readAndWriteLock() {
        DistributedLock readLock = new DistributedLock("readAndWriteLock");
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertFalse(readLock.lockForWrite(0));
    }

    @Test
    public void readAndWriteUnlockLock() {
        DistributedLock readLock = new DistributedLock("readAndWriteUnlockLock");
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertFalse(readLock.lockForWrite(0));
        Assert.assertTrue(readLock.release());
        Assert.assertTrue(readLock.lockForWrite(0));
    }

    @Test
    public void multipleWriteLock() {
        DistributedLock readLock = new DistributedLock("multipleWriteLock");
        Assert.assertTrue(readLock.lockForWrite(0));
        Assert.assertFalse(readLock.lockForWrite(0));
    }

}