/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect;

/**
 * Created by vkumar21 on 4/11/17.
 */
public class IndexInfo {
    private String indexName;
    private boolean local;
    private String partitionKey;
    private String partitionKeyType;
    private String rangeKey;
    private String rangeKeyType;

    public IndexInfo(String indexName, boolean local, String partitionKey, String partitionKeyType, String rangeKey, String rangeKeyType) {
        this.indexName = indexName;
        this.local = local;
        this.partitionKey = partitionKey;
        this.partitionKeyType = partitionKeyType;
        this.rangeKey = rangeKey;
        this.rangeKeyType = rangeKeyType;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getPartitionKeyType() {
        return partitionKeyType;
    }

    public void setPartitionKeyType(String partitionKeyType) {
        this.partitionKeyType = partitionKeyType;
    }

    public String getRangeKey() {
        return rangeKey;
    }

    public void setRangeKey(String rangeKey) {
        this.rangeKey = rangeKey;
    }

    public String getRangeKeyType() {
        return rangeKeyType;
    }

    public void setRangeKeyType(String rangeKeyType) {
        this.rangeKeyType = rangeKeyType;
    }
}
