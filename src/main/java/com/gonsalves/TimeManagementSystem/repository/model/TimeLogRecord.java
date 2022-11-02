package com.gonsalves.TimeManagementSystem.repository.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class TimeLogRecord {
    @DynamoDBAttribute
    private String startDateTime; //time in format yyyy/MM/dd HH:mm:ss
    @DynamoDBAttribute
    private String endDateTime; //time in format yyyy/MM/dd HH:mm:ss

}


