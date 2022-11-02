package com.gonsalves.TimeManagementSystem.service.model;

import com.gonsalves.TimeManagementSystem.repository.model.TimeLogRecord;

import java.util.ArrayList;
import java.util.List;

public class TimeLogListConverter {


    public static List<TimeLogRecord> convertToRecord(List<TimeLog> timelogList) {
        List<TimeLogRecord> timeLogRecordList = new ArrayList<>();
        timelogList.forEach(timeLog -> timeLogRecordList.add(
                TimeLogRecord.builder()
                        .startDateTime(timeLog.getStartDateTime())
                        .endDateTime(timeLog.getEndDateTime())
                        .build()));
        return timeLogRecordList;
    }

    public static List<TimeLog> convertFromRecord(List<TimeLogRecord> timeLogRecords) {
        List<TimeLog> timeLogList = new ArrayList<>();
        timeLogRecords.forEach(timeLogRecord -> timeLogList.add(
                TimeLog.builder()
                        .startDateTime(timeLogRecord.getStartDateTime())
                        .endDateTime(timeLogRecord.getEndDateTime())
                        .build()));
        return timeLogList;
    }
}
