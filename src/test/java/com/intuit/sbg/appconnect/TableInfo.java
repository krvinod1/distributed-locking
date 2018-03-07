/*
 * CONFIDENTIAL -- Copyright 2017 Intuit Inc. This material contains certain trade secrets and confidential and
 * proprietary information of Intuit Inc. Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from Intuit Inc. Use of copyright notice is precautionary and does not imply
 * publication or disclosure.
 *
 */

package com.intuit.sbg.appconnect;

import java.util.List;

/**
 * Created by vkumar21 on 4/11/17.
 */
public class TableInfo {

    private String tableName;
    private String partitionKey;
    private String partitionKeyType;
    private String rangeKey;
    private String rangeKeyType;
    private List<IndexInfo> indexInfos;

    public TableInfo(String tableName, String partitionKey, String partitionKeyType, String rangeKey, String rangeKeyType) {
        this.tableName = tableName;
        this.partitionKey = partitionKey;
        this.partitionKeyType = partitionKeyType;
        this.rangeKey = rangeKey;
        this.rangeKeyType = rangeKeyType;
    }

    public TableInfo(String tableName, String partitionKey, String partitionKeyType) {
        this.tableName = tableName;
        this.partitionKey = partitionKey;
        this.partitionKeyType = partitionKeyType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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

    public List<IndexInfo> getIndexInfos() {
        return indexInfos;
    }

    public void setIndexInfos(List<IndexInfo> indexInfos) {
        this.indexInfos = indexInfos;
    }
}
