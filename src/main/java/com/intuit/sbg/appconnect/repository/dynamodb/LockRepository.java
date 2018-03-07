/*
 * Copyright (c) 2017. This material contains certain trade secrets and confidential and proprietary information of Intuit Inc. Use,
 * reproduction, disclosure and distribution by any means are prohibited,except pursuant to a written license from Intuit Inc. Use of
 * copyright notice is precautionary and does not imply
 * publication or disclosure.
 */

package com.intuit.sbg.appconnect.repository.dynamodb;

import com.intuit.sbg.appconnect.domain.Lock;
import com.intuit.sbg.appconnect.repository.Repository;

/**
 * Created by vkumar21 on 7/6/17.
 */
public interface LockRepository extends Repository<Lock> {

    /**
     * Acquire a read lock. A read lock can be acquire only if there is no writer for a given lock
     * @param name
     * @return
     */
    boolean acquireReadLock(String name);

    /**
     * Acquire a write lock. A write lock can be acquire only if there is no writer for a given lock
     * @param name
     * @param writerName
     * @return
     */
    boolean reserveWriteLock(String name, String writerName);

    /**
     * Acquire an instant write lock. A instant write lock can be acquire only if there is no writer and no readers for a given lock
     * @param name
     * @param writerName
     * @return
     */
    boolean acquireWriteLock(String name, String writerName);

    /**
     * Release a read lock. If no read lock exist, this method will throw the exception
     * @param name
     * @return
     */
    boolean releaseReadLock(String name);

    /**
     * Release a write lock. This method is immutable and calling a release lock on again will not have any side effect
     * @param name
     * @return
     */
    boolean releaseWriteLock(String name);

    /**
     *  Clear the write locks for a given writer
     * @param writerName
     * @return No of locks cleaned
     */

    int cleanupWriteLocks(String writerName);

}
