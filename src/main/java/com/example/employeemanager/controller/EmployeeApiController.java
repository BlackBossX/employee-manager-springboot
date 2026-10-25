package com.example.employeemanager.controller;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.employeemanager.model.Employee;
import com.example.employeemanager.service.EmployeeService;

@RestController
@RequestMapping("/api/employees")
public class EmployeeApiController {
    private final EmployeeService service;

    public EmployeeApiController(EmployeeService service) { this.service = service; }

    @GetMapping
    public List<Employee> list(@RequestParam(required = false) String keyword) {
        return service.findAll(keyword);
    }

    @GetMapping("/{id}")
    public Employee get(@PathVariable Long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee input) {
        Employee employee = service.create(input);
        return ResponseEntity.created(URI.create("/api/employees/" + employee.getId())).body(employee);
    }

    @PutMapping("/{id}")
    public Employee update(@PathVariable Long id, @Valid @RequestBody Employee input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
