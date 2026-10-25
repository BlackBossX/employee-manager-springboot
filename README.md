# Employee Management System

Full-Stack Software Development, Lab Sheet 05 (COSC 32133 / BECS 32263).
Spring Boot 4, Java 21, MySQL, Spring Data JPA, Lombok, validation and Thymeleaf.

## Run locally

1. Install Java 21 and start MySQL 8 or XAMPP/MariaDB on port 3306.
2. In MySQL, run `CREATE DATABASE IF NOT EXISTS employee_db;`.
3. From this folder, copy `secrets.properties.example` to `secrets.properties` and set your MySQL username and password. This local file is ignored by Git. The default connection uses localhost:3306 and database employee_db. You can alternatively set DB_URL, DB_USERNAME and DB_PASSWORD environment variables; local secrets.properties takes precedence if present.
4. Run `bash mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`). If Maven is installed, `mvn spring-boot:run` also works.
5. Open <http://localhost:8080>. The root redirects to `/employees`.

The form requires a name, valid email, exactly 10 phone digits, department and positive salary with at most two decimal places. Email addresses are normalized and checked for duplicates. The directory supports name search, editing, deletion with confirmation, and a count of matching employees. Data is stored in MySQL and remains after restarting the app.

## Browser routes

| Method | Route | Action |
| --- | --- | --- |
| GET | `/employees?keyword=...` | Form and employee directory; optional name search |
| POST | `/employees` | Create from form fields |
| GET | `/employees/{id}/edit` | Edit form |
| POST | `/employees/{id}/update` | Update from form fields |
| POST | `/employees/{id}/delete` | Delete |

## Postman

Import `postman/employee-manager.postman_collection.json`. Start the application and run requests in collection order. The create request saves its returned ID as the collection variable `employeeId`. Start with a fresh email address if the example was already added. Duplicate validation is intentionally tested before deletion.

| Method | Route | Expected status |
| --- | --- | --- |
| GET | `/api/employees` | 200 |
| GET | `/api/employees?keyword=nimal` | 200 |
| GET | `/api/employees/{id}` | 200, or 404 if missing |
| POST | `/api/employees` | 201 with Location header |
| PUT | `/api/employees/{id}` | 200 |
| DELETE | `/api/employees/{id}` | 204 |

POST and PUT accept JSON:

```json
{
  "fullName": "Nimal Perera",
  "email": "nimal.perera@example.com",
  "phone": "0771234567",
  "department": "Engineering",
  "salary": 75000.00
}
```

Invalid JSON field values return 400 with field errors; duplicate email returns 409. Updates use the path ID. Client-supplied IDs cannot overwrite existing employees during creation.

## Verify MySQL and persistence

```sql
USE employee_db;
SHOW TABLES;
DESCRIBE employees;
SELECT * FROM employees;
```

Add four employees with distinct emails. Run SELECT after creating, editing and deleting, and compare the rows with the browser and Postman responses. Stop and restart the application, then confirm the remaining rows persist. These manual checks require a running MySQL server; automated H2 tests do not prove MySQL persistence.

## Automated tests

Run `bash mvnw test` or `mvn test`. The test profile uses an isolated H2 database in MySQL compatibility mode; normal application runs use MySQL. Tests cover rendered forms, create/edit/update/delete, search, validation, duplicate emails, missing IDs and prevention of create requests overwriting existing records.

## GitHub and submission

Use the lab's `feature/employee-crud` branch and pull-request workflow to publish and merge your changes. Check that neither `secrets.properties` nor `target/` is tracked before pushing. This code update does not publish or merge anything on GitHub.

For the lab submission, capture the browser operations, Postman requests/responses, MySQL changes and persistence after restart, and your GitHub pull request. Include the code, your name, student ID, date, course information and repository URL in `Labsheet05_{your student ID}.pdf` as required by the lab sheet.
