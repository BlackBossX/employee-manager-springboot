package com.example.employeemanager.model;

import java.math.BigDecimal;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Enter the employee's full name.")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String fullName;

    @NotBlank(message = "Enter an email address.")
    @Email(message = "Enter a valid email address.")
    @Size(max = 150)
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Enter a phone number.")
    @Pattern(regexp = "[0-9]{10}", message = "Enter a 10-digit phone number.")
    @Column(nullable = false, length = 10)
    private String phone;

    @NotBlank(message = "Enter a department.")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String department;

    @NotNull(message = "Enter a salary.")
    @Positive(message = "Salary must be greater than zero.")
    @Digits(integer = 10, fraction = 2, message = "Use at most 10 digits and 2 decimal places.")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;
}
