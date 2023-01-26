package com.gonsalves.timely.controller.model;

import com.gonsalves.timely.service.model.TimeLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {

    private String projectId;
    private String taskName;
    private String updatedTaskName;
    private String notes;
    private String timeSpent;
    private List<TimeLog> timeLogs;
    private String status;

}
