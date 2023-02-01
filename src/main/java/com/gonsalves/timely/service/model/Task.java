package com.gonsalves.timely.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        timeSpent = TimeSpentConverter.convertToString(timeSpentSeconds);
    }

    public void startTime() {
        TimeLog timeLog = new TimeLog();
        timeLog.setStartDateTime(LocalDateTime.now().toString());
        Optional.ofNullable(this.timeLogs)
                .orElse(new ArrayList<>()).add(timeLog);
    }
    public void stopTime() {
        Optional<TimeLog> timeLog = Optional.ofNullable(timeLogs)
                        .flatMap(timeLogs -> Optional.ofNullable(timeLogs.get(timeLogs.size()-1)));
        if (timeLog.isPresent()) {
            TimeLog log = timeLog.get();
            if (Objects.isNull(log.getEndDateTime())) {
                log.setEndDateTime(LocalDateTime.now().toString());
            } else {
                throw new IllegalStateException("Cannot stop time for not started, or already completed time log.");
            }
        }
    }



}
