package com.example.employeemanager.service;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("This email is already registered");
    }
}
