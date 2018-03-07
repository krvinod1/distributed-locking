/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect.util;


import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by vkumar21 on 4/12/17.
 */
public class AppConnectDynamoDBManagerTest {

    @Test
    public void constructionOneTest() {
        AppConnectDynamoDBManager appConnectDynamoDBManager = new AppConnectDynamoDBManager("http://locahost:9000");
        DynamoDBMapperConfig config = appConnectDynamoDBManager.getDynamoDBConfig();
        DynamoDBMapper mapper = appConnectDynamoDBManager.getDynamoDBMapper();
        Assert.assertNotNull(config);
        Assert.assertNotNull(mapper);
        Assert.assertEquals("dev.", config.getTableNameOverride().getTableNamePrefix());
    }


    @Test
    public void constructionStageTest() {
        // this is done to run test at QA machine - related to AWS AWSCredentials
        String  oldAccessKey = System.getProperty("aws.accessKeyId");
        String  oldSecretKey = System.getProperty("aws.secretKey");
        System.setProperty("aws.accessKeyId", "foo");
        System.setProperty("aws.secretKey", "bar");
        AWSCredentialsProvider awsCredentialsProvider = new InstanceProfileCredentialsProvider();
        Region region = Region.getRegion(Regions.US_WEST_2);
        AppConnectDynamoDBManager appConnectDynamoDBManager = new AppConnectDynamoDBManager(awsCredentialsProvider, region,  RuntimeEnvironment.STAGE, 5);

        Assert.assertNotNull(appConnectDynamoDBManager.getDynamoDBConfig());
        Assert.assertNotNull(appConnectDynamoDBManager.getDynamoDBMapper());
        Assert.assertEquals("stage.", appConnectDynamoDBManager.getDynamoDBConfig().getTableNameOverride().getTableNamePrefix());
        //reset the property
        if ( oldAccessKey != null) {
            System.setProperty("aws.accessKeyId", oldAccessKey);
        }

        if ( oldSecretKey != null) {
            System.setProperty("aws.secretKey", oldSecretKey);
        }
    }

}