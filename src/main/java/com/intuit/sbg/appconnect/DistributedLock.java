package com.intuit.sbg.appconnect;

import com.intuit.sbg.appconnect.repository.dynamodb.LockRepository;
import com.intuit.sbg.appconnect.repository.dynamodb.impl.LockRepositoryImpl;
import com.intuit.sbg.appconnect.util.AppConnectDynamoDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Date;

/**
 * Created by vkumar21 on 3/15/18.
 */
public class DistributedLock {
    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedLock.class);

    private final String resourceName;
    private LockState active_lock;
    private LockRepository lockRepository;


    /**
     * Create a named lock, where the name defines a single named construct that
     * will regulate access to a specific resource.
     *
     * @param name Logical name of LockState to create.
     */
    public DistributedLock(String name) {
        resourceName = name;
        active_lock = LockState.NO_LOCK;
        lockRepository = new LockRepositoryImpl(new AppConnectDynamoDBManager("http://localhost:8000"));
    }

    /**
     * Get a read lock on the protected resource.  Multiple readers are allowed concurrently;
     * however, the requestor will block if a reader is currently accessing the resource.
     *
     * @return boolean: return true, if read lock acquire otherwise false

     */
    public boolean lockForRead() {
        boolean acquiredLock = false;
        try {
            int attempts = 0;
            while (!acquiredLock && attempts < 5) {
                acquiredLock = lockRepository.acquireReadLock(resourceName);
                attempts++;
                if (!acquiredLock) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception ex) {
                    }
                }
            }
            active_lock = LockState.READ_LOCK;
        } catch (Exception e) {
            LOGGER.warn("lockForRead(); lock for read acquire failed, resource name ={};", resourceName, e);
        }
        return acquiredLock;
    }

    /**
     * Acquire a lock with a given timeout. If lock can't be acquired in specified time, return false.
     * value of 0 indicates that the call should try to get a lock only once.
     *
     * @param timeout approximate milliseconds to wait before returning
     *                a false value.
     * @return boolean true if a lock has been obtained, false if
     * a lock has not been obtained within the requested time.
     */
    public boolean lockForWrite(int timeout)  {
        int elapsedWait = 0;
        boolean acquiredLock = false;
        boolean reserved = false;
        String hostname = "unknown";
        int pollInterval = (timeout < 100) ? timeout : 100;
        /*
           First try to get a write lock ( with writer_name as null and no reader). If unable to get a lock, try to reserve the lock
           ( writer_name should be empty to reserve the lock) and wait for the reader to drain out. If readers drain out can't be completed
            in timeout, release the reserve lock
         */

        try {
            hostname = InetAddress.getLocalHost().getHostAddress();
            acquiredLock = lockRepository.acquireWriteLock(resourceName, hostname);
            while (!acquiredLock && elapsedWait < timeout) {
                if (!reserved) {
                    reserved = lockRepository.reserveWriteLock(resourceName, hostname);
                }
                try {
                    Thread.sleep(pollInterval);
                } catch (Exception e) {
                }
                elapsedWait = elapsedWait + pollInterval;
                if (reserved) {
                    com.intuit.sbg.appconnect.domain.Lock lock = lockRepository.findOne(new com.intuit.sbg.appconnect.domain.Lock(resourceName));
                    acquiredLock = lock.getReaders() < 1;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("lockForWrite(); lock for write acquire failed, resource name ={}; writer ={};", resourceName, hostname, e);
        }

        if (acquiredLock) {
            active_lock = LockState.WRITE_LOCK;
        } else if (reserved) {
            try {
                com.intuit.sbg.appconnect.domain.Lock lock = lockRepository.findOne(new com.intuit.sbg.appconnect.domain.Lock(resourceName));
                lock.setWriterName(null);
                lockRepository.save(lock);
            } catch (Exception e) {
                LOGGER.warn("lockForWrite(); reset reserve lock failed, resource name ={}; writer ={};", resourceName, hostname, e);
            }
        }
        return acquiredLock;
    }

    /**
     * Unlock a read or write lock on a protected resource.  The application must
     * call this upon completion of a read or write task; otherwise, the lock will
     * not be released, inducing deadlocks on the system.
     *
     * @return boolean: returns if lock successfully released
     */
    public boolean release() {
        return release(false);
    }

    /**
     * Unlock a read or write lock on a protected resource.  The application must
     * call this upon completion of a read or write task; otherwise, the lock will
     * not be released, inducing deadlocks on the system.
     *
     * @param deleteLock - if true, and the lock being released is a write lock or
     *                   a read lock in which the number of readers is now 0,
     *                   delete the lock entry (rather than persisting with an
     *                   empty writer and 0 readers).
     *
     * @return boolean: returns if lock successfully released
     */
    public boolean release(boolean deleteLock) {
        // Start the clock
        long startTime = System.currentTimeMillis();
        boolean lockReleased = false;
        for (int attempts = 0; attempts < 5 && !lockReleased; attempts++) {
            try {
                switch (active_lock) {
                    case READ_LOCK:
                        lockReleased = lockRepository.releaseReadLock(resourceName);
                        break;
                    case WRITE_LOCK:
                        if (deleteLock) {
                            com.intuit.sbg.appconnect.domain.Lock lock = lockRepository.findOne(new com.intuit.sbg.appconnect.domain.Lock(resourceName));
                            lockRepository.delete(lock);
                            lockReleased = true;
                        } else {
                            lockReleased = lockRepository.releaseWriteLock(resourceName);
                        }
                        break;
                    case NO_LOCK:
                        break;
                }
            } catch (Exception e) {
                LOGGER.warn("release(); release failed, resource name ={}; active lock ={};", resourceName, active_lock, e);
            }
        }

        if (lockReleased) {
            active_lock = LockState.NO_LOCK;
        }

        if (active_lock != LockState.NO_LOCK) {
            LOGGER.error("release(); unable to release the lock , resource name ={}; active lock ={};", resourceName, active_lock);
        }
        return lockReleased;
    }

    /**
     * This method enables cleanup of distributed LockState locks.  Although scoped
     * publicly, it should normally not be invoked by application code, as incorrect
     * usage will cause undesired behavior.
     *
     * @param aNode       - The node for which locks are to be cleaned.  Absence of node (null
     *                    or empty node name) is treated as a wildcard.
     * @param lockPattern - A pattern to be matched against resource name
     * @param when        - If non-null, refines the list of locks to be cleaned by restricting
     *                    cleaning to those locks whose last changed date is before this datetime.
     */
    public int cleanupLocks(String aNode, String lockPattern, Date when) {
        return lockRepository.cleanupWriteLocks(aNode);
    }

    public enum LockState {
        NO_LOCK,
        READ_LOCK,
        WRITE_LOCK
    }

}
