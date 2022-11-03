package com.bjit.salon.reservation.service.util;


import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// todo: (PR-Review) Change class name
@UtilityClass
public class MethodsUtil {
    // todo: (PR-Review) Change as Instant
    public static LocalTime minutesToLocalTime(long minutes){
        return LocalTime.MAX.plusMinutes(minutes);
    }
}
