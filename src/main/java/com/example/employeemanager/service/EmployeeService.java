package com.example.employeemanager.service;

import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.employeemanager.model.Employee;
import com.example.employeemanager.repository.EmployeeRepository;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> findAll(String keyword) {
        return keyword == null || keyword.isBlank() ? repository.findAll()
                : repository.findByFullNameContainingIgnoreCase(keyword.trim());
    }

    public Employee findById(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    @Transactional
    public Employee create(Employee input) {
        Employee employee = new Employee();
        return saveDetails(employee, input);
    }

    @Transactional
    public Employee update(Long id, Employee input) {
        return saveDetails(findById(id), input);
    }

    private Employee saveDetails(Employee employee, Employee input) {
        String email = input.getEmail().trim().toLowerCase(Locale.ROOT);
        boolean duplicate = employee.getId() == null ? repository.existsByEmailIgnoreCase(email)
                : repository.existsByEmailIgnoreCaseAndIdNot(email, employee.getId());
        if (duplicate) throw new DuplicateEmailException();
        employee.setFullName(input.getFullName().trim());
        employee.setEmail(email);
        employee.setPhone(input.getPhone());
        employee.setDepartment(input.getDepartment().trim());
        employee.setSalary(input.getSalary());
        try {
            return repository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException exception) {
            // The database's unique constraint also covers concurrent requests.
            throw new DuplicateEmailException();
        }
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
