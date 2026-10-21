package com.example.employee_manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@Controller 
public class EmployeeController {
    
    @GetMapping("/employees")
    public String requestMethodName(@RequestParam String param) {
        return new String();
    }
    
    
}
