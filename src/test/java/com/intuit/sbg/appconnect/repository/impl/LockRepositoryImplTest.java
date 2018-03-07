/*
 * Copyright (c) 2017. This material contains certain trade secrets and confidential and proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 */

package com.intuit.sbg.appconnect.repository.impl;

import com.intuit.sbg.appconnect.DynamoDBBaseTest;
import com.intuit.sbg.appconnect.TableInfo;
import com.intuit.sbg.appconnect.domain.Lock;
import com.intuit.sbg.appconnect.repository.dynamodb.LockRepository;
import com.intuit.sbg.appconnect.repository.dynamodb.impl.LockRepositoryImpl;
import org.junit.Assert;
import org.testng.annotations.Test;

/**
 * Created by vkumar21 on 7/7/17.
 */
public class LockRepositoryImplTest extends DynamoDBBaseTest {

    LockRepository repository = null;

    @Override
    public void initializeTableInfoAndRespository() {
        tableInfo = new TableInfo("dev.Lock", "Name", "S");
        repository = new LockRepositoryImpl(appConnectDynamoDBManager);
    }

    @Test
    public void testAcquireReadLockPositive() throws Exception {
        String lockName = "read-lock-positive";

        // acquire read lock three time
        Assert.assertTrue(repository.acquireReadLock(lockName));
        Assert.assertTrue(repository.acquireReadLock(lockName));
        Assert.assertTrue(repository.acquireReadLock(lockName));
        Lock lock = new Lock(lockName);
        Lock saved = repository.findOne(lock);
        Assert.assertEquals(3, saved.getReaders());
    }

    @Test()
    public void testAcquireReadLockNegative() throws Exception {
        String lockName = "read-lock-negative";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock= repository.findOne(lock);
        lock.setWriterName("hello");
        repository.save(lock);
        Assert.assertFalse(repository.acquireReadLock(lockName));
        Assert.assertEquals(0, lock.getReaders());
    }

    @Test
    public void testReserveWriteLockPositive() throws Exception {
        String lockName = "write-lock-positive";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock= repository.findOne(lock);
        // acquire write lock
        Assert.assertTrue(repository.reserveWriteLock(lock.getName(), lock.getName()));
        lock = repository.findOne(lock);
        Assert.assertEquals(lockName, lock.getWriterName());
    }

    @Test
    public void testReserveWriteLockPositiveWithReaders() throws Exception {
        String lockName = "write-lock-negativeReader";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock = repository.findOne(lock);
        lock.setReaders(10);
        repository.save(lock);

        // try to acquire the lock.. should fail
        Assert.assertTrue(repository.reserveWriteLock(lockName, lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(10, lock.getReaders());
        Assert.assertEquals(lockName, lock.getWriterName());
    }

    @Test
    public void testReserveWriteLockNegativeWithWriter() throws Exception {
        String lockName = "write-lock-negativeWriter";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock = repository.findOne(lock);
        lock.setWriterName(lockName);
        repository.save(lock);

        // try to acquire the lock.. should fail
        Assert.assertFalse(repository.reserveWriteLock(lockName, lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(0, lock.getReaders());
        Assert.assertEquals(lockName, lock.getWriterName());
    }


    @Test
    public void testAcquireWriteLockPositive() throws Exception {
        String lockName = "write-instant-lock-positive";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock= repository.findOne(lock);
        // acquire write lock
        Assert.assertTrue(repository.acquireWriteLock(lock.getName(), lock.getName()));
        lock = repository.findOne(lock);
        Assert.assertEquals(0, lock.getReaders());
        Assert.assertEquals(lockName, lock.getWriterName());
    }

    @Test
    public void testAcquireWriteLockNegativeWithReaders() throws Exception {
        String lockName = "write-instant-lock-negativeReader";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock = repository.findOne(lock);
        lock.setReaders(10);
        repository.save(lock);

        // try to acquire the lock.. should fail
        Assert.assertFalse(repository.acquireWriteLock(lockName, lockName));
    }

    @Test
    public void testAcquireWriteLockNegativeWithWriter() throws Exception {
        String lockName = "write-instant-lock-negativeWriter";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock = repository.findOne(lock);
        lock.setWriterName(lockName);
        repository.save(lock);

        // try to acquire the lock.. should fail
        Assert.assertFalse(repository.acquireWriteLock(lockName, lockName));
    }

    @Test
    public void testReleaseReadLock() throws Exception {
        String lockName = "read-lock-release";
        Lock lock = new Lock(lockName);
        repository.save(lock);

        // acquire read lock three time
        Assert.assertTrue(repository.acquireReadLock(lockName));
        Assert.assertTrue(repository.acquireReadLock(lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(2, lock.getReaders());
        Assert.assertTrue(repository.releaseReadLock(lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(1, lock.getReaders());
        Assert.assertTrue(repository.releaseReadLock(lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(0, lock.getReaders());
        // try to release a lock which have zero readers - means no lock
        Assert.assertFalse(repository.releaseReadLock(lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(0, lock.getReaders());
    }

    @Test
    public void testReleaseWriteReaders() throws Exception {
        String lockName = "write-lock-release";
        Lock lock = new Lock(lockName);
        repository.save(lock);

        // acquire write lock
        Assert.assertTrue(repository.reserveWriteLock(lockName, lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(lockName, lock.getWriterName());
        // release lock. Writer nam should be null
        Assert.assertTrue(repository.releaseWriteLock(lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(null, lock.getWriterName());
    }

    @Test
    public void testReleaseWriteWithReadersLock() throws Exception {
        String lockName = "write-lock-release-with-readers";
        Lock lock = new Lock(lockName);
        repository.save(lock);
        lock = repository.findOne(lock);
        lock.setReaders(10);
        repository.save(lock);

        // acquire write lock
        Assert.assertTrue(repository.reserveWriteLock(lockName, lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(lockName, lock.getWriterName());

        // release lock and check readers
        Assert.assertTrue(repository.releaseWriteLock(lockName));
        lock = repository.findOne(lock);
        Assert.assertEquals(null, lock.getWriterName());
        Assert.assertEquals(10, lock.getReaders());
    }

    @Test
    public void testCleanLocks() throws Exception {
        String lockName = "clean-locks";
        int total = 10;
        for (int i = 0; i < total; i++) {
            Lock lock = new Lock(lockName + i);
            lock.setWriterName(lockName);
            repository.save(lock);
        }
        repository.cleanupWriteLocks(lockName);
        for (int i = 0; i < total; i++) {
            Lock lock = new Lock(lockName + i);
            lock = repository.findOne(lock);
            Assert.assertEquals(null, lock.getWriterName());
        }
    }
}