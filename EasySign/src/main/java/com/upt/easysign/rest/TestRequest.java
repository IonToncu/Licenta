package com.upt.easysign.rest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/test")
@CrossOrigin
public class TestRequest {

    @GetMapping
    public String testRequest() {
        return "ura";
    }
}
