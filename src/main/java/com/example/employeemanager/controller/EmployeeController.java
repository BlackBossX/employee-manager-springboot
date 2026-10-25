package com.example.employeemanager.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.employeemanager.model.Employee;
import com.example.employeemanager.service.EmployeeService;
import com.example.employeemanager.service.DuplicateEmailException;

@Controller
public class EmployeeController {
    private final EmployeeService service;

    public EmployeeController(EmployeeService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() { return "redirect:/employees"; }

    @GetMapping("/employees")
    public String list(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("employee", new Employee());
        populateList(model, keyword);
        return "employees";
    }

    @PostMapping("/employees")
    public String create(@Valid @ModelAttribute("employee") Employee employee,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        if (!errors.hasErrors()) {
            try {
                service.create(employee);
                redirect.addFlashAttribute("success", "Employee saved successfully.");
                return "redirect:/employees";
            } catch (DuplicateEmailException exception) {
                errors.rejectValue("email", "duplicate", exception.getMessage());
            }
        }
        populateList(model, null);
        return "employees";
    }

    @GetMapping("/employees/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("employee", service.findById(id));
        model.addAttribute("employeeId", id);
        return "edit-employee";
    }

    @PostMapping("/employees/{id}/update")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("employee") Employee employee,
            BindingResult errors, Model model, RedirectAttributes redirect) {
        service.findById(id);
        if (!errors.hasErrors()) {
            try {
                service.update(id, employee);
                redirect.addFlashAttribute("success", "Employee updated successfully.");
                return "redirect:/employees";
            } catch (DuplicateEmailException exception) {
                errors.rejectValue("email", "duplicate", exception.getMessage());
            }
        }
        model.addAttribute("employeeId", id);
        return "edit-employee";
    }

    @PostMapping("/employees/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("success", "Employee deleted successfully.");
        return "redirect:/employees";
    }

    private void populateList(Model model, String keyword) {
        model.addAttribute("employees", service.findAll(keyword));
        model.addAttribute("keyword", keyword);
    }
}
