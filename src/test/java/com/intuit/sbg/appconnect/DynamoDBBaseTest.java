/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.LocalSecondaryIndex;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.intuit.sbg.appconnect.util.AppConnectDynamoDBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * Created by vkumar21 on 4/11/17.
 */
public abstract class DynamoDBBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBBaseTest.class);

    protected static AppConnectDynamoDBManager appConnectDynamoDBManager;
    protected static TableInfo tableInfo = null;
    private DynamoDB dynamodb = null;
    private DynamoDBProxyServer server = null;

    @BeforeClass
    public void initialize() throws Exception {
        try {
            final String[] localArgs = {"-inMemory"};
            System.setProperty("sqlite4java.library.path", "native-libs");
            System.setProperty("aws.accessKeyId", "foo");
            System.setProperty("aws.secretKey", "foo");
            final String port = getAvailablePort();
            LOGGER.debug("Starting local dynamo db at port .." + port);

            server = ServerRunner.createServerFromCommandLineArgs(new String[]{"-inMemory", "-port", port});
            server.start();
            AmazonDynamoDB amazonDynamoDB = new AmazonDynamoDBClient();
            String endpoint = "http://localhost:" + port;
            amazonDynamoDB.setEndpoint(endpoint);
            dynamodb = new DynamoDB(amazonDynamoDB);
            appConnectDynamoDBManager = new AppConnectDynamoDBManager(endpoint);
        } catch (Exception e) {
            LOGGER.debug("exception while creating dynamo Local DB " + e.getMessage());
            throw e;
        }
        initializeTableInfoAndRespository();
        createTable();
    }

    public abstract void initializeTableInfoAndRespository();

    @AfterClass
    public void stopServer() {
        try {
            if (server != null) {
                LOGGER.debug("Stopping local server.. " );
                server.stop();
            }
        } catch (Exception e) {
            LOGGER.debug("got exception while trying to stop local dynamo db " + e.getMessage());
        }
    }

    private String getAvailablePort() {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            return String.valueOf(serverSocket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException("Available port was not found", e);
        }
    }

    private void createTable() throws Exception {
        if (tableInfo == null) {
            throw new Exception("Please initialize table info to create the table ");
        }
        try {

            ArrayList<KeySchemaElement> keySchema = new ArrayList<>();
            keySchema.add(new KeySchemaElement().withAttributeName(tableInfo.getPartitionKey()).withKeyType(KeyType.HASH)); //Partition key

            ArrayList<AttributeDefinition> attributeDefinitions = new ArrayList<>();
            attributeDefinitions.add(new AttributeDefinition().withAttributeName(tableInfo.getPartitionKey())
                    .withAttributeType(tableInfo.getPartitionKeyType()));

            if (tableInfo.getRangeKey() != null) {
                keySchema.add(new KeySchemaElement().withAttributeName(tableInfo.getRangeKey()).withKeyType(KeyType.RANGE)); //Sort key
                attributeDefinitions.add(new AttributeDefinition().withAttributeName(tableInfo.getRangeKey()).withAttributeType(tableInfo.getRangeKeyType()));
            }

            CreateTableRequest request = new CreateTableRequest().withTableName(tableInfo.getTableName()).withKeySchema(keySchema)
                    .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(50L).withWriteCapacityUnits(10l));

            if (tableInfo.getIndexInfos() != null && tableInfo.getIndexInfos().size() > 0) {
                ArrayList<LocalSecondaryIndex> localSecondaryIndexes = new ArrayList<>();
                ArrayList<GlobalSecondaryIndex> globalSecondaryIndexes = new ArrayList<>();
                for (IndexInfo indexInfo : tableInfo.getIndexInfos()) {
                    attributeDefinitions.add(new AttributeDefinition().withAttributeName(indexInfo.getRangeKey()).withAttributeType(indexInfo.getRangeKeyType()));

                    if (indexInfo.isLocal()) {
                        localSecondaryIndexes.add(new LocalSecondaryIndex().withIndexName(indexInfo.getIndexName()).withKeySchema(
                                new KeySchemaElement().withAttributeName(tableInfo.getPartitionKey()).withKeyType(KeyType.HASH), //Partition key
                                new KeySchemaElement().withAttributeName(indexInfo.getRangeKey()).withKeyType(KeyType.RANGE)) //Sort key
                                .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY)));
                    } else {
                        globalSecondaryIndexes.add(new GlobalSecondaryIndex().withIndexName(indexInfo.getIndexName()).withKeySchema(
                                new KeySchemaElement().withAttributeName(indexInfo.getPartitionKey()).withKeyType(KeyType.HASH),
                                //Partition key
                                new KeySchemaElement().withAttributeName(indexInfo.getRangeKey()).withKeyType(KeyType.RANGE)) //Sort key
                                .withProjection(new Projection().withProjectionType(ProjectionType.KEYS_ONLY)));

                        AttributeDefinition attributeDefinition = new AttributeDefinition().withAttributeName(indexInfo.getPartitionKey())
                                .withAttributeType(indexInfo.getPartitionKeyType());

                        if (!attributeDefinitions.contains(attributeDefinition)) {
                            attributeDefinitions.add(attributeDefinition);
                        }
                    }
                }

                if (localSecondaryIndexes.size() > 0) {
                    request.setLocalSecondaryIndexes(localSecondaryIndexes);
                }

                if (globalSecondaryIndexes.size() > 0) {
                    request.setGlobalSecondaryIndexes(globalSecondaryIndexes);
                }
            }

            request.setAttributeDefinitions(attributeDefinitions);
            LOGGER.debug("Creating table " + tableInfo.getTableName());
            Table table = dynamodb.createTable(request);
            table.waitForActive();

        } catch (Exception e) {
            LOGGER.error("CreateTable request failed for " + tableInfo.getTableName());
            throw e;
        }
    }
}
