package com.gonsalves.TimeManagementSystem.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
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
public class TimeLog {
    private static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private String startDateTime; //time in format yyyy/MM/dd HH:mm:ss
    private String endDateTime; //time in format yyyy/MM/dd HH:mm:ss


    public void start() {
        LocalDateTime now = LocalDateTime.now();
        this.startDateTime = dtf.format(now);

    }
    public void stop() {
        LocalDateTime now = LocalDateTime.now();
        this.endDateTime = dtf.format(now);
    }
}


