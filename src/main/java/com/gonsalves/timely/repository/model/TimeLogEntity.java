package com.gonsalves.timely.repository.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBDocument
public class TimeLogEntity{
    @DynamoDBAttribute
    private String startDateTime;
    @DynamoDBAttribute
    private String endDateTime;

}