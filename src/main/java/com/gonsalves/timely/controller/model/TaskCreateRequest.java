package com.gonsalves.timely.controller.model;

import com.gonsalves.timely.service.model.TimeLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    private String projectId;
    private String taskName;
    private String notes;
    private String status;

}
