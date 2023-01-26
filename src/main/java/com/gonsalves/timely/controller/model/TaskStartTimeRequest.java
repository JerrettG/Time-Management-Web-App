package com.gonsalves.timely.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskStartTimeRequest {

    private String projectId;
    private String taskName;
}
