package com.wurmonline.server.villages;

import com.wurmonline.server.TimeConstants;
import org.junit.Test;

import static com.wurmonline.server.villages.GuardPlanMethods.addGraceTimeRemaining;
import static org.junit.Assert.assertTrue;

public class AddGraceTimeMessage extends GuardPlanMethodsTest {
    @Test
    public void testDaysRemaining() {
        for (int i = 1; i < 100; i++) {
            StringBuilder sb = new StringBuilder();
            addGraceTimeRemaining(sb, TimeConstants.DAY_MILLIS * i);

            String message = sb.toString();
            assertTrue("Result was " + message, message.endsWith("start paying upkeep in " + (i == 1 ? "1 day" : i + " days") +  ".\"}"));
        }
    }

    @Test
    public void testDayHourThreshold() {
        StringBuilder sb = new StringBuilder();
        addGraceTimeRemaining(sb, TimeConstants.DAY_MILLIS - 1);

        String message = sb.toString();
        assertTrue("Result was " + message, message.endsWith("start paying upkeep in 23 hours.\"}"));
    }

    @Test
    public void testHoursRemaining() {
        for (int i = 1; i < 24; i++) {
            StringBuilder sb = new StringBuilder();
            addGraceTimeRemaining(sb, TimeConstants.HOUR_MILLIS * i);

            String message = sb.toString();
            assertTrue("Result was " + message, message.endsWith("start paying upkeep in " + (i == 1 ? "1 hour" : i + " hours") +  ".\"}"));
        }
    }

    @Test
    public void testLessThanOneHourRemaining() {
        StringBuilder sb = new StringBuilder();
        addGraceTimeRemaining(sb, TimeConstants.HOUR_MILLIS - 1);

        String message = sb.toString();
        assertTrue("Result was " + message, message.endsWith("start paying upkeep in less than an hour.\"}"));
    }
}
