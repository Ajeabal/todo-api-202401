package com.study.todoapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class HealthCheckController {

    @GetMapping("/")
    @ResponseBody
    public String heathCheck() {
        log.debug("server is running");
        return "Server is Running....";
    }
}
