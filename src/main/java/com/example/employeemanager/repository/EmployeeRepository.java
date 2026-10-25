package com.example.employeemanager.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.employeemanager.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);
}
