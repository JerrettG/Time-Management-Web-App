package com.gonsalves.timely.service.model;

public class TimeSpentConverter {

    public static long convertFromString(String timeSpent) {
        long hours = Long.parseLong(timeSpent.substring(0, 2)) * 3600;
        long minutes = Long.parseLong(timeSpent.substring(3,5)) * 60;
        long seconds = Long.parseLong(timeSpent.substring(6));
        return hours + minutes + seconds;
    }

    public static String convertToString(long timeSpentSeconds) {
        Long hours = Math.abs(timeSpentSeconds/3600);
        Long minutes = Math.abs(timeSpentSeconds % 3600 / 60);
        Long seconds = Math.abs(timeSpentSeconds % 3600 % 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
