package com.gonsalves.timely.unit;

import com.gonsalves.timely.service.model.TimeSpentConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeSpentConverterTest {

    @Test
    public void convertToString() {
        long timeSpentSeconds = 7671;
        String expected = "02:07:51";

        assertEquals(expected, TimeSpentConverter.convertToString(timeSpentSeconds));
    }

    @Test
    public void convertFromString() {
        String timeSpent = "02:07:51";
        long expected = 7671;

        assertEquals(expected, TimeSpentConverter.convertFromString(timeSpent));
    }
}
