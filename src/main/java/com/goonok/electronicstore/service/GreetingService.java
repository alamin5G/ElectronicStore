package com.goonok.electronicstore.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class GreetingService {

    private static final Logger log = LoggerFactory.getLogger(GreetingService.class);

    public String greet(LocalTime time) {

        if (time.isAfter(LocalTime.of(4, 59)) && time.isBefore(LocalTime.of(12, 0))) {
            return "Good Morning";
        } else if (time.isAfter(LocalTime.of(11, 59)) && time.isBefore(LocalTime.of(17, 0))) {
            return "Good Afternoon";
        } else if (time.isAfter(LocalTime.of(16, 59))) {
            return "Good Evening";
        }else if(time.isBefore(LocalTime.of(5, 0))){
            return "Good Evening";
        }else {
            return "Welcome";
        }
    }
}
