import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class EmployeeApp {

    static final String URL = "jdbc:mysql://localhost:3306/employee_db";
    static final String USER = "root";
    static final String PASSWORD = "your_password";
    // ==================================

    static final String BASE_SELECT =
            "SELECT e.emp_id, e.name, e.email, e.phone, e.designation, "
          + "d.dept_name, e.salary, e.join_date "
          + "FROM employees e JOIN departments d ON e.dept_id = d.dept_id";

    static Connection conn;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("MySQL se connect ho gaya!");
            createTables();

            while (true) {
                System.out.println("\n===== Employee Management System =====");
                System.out.println("1.  Add Department");
                System.out.println("2.  View Departments");
                System.out.println("3.  Delete Department");
                System.out.println("4.  Register Employee");
                System.out.println("5.  View Employees");
                System.out.println("6.  Update Employee");
                System.out.println("7.  Delete Employee");
                System.out.println("8.  Search Employees");
                System.out.println("9.  Reports");
                System.out.println("10. Exit");
                System.out.print("Choice: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": addDepartment(); break;
                    case "2": viewDepartments(); break;
                    case "3": deleteDepartment(); break;
                    case "4": registerEmployee(); break;
                    case "5": viewEmployeesPaged(); break;
                    case "6": updateEmployee(); break;
                    case "7": deleteEmployee(); break;
                    case "8": searchEmployees(); break;
                    case "9": reportsMenu(); break;
                    case "10":
                        conn.close();
                        System.out.println("Bye!");
                        return;
                    default: System.out.println("Galat choice!");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- SETUP ----------
    static void createTables() throws SQLException {
        String departments = "CREATE TABLE IF NOT EXISTS departments ("
                + "dept_id INT AUTO_INCREMENT PRIMARY KEY, "
                + "dept_name VARCHAR(50) NOT NULL, "
                + "UNIQUE KEY uq_dept_name (dept_name))";

        // INDEX lagane se name / designation par search tez hoti hai (performance)
        String employees = "CREATE TABLE IF NOT EXISTS employees ("
                + "emp_id INT PRIMARY KEY, "
                + "name VARCHAR(100) NOT NULL, "
                + "email VARCHAR(100) NOT NULL, "
                + "phone VARCHAR(15), "
                + "designation VARCHAR(50), "
                + "salary DECIMAL(10,2) NOT NULL, "
                + "join_date DATE NOT NULL, "
                + "dept_id INT NOT NULL, "
                + "UNIQUE KEY uq_email (email), "
                + "INDEX idx_emp_name (name), "
                + "INDEX idx_designation (designation), "
                + "FOREIGN KEY (dept_id) REFERENCES departments(dept_id))";

        try (Statement st = conn.createStatement()) {
            st.execute(departments);
            st.execute(employees);
        }
    }

    // ---------- INPUT HELPERS ----------
    static int readInt(String msg) {
        System.out.print(msg);
        try {
            int v = Integer.parseInt(sc.nextLine().trim());
            if (v < 0) {
                System.out.println("Negative number nahi chalega!");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            System.out.println("Number likho!");
            return -1;
        }
    }

    static double readDouble(String msg) {
        System.out.print(msg);
        try {
            double v = Double.parseDouble(sc.nextLine().trim());
            if (v < 0) {
                System.out.println("Negative number nahi chalega!");
                return -1;
            }
            return v;
        } catch (NumberFormatException e) {
            System.out.println("Number likho!");
            return -1;
        }
    }

    // khali chhodo to aaj ki date; galat format par null
    static Date readDate(String msg) {
        System.out.print(msg);
        String in = sc.nextLine().trim();
        if (in.isEmpty()) return Date.valueOf(LocalDate.now());
        try {
            return Date.valueOf(in);
        } catch (IllegalArgumentException e) {
            System.out.println("Date ka format galat hai! YYYY-MM-DD likho (jaise 2026-11-07).");
            return null;
        }
    }

    static boolean isValidEmail(String email) {
        return email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    }

    static boolean isValidPhone(String phone) {
        return phone.matches("\\d{10}");
    }

    // ---------- DEPARTMENT MANAGEMENT ----------
    static void addDepartment() {
        System.out.print("Department ka naam: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Naam khali nahi ho sakta!");
            return;
        }
        String sql = "INSERT INTO departments (dept_name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
            System.out.println("Department add ho gaya!");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Ye department pehle se hai!");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewDepartments() {
        String sql = "SELECT d.dept_id, d.dept_name, COUNT(e.emp_id) AS employees "
                   + "FROM departments d LEFT JOIN employees e ON d.dept_id = e.dept_id "
                   + "GROUP BY d.dept_id, d.dept_name ORDER BY d.dept_id";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-8s %-25s %-10s%n", "Dept ID", "Department", "Employees");
            System.out.println("-------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-8d %-25s %-10d%n",
                        rs.getInt("dept_id"), rs.getString("dept_name"), rs.getInt("employees"));
            }
            if (!found) System.out.println("Abhi koi department nahi hai. Pehle add karo.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void deleteDepartment() {
        viewDepartments();
        int id = readInt("Kis Department ID ko delete karna hai? ");
        if (id < 0) return;
        String sql = "DELETE FROM departments WHERE dept_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("Department delete ho gaya!");
                resetDeptIdCounter();
            } else {
                System.out.println("Department ID nahi mili.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Is department mein employees hain. Pehle unhe delete ya shift karo.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // Delete ke baad ID counter ko max(ID)+1 par laata hai.
    // Matlab sabse aakhri department delete karne par uski ID agle naye department ko mil jaati hai.
    // (Beech ke gaps, jaise 1, 3 mein se 2 delete hona, waise hi rehte hain.)
    static void resetDeptIdCounter() {
        try (Statement st = conn.createStatement()) {
            st.execute("ALTER TABLE departments AUTO_INCREMENT = 1");
        } catch (SQLException e) {
            System.out.println("ID counter reset nahi ho paya: " + e.getMessage());
        }
    }

    static boolean deptExists(int id) throws SQLException {
        String sql = "SELECT 1 FROM departments WHERE dept_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ---------- EMPLOYEE MANAGEMENT ----------
    static void registerEmployee() {
        int id = readInt("Employee ID: ");
        if (id < 0) return;
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name khali nahi ho sakta!");
            return;
        }
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        if (!isValidEmail(email)) {
            System.out.println("Email sahi nahi hai!");
            return;
        }
        System.out.print("Phone (10 digit): ");
        String phone = sc.nextLine().trim();
        if (!isValidPhone(phone)) {
            System.out.println("Phone 10 digit ka number hona chahiye!");
            return;
        }
        System.out.print("Designation: ");
        String designation = sc.nextLine().trim();
        double salary = readDouble("Salary: ");
        if (salary < 0) return;
        if (salary == 0) {
            System.out.println("Salary 0 se zyada honi chahiye!");
            return;
        }
        Date joinDate = readDate("Joining Date (YYYY-MM-DD, khali = aaj): ");
        if (joinDate == null) return;

        viewDepartments();
        int deptId = readInt("Department ID: ");
        if (deptId < 0) return;

        String sql = "INSERT INTO employees (emp_id, name, email, phone, designation, salary, join_date, dept_id) "
                   + "VALUES (?,?,?,?,?,?,?,?)";
        try {
            if (!deptExists(deptId)) {
                System.out.println("Ye Department ID nahi mili. Pehle department add karo.");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, email);
                ps.setString(4, phone);
                ps.setString(5, designation);
                ps.setDouble(6, salary);
                ps.setDate(7, joinDate);
                ps.setInt(8, deptId);
                ps.executeUpdate();
                System.out.println("Employee register ho gaya!");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            handleIntegrityError(e);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void updateEmployee() {
        int id = readInt("Kis Employee ID ko update karna hai? ");
        if (id < 0) return;
        System.out.print("Naya Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name khali nahi ho sakta!");
            return;
        }
        System.out.print("Naya Email: ");
        String email = sc.nextLine().trim();
        if (!isValidEmail(email)) {
            System.out.println("Email sahi nahi hai!");
            return;
        }
        System.out.print("Naya Phone (10 digit): ");
        String phone = sc.nextLine().trim();
        if (!isValidPhone(phone)) {
            System.out.println("Phone 10 digit ka number hona chahiye!");
            return;
        }
        System.out.print("Nayi Designation: ");
        String designation = sc.nextLine().trim();
        double salary = readDouble("Nayi Salary: ");
        if (salary < 0) return;
        if (salary == 0) {
            System.out.println("Salary 0 se zyada honi chahiye!");
            return;
        }
        viewDepartments();
        int deptId = readInt("Naya Department ID: ");
        if (deptId < 0) return;

        String sql = "UPDATE employees SET name=?, email=?, phone=?, designation=?, salary=?, dept_id=? "
                   + "WHERE emp_id=?";
        try {
            if (!deptExists(deptId)) {
                System.out.println("Ye Department ID nahi mili.");
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, designation);
                ps.setDouble(5, salary);
                ps.setInt(6, deptId);
                ps.setInt(7, id);
                int rows = ps.executeUpdate();
                System.out.println(rows > 0 ? "Update ho gaya!" : "Employee ID nahi mili.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            handleIntegrityError(e);
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void deleteEmployee() {
        int id = readInt("Kis Employee ID ko delete karna hai? ");
        if (id < 0) return;
        System.out.print("Pakka delete karna hai? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Delete cancel kiya.");
            return;
        }
        String sql = "DELETE FROM employees WHERE emp_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "Delete ho gaya!" : "Employee ID nahi mili.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void handleIntegrityError(SQLIntegrityConstraintViolationException e) {
        String m = e.getMessage() == null ? "" : e.getMessage();
        if (m.contains("uq_email")) {
            System.out.println("Ye email kisi aur employee ke paas pehle se hai!");
        } else if (m.contains("PRIMARY")) {
            System.out.println("Ye Employee ID pehle se hai!");
        } else {
            System.out.println("Data sahi nahi hai: " + m);
        }
    }

    // ---------- VIEW (PAGINATION se tez) ----------
    static void viewEmployeesPaged() {
        final int size = 5;
        try {
            int total;
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM employees")) {
                rs.next();
                total = rs.getInt(1);
            }
            if (total == 0) {
                System.out.println("Abhi koi employee nahi hai.");
                return;
            }
            int pages = (total + size - 1) / size;
            int page = 0;

            while (true) {
                System.out.println("\nPage " + (page + 1) + " of " + pages
                        + " (total " + total + " employees)");
                queryEmployees(BASE_SELECT + " ORDER BY e.emp_id LIMIT ? OFFSET ?",
                        size, page * size);
                if (pages == 1) return;

                System.out.print("[N]ext / [P]revious / [Q]uit: ");
                String c = sc.nextLine().trim().toLowerCase();
                if (c.equals("n")) {
                    if (page < pages - 1) page++;
                    else System.out.println("Ye aakhri page hai.");
                } else if (c.equals("p")) {
                    if (page > 0) page--;
                    else System.out.println("Ye pehla page hai.");
                } else if (c.equals("q")) {
                    return;
                } else {
                    System.out.println("n, p ya q likho.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- SEARCH ----------
    static void searchEmployees() {
        System.out.println("\n--- Search Employees ---");
        System.out.println("1. Name se (shuru ka hissa)");
        System.out.println("2. Department se");
        System.out.println("3. Designation se (shuru ka hissa)");
        System.out.println("4. Salary range se");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();

        switch (c) {
            case "1":
                System.out.print("Naam ka shuru ka hissa likho: ");
                String n = sc.nextLine().trim();
                // prefix match: index use hota hai, isliye tez
                queryEmployees(BASE_SELECT + " WHERE e.name LIKE ? ORDER BY e.emp_id", n + "%");
                break;
            case "2":
                System.out.print("Department ka naam (ya uska part): ");
                String d = sc.nextLine().trim();
                queryEmployees(BASE_SELECT + " WHERE d.dept_name LIKE ? ORDER BY e.emp_id",
                        "%" + d + "%");
                break;
            case "3":
                System.out.print("Designation ka shuru ka hissa likho: ");
                String g = sc.nextLine().trim();
                queryEmployees(BASE_SELECT + " WHERE e.designation LIKE ? ORDER BY e.emp_id", g + "%");
                break;
            case "4":
                double min = readDouble("Minimum salary: ");
                if (min < 0) return;
                double max = readDouble("Maximum salary: ");
                if (max < 0) return;
                queryEmployees(BASE_SELECT + " WHERE e.salary BETWEEN ? AND ? ORDER BY e.salary",
                        min, max);
                break;
            default:
                System.out.println("Galat choice!");
        }
    }

    // ---------- REPORTS ----------
    static void reportsMenu() {
        System.out.println("\n--- Reports ---");
        System.out.println("1. Employee Summary");
        System.out.println("2. Department-wise Report");
        System.out.println("3. Top 5 Highest Paid");
        System.out.println("4. Recent Joiners (last 5)");
        System.out.print("Choice: ");
        String c = sc.nextLine().trim();
        switch (c) {
            case "1": summaryReport(); break;
            case "2": departmentReport(); break;
            case "3":
                System.out.println("\nTop 5 Highest Paid Employees:");
                queryEmployees(BASE_SELECT + " ORDER BY e.salary DESC LIMIT 5");
                break;
            case "4":
                System.out.println("\nRecent Joiners:");
                queryEmployees(BASE_SELECT + " ORDER BY e.join_date DESC, e.emp_id DESC LIMIT 5");
                break;
            default: System.out.println("Galat choice!");
        }
    }

    static void summaryReport() {
        String sql = "SELECT COUNT(*) AS total, "
                + "COALESCE(SUM(salary),0) AS payroll, "
                + "COALESCE(AVG(salary),0) AS avg_salary, "
                + "COALESCE(MAX(salary),0) AS max_salary, "
                + "COALESCE(MIN(salary),0) AS min_salary "
                + "FROM employees";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                System.out.println("\n========== EMPLOYEE SUMMARY ==========");
                System.out.println("Total employees  : " + rs.getLong("total"));
                System.out.printf("Total payroll    : Rs. %.2f%n", rs.getDouble("payroll"));
                System.out.printf("Average salary   : Rs. %.2f%n", rs.getDouble("avg_salary"));
                System.out.printf("Highest salary   : Rs. %.2f%n", rs.getDouble("max_salary"));
                System.out.printf("Lowest salary    : Rs. %.2f%n", rs.getDouble("min_salary"));
                System.out.println("======================================");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void departmentReport() {
        String sql = "SELECT d.dept_name, COUNT(e.emp_id) AS employees, "
                + "COALESCE(SUM(e.salary),0) AS payroll, "
                + "COALESCE(AVG(e.salary),0) AS avg_salary "
                + "FROM departments d LEFT JOIN employees e ON d.dept_id = e.dept_id "
                + "GROUP BY d.dept_id, d.dept_name ORDER BY payroll DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            System.out.printf("%-20s %-10s %-18s %-18s%n",
                    "Department", "Employees", "Total Payroll", "Average Salary");
            System.out.println("-------------------------------------------------------------------");
            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-20s %-10d Rs. %-14.2f Rs. %-14.2f%n",
                        rs.getString("dept_name"), rs.getInt("employees"),
                        rs.getDouble("payroll"), rs.getDouble("avg_salary"));
            }
            if (!found) System.out.println("Abhi koi department nahi hai.");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ---------- COMMON HELPERS ----------
    static void queryEmployees(String sql, Object... params) {
        long start = System.nanoTime();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                int count = printEmployees(rs);
                double ms = (System.nanoTime() - start) / 1_000_000.0;
                System.out.printf("(%d record(s), %.1f ms)%n", count, ms);
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static int printEmployees(ResultSet rs) throws SQLException {
        System.out.printf("%-6s %-20s %-26s %-12s %-18s %-14s %-12s %-12s%n",
                "ID", "Name", "Email", "Phone", "Designation", "Department", "Salary", "Joined");
        System.out.println("------------------------------------------------------------------------------------------------------------------------");
        int count = 0;
        while (rs.next()) {
            count++;
            System.out.printf("%-6d %-20s %-26s %-12s %-18s %-14s %-12.2f %-12s%n",
                    rs.getInt("emp_id"), rs.getString("name"), rs.getString("email"),
                    rs.getString("phone"), rs.getString("designation"),
                    rs.getString("dept_name"), rs.getDouble("salary"),
                    rs.getDate("join_date"));
        }
        if (count == 0) System.out.println("Koi employee nahi mila.");
        return count;
    }
}