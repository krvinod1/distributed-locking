/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect.repository.dynamodb.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.intuit.sbg.appconnect.repository.Repository;
import com.intuit.sbg.appconnect.util.AppConnectDynamoDBManager;

import java.util.List;

/**
 * Created by vkumar21 on 3/28/17.
 */
public abstract class AbstractDynamoRepository<T> implements Repository<T> {
    protected DynamoDBMapper dynamoDBMapper;
    protected DynamoDBMapperConfig config;
    protected DynamoDB dynamoDB;

    public AbstractDynamoRepository(AppConnectDynamoDBManager appConnectDynamoDBManager) {
        this.dynamoDBMapper = appConnectDynamoDBManager.getDynamoDBMapper();
        this.config = appConnectDynamoDBManager.getDynamoDBConfig();
        this.dynamoDB = appConnectDynamoDBManager.getAmazonDynamoDB();
    }

    @Override
    public void save(T entity, boolean override) {
        DynamoDBMapperConfig localConfig = config;
        if (override) {
            localConfig = new DynamoDBMapperConfig.Builder().withTableNameOverride(config.getTableNameOverride())
                    .withSaveBehavior(DynamoDBMapperConfig.SaveBehavior.CLOBBER).build();
        }
        dynamoDBMapper.save(entity, localConfig);
    }

    @Override
    public void save(T entity) {
       save(entity, false);
    }

    @Override
    public void save(List<T> entities) {
        dynamoDBMapper.batchSave(entities);
    }

    @Override
    public T findOne(T entity) {
        return dynamoDBMapper.load(entity, config);
    }

    @Override
    public List<T> findAll() {
        return dynamoDBMapper.scan(getDomainClass(), new DynamoDBScanExpression(), config);
    }

    @Override
    public List<T> query(T partitionKey) {
        DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                .withHashKeyValues(partitionKey);

        return dynamoDBMapper.query(getDomainClass(), queryExpression, config);
    }

    @Override
    public void delete(T entity) {
        dynamoDBMapper.delete(entity);
    }

    @Override
    public void deleteAll() {
        dynamoDBMapper.batchDelete(findAll());
    }

    @Override
    public long count() {
        return dynamoDBMapper.count(getDomainClass(), new DynamoDBScanExpression(), config);
    }
}
