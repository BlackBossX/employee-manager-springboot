package com.example.employeemanager;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.example.employeemanager.model.Employee;
import com.example.employeemanager.repository.EmployeeRepository;
import com.example.employeemanager.service.EmployeeService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeManagerApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired EmployeeRepository repository;
    @Autowired EmployeeService service;

    @BeforeEach
    void clearEmployees() { repository.deleteAll(); }

    private Employee employee(String name, String email) {
        Employee employee = new Employee();
        employee.setFullName(name);
        employee.setEmail(email);
        employee.setPhone("0771234567");
        employee.setDepartment("Engineering");
        employee.setSalary(new BigDecimal("75000.00"));
        return employee;
    }

    private String json(String email) {
        return """
                {"fullName":"Nimal Perera","email":"%s","phone":"0771234567",
                 "department":"Engineering","salary":75000.00}
                """.formatted(email);
    }

    @Test
    void browserCreateEditUpdateSearchAndDelete() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/employees"));
        mvc.perform(get("/employees")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No employees yet")));
        mvc.perform(post("/employees").param("fullName", "Nimal Perera")
                .param("email", "nimal@example.com").param("phone", "0771234567")
                .param("department", "Engineering").param("salary", "75000.00"))
                .andExpect(redirectedUrl("/employees"));
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(get("/employees/{id}/edit", id)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("nimal@example.com")));
        mvc.perform(post("/employees/{id}/update", id).param("fullName", "Nimal Perera")
                .param("email", "nimal@example.com").param("phone", "0771234567")
                .param("department", "Finance").param("salary", "90000.00"))
                .andExpect(redirectedUrl("/employees"));
        assertThat(service.findById(id).getDepartment()).isEqualTo("Finance");
        assertThat(service.findById(id).getSalary()).isEqualByComparingTo("90000.00");
        mvc.perform(get("/employees").param("keyword", "NIMAL")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("nimal@example.com")));
        mvc.perform(get("/employees").param("keyword", "missing")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No employees match")));
        mvc.perform(post("/employees/{id}/delete", id)).andExpect(redirectedUrl("/employees"));
        assertThat(repository.count()).isZero();
    }

    @Test
    void browserValidationAndDuplicateEmailDoNotSave() throws Exception {
        mvc.perform(post("/employees")).andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("employee", "fullName", "email", "phone", "department", "salary"));
        mvc.perform(post("/employees").param("fullName", "Test").param("email", "invalid")
                .param("phone", "12345").param("department", "HR").param("salary", "0"))
                .andExpect(model().attributeHasFieldErrors("employee", "email", "phone", "salary"));
        assertThat(repository.count()).isZero();
        service.create(employee("Nimal", "nimal@example.com"));
        mvc.perform(post("/employees").param("fullName", "Another Employee")
                .param("email", "NIMAL@example.com").param("phone", "0771234567")
                .param("department", "HR").param("salary", "50000"))
                .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("employee", "email"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("This email is already registered")));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void apiCrudAndSearch() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json("nimal@example.com")))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"));
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(get("/api/employees/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nimal Perera"));
        mvc.perform(get("/api/employees").param("keyword", "nImAl"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/employees").param("keyword", "unknown"))
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(put("/api/employees/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(json("nimal@example.com").replace("Engineering", "Finance").replace("75000.00", "90000.00")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.department").value("Finance"))
                .andExpect(jsonPath("$.salary").value(90000.00));
        mvc.perform(delete("/api/employees/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/employees/{id}", id)).andExpect(status().isNotFound());
        assertThat(repository.count()).isZero();
    }

    @Test
    void apiRejectsInvalidAndDuplicateData() throws Exception {
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(json("bad-email").replace("0771234567", "12345").replace("75000.00", "0")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.phone").exists()).andExpect(jsonPath("$.salary").exists());
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        Employee first = service.create(employee("Nimal", "nimal@example.com"));
        Employee second = service.create(employee("Amali", "amali@example.com"));
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(json("NIMAL@example.com")))
                .andExpect(status().isConflict());
        mvc.perform(put("/api/employees/{id}", second.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(json(first.getEmail()))).andExpect(status().isConflict());
        assertThat(service.findById(second.getId()).getEmail()).isEqualTo("amali@example.com");
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void invalidEditKeepsEmployeeIdAndMissingRecordsReturn404() throws Exception {
        Employee saved = service.create(employee("Nimal", "nimal@example.com"));
        mvc.perform(post("/employees/{id}/update", saved.getId()).param("salary", "0"))
                .andExpect(status().isOk()).andExpect(model().attribute("employeeId", saved.getId()))
                .andExpect(model().attributeHasFieldErrors("employee", "salary"));
        assertThat(service.findById(saved.getId()).getSalary()).isEqualByComparingTo("75000");
        mvc.perform(get("/employees/999999/edit")).andExpect(status().isNotFound());
        mvc.perform(post("/employees/999999/delete")).andExpect(status().isNotFound());
        mvc.perform(put("/api/employees/999999").contentType(MediaType.APPLICATION_JSON)
                .content(json("missing@example.com"))).andExpect(status().isNotFound());
        mvc.perform(delete("/api/employees/999999")).andExpect(status().isNotFound());
    }

    @Test
    void clientSuppliedIdCannotOverwriteAnExistingEmployee() throws Exception {
        Employee saved = service.create(employee("Original", "original@example.com"));
        String payload = json("new@example.com").replace("{", "{\"id\":" + saved.getId() + ",");
        mvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        assertThat(repository.count()).isEqualTo(2);
        assertThat(service.findById(saved.getId()).getFullName()).isEqualTo("Original");
    }
}
