package com.gonsalves.timely.repository.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Timely-Tasks")
public class TaskEntity {
    @DynamoDBHashKey(attributeName = "project_id")
    private String projectId;
    @DynamoDBRangeKey(attributeName = "task_name")
    private String taskName;
    @DynamoDBAttribute(attributeName = "notes")
    private String notes;
    @DynamoDBAttribute(attributeName = "time_spent")
    private String timeSpent;
    @DynamoDBAttribute(attributeName = "time_logs")
    private List<TimeLogEntity> timeLogEntities;
    @DynamoDBAttribute(attributeName = "status")
    private String status;


}