package com.upt.easysign.tempController;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TempController {
    @GetMapping("/")
    public String test(){
        return "Temporal";
    }
}
