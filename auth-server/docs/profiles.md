# Profile

VAuthenticator can be configured to be strongly AWS integrated using DynamoDB for the persistence layer and KMS for Key management. 

If your organization or for you run VAuthenticator so tiny integrated with AWS does not is suitable you can decide to switch postgresql instead dynamodb for the persistence
and plain java security key management instead of KMS

All what you need is enable the relative spring profile as below:

use ```spring.profiles.active``` with 

- ```database```: to use PostgresSQL 
- ```dynamo```: to use DynamoDB
- ```kms``` to use KMS
- omitting ```kms``` to use plain java security api

in case of plain java security implementation the follow configuration is required:


```yaml
key:
  master-key:
    storage:
      content:
        key : value
        key2 : value2
```

