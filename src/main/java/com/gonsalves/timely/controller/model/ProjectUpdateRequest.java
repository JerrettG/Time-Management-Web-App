package com.gonsalves.timely.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {

    private String userId;
    private String projectName;
    private String updatedProjectName;
    private String totalTimeSpent;
    private Integer completionPercent;
}
