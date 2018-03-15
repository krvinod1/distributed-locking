/*
 * Copyright (c) 2017. This material contains certain trade secrets and confidential and proprietary information of Intuit Inc. Use,
 * reproduction, disclosure and distribution by any means are prohibited,except pursuant to a written license from Intuit Inc. Use of
 * copyright notice is precautionary and does not imply
 * publication or disclosure.
 */

package com.intuit.sbg.appconnect.repository.dynamodb.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.intuit.sbg.appconnect.domain.Lock;
import com.intuit.sbg.appconnect.repository.dynamodb.LockRepository;
import com.intuit.sbg.appconnect.util.AppConnectDynamoDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vkumar21 on 7/6/17.
 */
public class LockRepositoryImpl extends AbstractDynamoRepository<Lock> implements LockRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(LockRepositoryImpl.class);

    public LockRepositoryImpl(AppConnectDynamoDBManager appConnectDynamoDBManager) {
        super(appConnectDynamoDBManager);
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(config.getTableNameOverride())
                .withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build();
    }

    @Override
    public Class<Lock> getDomainClass() {
        return Lock.class;
    }

    private  void createLockIfNeeded(String name) {
        Lock lock = new Lock(name);
        try {
            lock = findOne(lock);
                if (lock == null) {
                    lock = new Lock(name);
                    save(lock);
                }
        } catch (Exception ex) {
            LOGGER.info("createLockIfNeeded(); probably lock already exist ={} ", name);
        }
    }


    @Override
    public boolean acquireReadLock(String name) {
        boolean retValue = false;
        try {
            createLockIfNeeded(name);
            String tableName = DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE.getTableName(Lock.class, config);
            Table table = dynamoDB.getTable(tableName);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Name", name)
                                            .withUpdateExpression("SET Readers = Readers + :incr")
                                            .withConditionExpression("attribute_not_exists(WriterName) OR WriterName = :writer")
                                            .withValueMap(new ValueMap()
                                            .withNumber(":incr", 1).withNull(":writer"));

            table.updateItem(updateItemSpec);
            retValue = true;
            LOGGER.info("acquireReadLock(); Read Lock acquired, resource name ={};" , name);
        } catch (Exception e) {
            LOGGER.info("acquireReadLock(); Save condition didn't match, resource name ={};" , name);
        }
        return retValue;
    }


    @Override
    public boolean reserveWriteLock(String name, String writerName) {
        boolean retValue = false;
        try {
            createLockIfNeeded(name);
            String tableName = DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE.getTableName(Lock.class, config);
            Table table = dynamoDB.getTable(tableName);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Name", name)
                                            .withUpdateExpression("SET WriterName = :writerName")
                                            .withConditionExpression("attribute_not_exists(WriterName) OR WriterName = :writer")
                                            .withValueMap(new ValueMap()
                                            .withNull(":writer").withString(":writerName", writerName));

            table.updateItem(updateItemSpec);
            retValue = true;
            LOGGER.info("reserveWriteLock(); Lock reserved ={}; writer name ={}", name, writerName);
        } catch (Exception e) {
            LOGGER.info("reserveWriteLock(); Save condition didn't match, resource name ={}; writer name ={}", name, writerName);
        }
        return retValue;
    }

    @Override
    public boolean acquireWriteLock(String name, String writerName) {
        boolean retValue = false;
        try {
            createLockIfNeeded(name);
            String tableName = DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE.getTableName(Lock.class, config);
            Table table = dynamoDB.getTable(tableName);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Name", name)
                    .withUpdateExpression("SET WriterName = :writerName")
                    .withConditionExpression("(attribute_not_exists(WriterName) OR WriterName = :writer) and Readers=:readers")
                    .withValueMap(new ValueMap()
                    .withNull(":writer").withString(":writerName", writerName)
                    .withInt(":readers", 0));
            table.updateItem(updateItemSpec);
            retValue = true;
            LOGGER.info("acquireWriteLock(); Write lock acquired, resource name ={}; writer name ={}", name, writerName);
        } catch (Exception e) {
            LOGGER.info("acquireWriteLock(); Save condition didn't match, resource name ={}; writer name ={}", name, writerName);
        }
        return retValue;
    }

    @Override
    public boolean releaseReadLock(String name) {
        boolean retValue = false;
        try {
            createLockIfNeeded(name);
            String tableName = DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE.getTableName(Lock.class, config);
            Table table = dynamoDB.getTable(tableName);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Name", name)
                    .withUpdateExpression("SET Readers = Readers - :decr")
                    .withConditionExpression("NOT Readers = :min")
                    .withValueMap(new ValueMap().withNumber(":decr", 1).withNumber(":min", 0));

            table.updateItem(updateItemSpec);
            retValue = true;
            LOGGER.info("releaseReadLock(); Read Lock released, resource name ={};" , name);
        } catch (Exception e) {
            LOGGER.info("releaseReadLock(); Save condition didn't match, resource name ={};" , name);
        }
        return retValue;
    }

    @Override
    public boolean releaseWriteLock(String name) {
        boolean retValue = false;
        try {
            createLockIfNeeded(name);
            String tableName = DynamoDBMapperConfig.DefaultTableNameResolver.INSTANCE.getTableName(Lock.class, config);
            Table table = dynamoDB.getTable(tableName);
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey("Name", name)
                    .withUpdateExpression("REMOVE WriterName");

            table.updateItem(updateItemSpec);
            retValue = true;
            LOGGER.info("releaseWriteLock(); Write lock releasd, resource name ={};" , name);
        } catch (Exception e) {
            LOGGER.info("releaseWriteLock(); Save condition didn't match, resource name ={};" , name);
        }
        return retValue;
    }

    @Override
    public int cleanupWriteLocks(String writerName) {

        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":writer", new AttributeValue().withS(writerName));

        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                .withFilterExpression("WriterName = :writer").withExpressionAttributeValues(eav);
        List<Lock> locks = dynamoDBMapper.scan(getDomainClass(), scanExpression);
        int lockCleaned = 0;
        for (Lock lock : locks) {
            try {
                LOGGER.info("cleanupWriteLocks, cleaning lock ={}", lock.getName());
                lock.setWriterName(null);
                dynamoDBMapper.save(lock);
                lockCleaned++;
            } catch (Exception e) {
                LOGGER.debug("cleanupWriteLocks, error encountered when clearing a lock ={}", lockCleaned, e);
            }
        }
        LOGGER.info("cleanupWriteLocks(), locks cleaned ={}", lockCleaned);
        return lockCleaned;
    }
}