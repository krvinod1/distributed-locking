/*
 * Copyright (c) 2017. This material contains certain trade secrets and confidential and proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 */

package com.intuit.sbg.appconnect.domain;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by vkumar21 on 7/6/17.
 */
public class DistributedLockTest {

    @Test
    public void constructionTest() {
        String lockName = "evdLock";
        Lock lock = new Lock(lockName);
        Assert.assertNotNull(lock.getName());
        Assert.assertEquals(lockName, lock.getName());
    }

    @Test
    public void getterSetterTest() {
        String name = "evdLock";
        Lock lock = new Lock(name);
        Assert.assertEquals(name, lock.getName());
        name = "evdLock-updated";
        lock.setName(name);
        Assert.assertEquals(name, lock.getName());
        lock.setWriterName(name);
        Assert.assertEquals(name, lock.getWriterName());
        lock.setReaders(5);
        Assert.assertEquals(5, lock.getReaders());
        String date = "10-10-2017";
        lock.setCreated(date);
        Assert.assertEquals(date, lock.getCreated());
    }

}