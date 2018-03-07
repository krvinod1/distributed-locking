
package com.intuit.sbg.appconnect.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

import java.time.Instant;

/**
 * Created by vkumar21 on 7/6/17.
 */

@DynamoDBTable(tableName = "Lock")
public class Lock {
    private String name;
    private String writerName;
    private int readers;
    private String created;
    private Long version;

    public Lock() {
        this.created = Instant.now().toString();
    }

    public Lock(String name) {
        this.name = name;
        this.created = Instant.now().toString();
    }

    @DynamoDBHashKey(attributeName = "Name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBAttribute(attributeName = "WriterName")
    public String getWriterName() {
        return writerName;
    }
    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    @DynamoDBAttribute(attributeName = "Readers")
    public int getReaders() {
        return readers;
    }
    public void setReaders(int readers) {
        this.readers = readers;
    }

    @DynamoDBAttribute(attributeName = "Created")
    public String getCreated() {
        return created;
    }
    public void setCreated(String created) {
        this.created = created;
    }

    @DynamoDBVersionAttribute(attributeName = "Ver")
    public Long getVersion() {
        return version;
    }
    public void setVersion(Long version) {
        this.version = version;
    }
}
