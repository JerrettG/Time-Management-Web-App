package com.gonsalves.timely.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String projectId;
    private String taskName;
    private String notes;
    private String timeSpent;
    private List<TimeLog> timeLogs;
    private String status;

    public void calculateAndUpdateTimeSpent() {
        Long timeSpentSeconds = Optional.ofNullable(timeLogs)
                .orElseThrow(() -> new IllegalStateException("Cannot calculate time spent when time logs is null"))
                .stream()
                .reduce(
                        0L, (partialTimeSpentResult, timeLog) -> {
                            Optional<String> startDateTime = Optional.ofNullable(timeLog.getStartDateTime());
                            Optional<String> endDateTime = Optional.ofNullable(timeLog.getEndDateTime());
                            if (startDateTime.isEmpty() || endDateTime.isEmpty())
                                return partialTimeSpentResult;
                            return partialTimeSpentResult + ChronoUnit.SECONDS.between(LocalDateTime.parse(startDateTime.get()), LocalDateTime.parse(endDateTime.get()));
                        }, Long::sum);
        Long hours = Math.abs(timeSpentSeconds/3600);
        Long minutes = Math.abs(timeSpentSeconds % 3600 / 60);
        Long seconds = Math.abs(timeSpentSeconds % 3600 % 60);
        timeSpent = String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void startTime() {
        TimeLog timeLog = new TimeLog();
        timeLog.setStartDateTime(LocalDateTime.now().toString());
        Optional.ofNullable(this.timeLogs)
                .orElse(new ArrayList<>()).add(timeLog);
    }
    public void stopTime() {
        TimeLog timeLog = Optional.ofNullable(this.timeLogs)
                .flatMap(timeLogs -> Optional.ofNullable(timeLogs.get(timeLogs.size()-1)))
                .orElseThrow(() -> new IllegalStateException("Cannot stop task time for not started task"));
        timeLog.setEndDateTime(LocalDateTime.now().toString());
    }



}
