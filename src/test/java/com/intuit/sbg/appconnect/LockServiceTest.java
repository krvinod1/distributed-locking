package com.intuit.sbg.appconnect;

import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Created by vkumar21 on 3/15/18.
 */
public class LockServiceTest {

    @Test
    public void multipleReadLock() {
        LockService readLock = new LockService("multipleReadLock");
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertTrue(readLock.lockForRead());
    }

    @Test
    public void readAndWriteLock() {
        LockService readLock = new LockService("readAndWriteLock");
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertFalse(readLock.lockForWrite(0));
    }

    @Test
    public void readAndWriteUnlockLock() {
        LockService readLock = new LockService("readAndWriteUnlockLock");
        Assert.assertTrue(readLock.lockForRead());
        Assert.assertFalse(readLock.lockForWrite(0));
        Assert.assertTrue(readLock.release());
        Assert.assertTrue(readLock.lockForWrite(0));
    }

    @Test
    public void multipleWriteLock() {
        LockService readLock = new LockService("multipleWriteLock");
        Assert.assertTrue(readLock.lockForWrite(0));
        Assert.assertFalse(readLock.lockForWrite(0));
    }

}