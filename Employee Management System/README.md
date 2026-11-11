# 👨‍💼 Employee Management System

> A console-based Java application to manage employee records and departments using a MySQL database.
> Built as **Task 4 (Medium)** of the **Auspify Technologies Software Development Internship**.

---

## 📌 Project Name

**Employee Management System (Java + MySQL)**

---

## 📝 Project Description

The Employee Management System helps an organization keep its employee and department data in one place. It replaces manual registers and spreadsheets with a database-driven application.

Users can create departments, register employees under a department, update or delete records, search employees in different ways, and generate reports such as total payroll, department-wise salary summary, highest paid employees, and recent joiners.

The project also focuses on **performance optimization**: database indexes make name and designation searches faster, results are shown **page by page** instead of loading everything at once, and the time taken by each query is displayed. Employees and departments are linked with a **foreign key**, so an employee can never belong to a department that does not exist.

---

## 🛠️ Technologies Used

| Component | Technology |
|---|---|
| Programming Language | Java (JDK 8 or above) |
| Database | MySQL |
| Database Connectivity | JDBC (MySQL Connector/J 8.0.33) |
| Database Tool | MySQL Workbench |
| Editor / IDE | Notepad / VS Code |
| Version Control | Git & GitHub |

---

## ✨ Features

- 🏢 **Department Management:** Add, view (with employee count), and delete departments
- ➕ **Register Employee:** Store ID, name, email, phone, designation, salary, joining date, and department
- 📋 **View Employees:** Paginated table (5 per page) with Next / Previous navigation
- ✏️ **Update Employee:** Modify employee details and shift to another department
- 🗑️ **Delete Employee:** Remove a record (with confirmation)
- 🔍 **Search Employees:**
  - By name
  - By department
  - By designation
  - By salary range
- 📊 **Reports:**
  - Employee summary (total, payroll, average, highest, lowest salary)
  - Department-wise report
  - Top 5 highest paid employees
  - Recent joiners
- ✅ **Validation:** Email format, 10-digit phone, valid date, positive salary, duplicate ID, duplicate email, and department checks
- ⚡ **Performance Optimization:** Indexes, pagination, prefix searches, query timing display
- 🔒 **Secure Queries:** Uses `PreparedStatement` to prevent SQL injection

---

## 🗄️ Database Structure

**Database:** `employee_db`

### Table: `departments`

| Column | Data Type | Description |
|---|---|---|
| dept_id | INT | Primary Key (auto increment) |
| dept_name | VARCHAR(50) | Department name (unique) |

### Table: `employees`

| Column | Data Type | Description |
|---|---|---|
| emp_id | INT | Primary Key |
| name | VARCHAR(100) | Employee name (indexed) |
| email | VARCHAR(100) | Unique email |
| phone | VARCHAR(15) | Phone number |
| designation | VARCHAR(50) | Job title (indexed) |
| salary | DECIMAL(10,2) | Monthly salary |
| join_date | DATE | Date of joining |
| dept_id | INT | Foreign Key → `departments.dept_id` |

> Both tables are created automatically when the program runs for the first time.

---

## ⚡ Performance Optimization

| Technique | Benefit |
|---|---|
| **Indexes** on `name` and `designation` | Faster searching on large data |
| **Prefix search** (`LIKE 'abc%'`) for name and designation | Lets MySQL use the index |
| **Pagination** (`LIMIT` / `OFFSET`) | Loads only 5 records at a time |
| **Single reused connection** | No repeated connection overhead |
| **Calculations done in SQL** (`SUM`, `AVG`, `GROUP BY`) | Less data moved into Java |
| **Query timing display** | Shows how fast each query ran |

---

## 📁 Project Structure

```
employee-management-system/
 ├── EmployeeApp.java
 ├── README.md
 └── screenshots/
      ├── home.png
      ├── add_employee.png
      ├── view_employees.png
      ├── search_employees.png
      ├── departments.png
      └── reports.png
```

