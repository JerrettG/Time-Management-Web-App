package com.gonsalves.timely.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    private String userId;
    private String projectName;
    private String projectId;
    private String creationDate;
    private String totalTimeSpent;
    private Integer completionPercent;

}