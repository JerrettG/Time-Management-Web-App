package com.gonsalves.timely.service.model;

public enum StatusEnum {
    COMPLETE("Complete"),
    IN_PROGRESS("In progress"),
    PLANNED("Planned"),
    UNDER_REVIEW("Under review");

    private final String status;
    public String getStatus() {return status;}
    StatusEnum(String status) {
        this.status = status;
    }
}
