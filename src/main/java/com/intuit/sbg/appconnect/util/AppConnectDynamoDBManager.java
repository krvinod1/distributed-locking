/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect.util;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;

/**
 * Created by vkumar21 on 3/27/17.
 */
public class AppConnectDynamoDBManager {
    private DynamoDBMapper dynamoDBMapper;
    private DynamoDBMapperConfig config;
    private DynamoDB dynamoDB;

    public AppConnectDynamoDBManager(String url) {
        DynamoDBMapperConfig.TableNameOverride tableNameOverride = DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix
                ("dev.");
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(tableNameOverride).build();
        AmazonDynamoDBClient amazonDynamoDB = new AmazonDynamoDBClient();
        amazonDynamoDB.setEndpoint(url);
        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, config);
        dynamoDB = new DynamoDB(amazonDynamoDB);
    }

    public AppConnectDynamoDBManager(AWSCredentialsProvider awsCredentialsProvider,
                                     Region region, RuntimeEnvironment runtimeEnvironment, int maxRetry) {

        ClientConfiguration dynamoClientConfiguration = PredefinedClientConfigurations.dynamoDefault().withMaxErrorRetry(maxRetry);
        AmazonDynamoDBClient amazonDynamoDB = new AmazonDynamoDBClient(awsCredentialsProvider, dynamoClientConfiguration);
        amazonDynamoDB.setRegion(region);

        DynamoDBMapperConfig.TableNameOverride tableNameOverride = DynamoDBMapperConfig.TableNameOverride.withTableNamePrefix
                (runtimeEnvironment.toString().toLowerCase()+ ".");
        config = new DynamoDBMapperConfig.Builder().withTableNameOverride(tableNameOverride).build();

        dynamoDBMapper = new DynamoDBMapper(amazonDynamoDB, config);
        dynamoDB = new DynamoDB(amazonDynamoDB);
    }

    public DynamoDBMapper getDynamoDBMapper() {
        return dynamoDBMapper;
    }

    public DynamoDBMapperConfig getDynamoDBConfig() {
        return config;
    }

    public DynamoDB getAmazonDynamoDB() {
        return dynamoDB;
    }

}
