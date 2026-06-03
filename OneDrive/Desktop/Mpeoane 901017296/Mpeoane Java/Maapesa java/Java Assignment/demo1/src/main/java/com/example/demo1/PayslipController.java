package com.example.demo1;

import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PayslipController {

    @FXML private Label lblName;
    @FXML private Label lblID;
    @FXML private Label lblDepartment;
    @FXML private Label lblBasic;
    @FXML private Label lblWorkingPay;
    @FXML private Label lblOvertimePay;
    @FXML private Label lblDeductions;
    @FXML private Label lblNetSalary;
    @FXML private VBox payslipVBox;
    @FXML private ComboBox<String> employeeComboBox;
    @FXML private TextField employeeIdField;
    @FXML private Button generateButton;
    @FXML private Button printButton;

    private Connection connection;
    private String userRole;
    private String currentEmployeeId;
    private final List<String> employeeNames = new ArrayList<>();
    private final List<String> employeeIds = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollsystem", "root", "123456");
            printButton.setDisable(true);
            generateButton.setDisable(true);
            setUserInfo("Admin", null);
        } catch (Exception e) {
            showAlert("Error", "Initialization failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setUserInfo(String userRole, String currentEmployeeId) {
        this.userRole = userRole;
        this.currentEmployeeId = currentEmployeeId;

        if ("Employee".equalsIgnoreCase(userRole)) {
            employeeIdField.setText(currentEmployeeId);
            employeeIdField.setDisable(true);
            loadCurrentEmployee();
        } else {
            populateEmployeeComboBox();
        }
        generateButton.setDisable(false);
    }

    private void loadCurrentEmployee() {
        try {
            String query = "SELECT name FROM employees WHERE employee_id = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, currentEmployeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String employeeName = rs.getString("name");
                employeeComboBox.getItems().add(employeeName);
                employeeComboBox.setValue(employeeName);
                employeeComboBox.setDisable(true);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load employee data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void populateEmployeeComboBox() {
        try {
            employeeComboBox.getItems().clear();
            employeeNames.clear();
            employeeIds.clear();

            String query = "SELECT name, employee_id FROM employees";
            PreparedStatement stmt = connection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String id = rs.getString("employee_id");
                if (name != null && !name.isEmpty() && id != null && !id.isEmpty()) {
                    employeeNames.add(name);
                    employeeIds.add(id);
                    employeeComboBox.getItems().add(name);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load employees: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGeneratePayslip() {
        try {
            String selectedName = employeeComboBox.getValue();
            String enteredId = employeeIdField.getText().trim();

            if (selectedName == null || selectedName.isEmpty()) {
                showAlert("Error", "Please select an employee", Alert.AlertType.ERROR);
                return;
            }
            if (enteredId.isEmpty()) {
                showAlert("Error", "Please enter the Employee ID", Alert.AlertType.ERROR);
                return;
            }

            // Get employee data including overtime hours
            String employeeQuery = "SELECT name, department, basic_salary, working_hours, overtime_hours FROM employees WHERE employee_id = ?";
            PreparedStatement employeeStmt = connection.prepareStatement(employeeQuery);
            employeeStmt.setString(1, enteredId);
            ResultSet employeeRs = employeeStmt.executeQuery();

            if (!employeeRs.next()) {
                showAlert("Error", "Employee ID not found", Alert.AlertType.ERROR);
                return;
            }

            // Verify name matches
            String nameFromEmployee = employeeRs.getString("name");
            if (!selectedName.equals(nameFromEmployee)) {
                showAlert("Error", "Employee ID does not match selected name", Alert.AlertType.ERROR);
                return;
            }

            // Calculate salary components
            double basicSalary = employeeRs.getDouble("basic_salary");
            int workingHours = employeeRs.getInt("working_hours");
            int overtimeHours = employeeRs.getInt("overtime_hours");
            String department = employeeRs.getString("department");

            // Calculate working days pay (assuming 8 hours/day)
            int workingDays = workingHours / 8;
            double dailyRate = basicSalary / 22; // Standard 22 working days/month
            double workingDaysPay = dailyRate * workingDays;

            // Calculate overtime pay (1.5x hourly rate)
            double hourlyRate = basicSalary / (22 * 8); // 22 days * 8 hours
            double overtimePay = hourlyRate * 1.5 * overtimeHours;

            // Calculate deductions (10% tax + fixed insurance)
            double tax = basicSalary * 0.10;
            double insurance = 200.00;
            double deductions = tax + insurance;

            double netSalary = basicSalary + workingDaysPay + overtimePay - deductions;

            // Create or update payslip record
            String payslipQuery = "SELECT * FROM payslips WHERE employee_id = ?";
            PreparedStatement payslipStmt = connection.prepareStatement(payslipQuery);
            payslipStmt.setString(1, enteredId);
            ResultSet payslipRs = payslipStmt.executeQuery();

            if (payslipRs.next()) {
                // Update existing payslip
                String updateSql = "UPDATE payslips SET " +
                        "employee_name = ?, " +
                        "department = ?, " +
                        "basic_salary = ?, " +
                        "working_days_pay = ?, " +
                        "overtime_pay = ?, " +
                        "deductions = ?, " +
                        "netsalary = ?, " +
                        "generated_on = ? " +
                        "WHERE employee_id = ?";

                PreparedStatement updateStmt = connection.prepareStatement(updateSql);
                updateStmt.setString(1, nameFromEmployee);
                updateStmt.setString(2, department);
                updateStmt.setDouble(3, basicSalary);
                updateStmt.setDouble(4, workingDaysPay);
                updateStmt.setDouble(5, overtimePay);
                updateStmt.setDouble(6, deductions);
                updateStmt.setDouble(7, netSalary);
                updateStmt.setString(8, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                updateStmt.setString(9, enteredId);
                updateStmt.executeUpdate();
            } else {
                // Insert new payslip
                String insertSql = "INSERT INTO payslips (" +
                        "employee_id, employee_name, department, " +
                        "basic_salary, working_days_pay, overtime_pay, " +
                        "deductions, netsalary, generated_on) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement insertStmt = connection.prepareStatement(insertSql);
                insertStmt.setString(1, enteredId);
                insertStmt.setString(2, nameFromEmployee);
                insertStmt.setString(3, department);
                insertStmt.setDouble(4, basicSalary);
                insertStmt.setDouble(5, workingDaysPay);
                insertStmt.setDouble(6, overtimePay);
                insertStmt.setDouble(7, deductions);
                insertStmt.setDouble(8, netSalary);
                insertStmt.setString(9, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                insertStmt.executeUpdate();
            }

            // Display the payslip
            lblName.setText(nameFromEmployee);
            lblID.setText(enteredId);
            lblDepartment.setText(department);
            lblBasic.setText(String.format("M%.2f", basicSalary));
            lblWorkingPay.setText(String.format("M%.2f", workingDaysPay));
            lblOvertimePay.setText(String.format("M%.2f", overtimePay));
            lblDeductions.setText(String.format("M%.2f", deductions));
            lblNetSalary.setText(String.format("M%.2f", netSalary));

            printButton.setDisable(false);

        } catch (Exception e) {
            showAlert("Error", "Failed to generate payslip: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePrint() {
        try {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(payslipVBox.getScene().getWindow())) {
                boolean success = job.printPage(payslipVBox);
                if (success) {
                    job.endJob();
                    showAlert("Success", "Payslip printed successfully", Alert.AlertType.INFORMATION);
                }
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to print: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}