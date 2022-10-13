package com.bjit.salon.reservation.service.util;


import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class MethodsUtil {

    public static LocalTime minutesToLocalTime(long minutes){
        return LocalTime.MAX.plusMinutes(minutes);
    }
}
