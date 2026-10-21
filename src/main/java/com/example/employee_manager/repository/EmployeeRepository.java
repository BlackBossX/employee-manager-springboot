package com.example.employee_manager.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.employee_manager.model.Employee;
import org.springframework.stereotype.Repository;

@Repository 
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
}
