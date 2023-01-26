package com.gonsalves.timely.repository.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Timely-Projects")
public class ProjectEntity {

    @DynamoDBHashKey(attributeName = "user_id")
    private String userId;
    @DynamoDBRangeKey(attributeName = "project_name")
    private String projectName;
    @DynamoDBAttribute
    private String projectId;
    @DynamoDBAttribute(attributeName = "creation_date")
    private String creationDate;
    @DynamoDBAttribute(attributeName = "total_time_spent")
    private String totalTimeSpent;
    @DynamoDBAttribute(attributeName = "completion_percent")
    private Integer completionPercent;


}