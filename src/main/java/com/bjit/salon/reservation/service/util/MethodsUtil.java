package com.bjit.salon.reservation.service.util;


import lombok.experimental.UtilityClass;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class MethodsUtil {

    public static LocalTime minutesToLocalTime(long minutes){
        return LocalTime.MAX.plusMinutes(minutes);
    }

    public static String loadFileFromClassPath(String filePath) throws IOException {
        File file = ResourceUtils.getFile("classpath:"+filePath);
//        return new String(Files.readAllBytes(file.toPath()));

        return Files.readAllLines(file.toPath()).stream()
                .map(String::trim)
                .reduce(String::concat)
                .orElseThrow(FileNotFoundException::new);
    }
}
