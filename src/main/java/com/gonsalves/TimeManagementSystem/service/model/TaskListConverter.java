package com.gonsalves.TimeManagementSystem.service.model;

import com.gonsalves.TimeManagementSystem.repository.model.TaskRecord;

import java.util.ArrayList;
import java.util.List;

public class TaskListConverter {

    public static List<TaskRecord> convertToRecord(List<Task> taskList) {
        List<TaskRecord> taskRecordList = new ArrayList<>();
        taskList.forEach(task -> taskRecordList.add(TaskRecord.builder()
                        .taskName(task.getTaskName())
                        .notes(task.getNotes())
                        .timeSpentOnTask(task.getTimeSpentOnTask())
                        .completionStatus(task.getCompletionStatus())
                        .timeLogRecords(TimeLogListConverter.convertToRecord(task.getTimeLogs()))
                .build()));
        return taskRecordList;
    }

    public static List<Task> convertFromRecord(List<TaskRecord> taskRecords) {
        List<Task> taskList = new ArrayList<>();
        taskRecords.forEach(taskRecord -> taskList.add(Task.builder()
                .taskName(taskRecord.getTaskName())
                .notes(taskRecord.getNotes())
                .timeSpentOnTask(taskRecord.getTimeSpentOnTask())
                .completionStatus(taskRecord.isCompletionStatus())
                .timeLogs(TimeLogListConverter.convertFromRecord(taskRecord.getTimeLogRecords()))
                .build()));
        return taskList;
    }
}
