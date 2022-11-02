package com.gonsalves.TimeManagementSystem.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
@Builder
@Data
@AllArgsConstructor
public class Task {
    private String taskName;
    private String notes;
    private String timeSpentOnTask;
    private List<TimeLog> timeLogs;
    private boolean completionStatus;

    public Task() {
        this.taskName = "";
        this.notes = "";
        this.timeSpentOnTask = "00:00:00";
        this.timeLogs = new ArrayList<TimeLog>();
        this.completionStatus = false;
    }
    public Task(String taskName, String notes) {
        this.taskName = taskName;
        this.notes = notes;
        this.timeSpentOnTask = "00:00:00";
        this.timeLogs = new ArrayList<TimeLog>();
        this.completionStatus = false;
    }
    public String getTimeSpentOnTask() {
        this.calculateTimeSpentOnTask();
        return this.timeSpentOnTask;
    }
    public boolean getCompletionStatus() {
        return this.completionStatus;
    }


    public long calculateTimeSpentOnTask() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        long timeElapsedSeconds=0;
        for (TimeLog timeLog: this.timeLogs) {
            String startStr = timeLog.getStartDateTime();
            String endStr = timeLog.getEndDateTime();
            if (startStr == null || endStr ==null)
                continue;
            else {
                LocalDateTime start = LocalDateTime.parse(startStr,dtf);
                LocalDateTime end = LocalDateTime.parse(endStr, dtf);
                timeElapsedSeconds += ChronoUnit.SECONDS.between(start,end);
            }
        }
        long hours = Math.abs(timeElapsedSeconds/3600);
        long minutes = Math.abs(hours*60 - timeElapsedSeconds/60);

        this.timeSpentOnTask = String.format("%dhrs %02dmins",hours,minutes);
        return timeElapsedSeconds;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskName='" + taskName + '\'' +
                ", notes='" + notes + '\'' +
                ", timeSpentOnTask='" + timeSpentOnTask + '\'' +
                ", timeLogs=" + timeLogs +
                '}';
    }
}
