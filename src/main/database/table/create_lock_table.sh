#!/usr/bin/env bash
source env_util.sh
echo $(prop 'amazon.dynamodb.endpoint')

aws dynamodb --profile $(prop 'aws.profile') --endpoint-url $(prop 'amazon.dynamodb.endpoint') \
   create-table --table-name $(prop 'environment').Lock \
   --attribute-definitions \
            AttributeName=Name,AttributeType=S \
    --key-schema AttributeName=Name,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=200,WriteCapacityUnits=50