---

## 📸 Screenshots

### Home Menu
![Home Menu](screenshots/home.png)

### Register Employee
![Register Employee](screenshots/add_employee.png)

### View Employees (Paginated)
![View Employees](screenshots/view_employees.png)

### Search Employees
![Search Employees](screenshots/search_employees.png)

### Department Management
![Departments](screenshots/departments.png)

### Reports
![Reports](screenshots/reports.png)

---

## ⚙️ Installation Steps

### 1. Install the prerequisites

- **JDK** (Java Development Kit). Check with:
  ```
  java -version
  ```
- **MySQL Server** and **MySQL Workbench**
- **MySQL Connector/J** jar file (`mysql-connector-j-8.0.33.jar`)

### 2. Clone the repository

```
git clone https://github.com/ansh-zero/employee-management-system.git
cd employee-management-system
```

*(Or simply download the project as a ZIP and extract it.)*

### 3. Add the MySQL Connector

[Click here to download mysql-connector-j-8.0.33.jar](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar)

Place the jar in the same folder as `EmployeeApp.java` (or keep it anywhere and use its full path when running).

### 4. Create the database

Open MySQL Workbench and run:

```sql
CREATE DATABASE employee_db;
```

### 5. Configure your MySQL credentials

Open `EmployeeApp.java` and update these lines with your own details:

```java
static final String URL = "jdbc:mysql://localhost:3306/employee_db";
static final String USER = "root";
static final String PASSWORD = "your_password";
```

---

## ▶️ How to Run the Project

Open Command Prompt / Terminal inside the project folder.

**Step 1: Compile**
```
javac EmployeeApp.java
```

**Step 2: Run**

Windows:
```
java -cp ".;mysql-connector-j-8.0.33.jar" EmployeeApp
```

Mac / Linux:
```
java -cp ".:mysql-connector-j-8.0.33.jar" EmployeeApp
```

On successful connection you will see:

```
MySQL se connect ho gaya!

===== Employee Management System =====
1.  Add Department
2.  View Departments
3.  Delete Department
4.  Register Employee
5.  View Employees
6.  Update Employee
7.  Delete Employee
8.  Search Employees
9.  Reports
10. Exit
```

> **Note:** MySQL Server must be running. If you get `Communications link failure`, start the MySQL service (`net start MySQL80` on Windows). Add at least one department before registering employees.

---

## 🧪 Testing Performed

- Added multiple departments and registered employees under them
- Tried to register an employee with a department that does not exist (blocked)
- Tried duplicate Employee ID and duplicate email (blocked)
- Tried invalid email, invalid phone, wrong date format, and zero salary (blocked)
- Viewed employees page by page using Next / Previous
- Updated an employee and moved them to another department
- Tried to delete a department that still has employees (blocked)
- Searched by name, department, designation, and salary range
- Generated all four reports
- Verified data in MySQL Workbench using `SELECT * FROM employees;`

---

## 🚀 Future Improvements

- Java Swing GUI
- Login system with admin and HR roles
- Attendance and leave management
- Export reports to CSV / PDF
- Salary slip generation

---

## 📚 What I Learned

- Designing related tables with primary keys, foreign keys, and unique constraints
- Writing `JOIN`, `LEFT JOIN`, `GROUP BY`, and aggregate queries for reports
- Improving performance with indexes and pagination
- Validating user input (email, phone, date, salary)
- Handling database constraint errors with clear messages
- Writing secure queries with `PreparedStatement`

---

## 📬 Contact

**GitHub:** [https://github.com/ansh-zero](https://github.com/ansh-zero)

Feel free to open an issue or reach out for suggestions and feedback.

---

## 🏢 About the Internship

This project was completed as part of the **Software Development Internship** at **Auspify Technologies**.

`#Auspify` `#AuspifyTechnologies` `#AuspifyInternship` `#AuspifyProjects`
