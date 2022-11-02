package com.gonsalves.TimeManagementSystem.repository.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.gonsalves.TimeManagementSystem.service.model.TimeLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
@Builder
@Data
@AllArgsConstructor
@DynamoDBDocument
public class TaskRecord {
    @DynamoDBAttribute
    private String taskName;
    @DynamoDBAttribute
    private String notes;
    @DynamoDBAttribute
    private String timeSpentOnTask;
    @DynamoDBAttribute
    private List<TimeLogRecord> timeLogRecords;
    @DynamoDBAttribute
    private boolean completionStatus;

    public TaskRecord() {
        this.taskName = "";
        this.notes = "";
        this.timeSpentOnTask = "00:00:00";
        this.timeLogRecords = new ArrayList<>();
        this.completionStatus = false;
    }

}
