package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

public class SalaryController {
    // Constants
    private static final double STANDARD_HOURS_PER_MONTH = 160;
    private static final double OVERTIME_RATE_MULTIPLIER = 1.5;
    private static final double INSURANCE_RATE = 0.05;
    private static final double PENSION_RATE = 0.07;

    // FXML Components
    @FXML private TextField txtName;
    @FXML private TextField txtBasicSalary;
    @FXML private TextField txtWorkingHours;
    @FXML private TextField txtOvertimeHours;
    @FXML private TextField txtOtherDeductions;
    @FXML private Label lblHourlyRate;
    @FXML private Label lblRegularPay;
    @FXML private Label lblOvertimePay;
    @FXML private Label lblGrossSalary;
    @FXML private Label lblTax;
    @FXML private Label lblInsurance;
    @FXML private Label lblPension;
    @FXML private Label lblOtherDeductions;
    @FXML private Label lblTotalDeductions;
    @FXML private Label lblNetSalary;
    @FXML private Label lblMonthlyTotal;
    @FXML private ComboBox<String> comboMonth;
    @FXML private Button btnCalculate;
    @FXML private Button btnSave;

    @FXML private TableView<SalaryRecord> salaryTable;
    @FXML private TableColumn<SalaryRecord, String> colName;
    @FXML private TableColumn<SalaryRecord, String> colMonth;
    @FXML private TableColumn<SalaryRecord, Double> colBasic;
    @FXML private TableColumn<SalaryRecord, Double> colWorkingHours;
    @FXML private TableColumn<SalaryRecord, Double> colOvertime;
    @FXML private TableColumn<SalaryRecord, Double> colGross;
    @FXML private TableColumn<SalaryRecord, Double> colTax;
    @FXML private TableColumn<SalaryRecord, Double> colInsurance;
    @FXML private TableColumn<SalaryRecord, Double> colPension;
    @FXML private TableColumn<SalaryRecord, Double> colOtherDeductions;
    @FXML private TableColumn<SalaryRecord, Double> colTotalDeductions;
    @FXML private TableColumn<SalaryRecord, Double> colNet;

    private ObservableList<SalaryRecord> salaryRecords = FXCollections.observableArrayList();
    private String userRole;

    @FXML
    public void initialize() {
        try {
            // Verify FXML injection first
            if (lblMonthlyTotal == null) {
                throw new IllegalStateException("FXML components not properly injected. Check FXML file.");
            }

            setupMonthComboBox();
            setupTableColumns();

            // Test database connection
            try (Connection conn = DatabaseConnection.getConnection()) {
                System.out.println("Database connection successful");
                loadSalaryData();
            } catch (SQLException e) {
                showAlert("Database Error", "Cannot connect to database: " + e.getMessage(), Alert.AlertType.ERROR);
                return;
            }

            btnSave.setDisable(true);

            if (userRole != null) {
                applyRolePermissions();
            }
        } catch (Exception e) {
            showAlert("Initialization Error", "Failed to initialize: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
        if (userRole != null) {
            applyRolePermissions();
        }
    }

    private void applyRolePermissions() {
        boolean isAdmin = "Admin".equalsIgnoreCase(userRole);

        txtBasicSalary.setDisable(!isAdmin);
        txtWorkingHours.setDisable(!isAdmin);
        txtOvertimeHours.setDisable(!isAdmin);
        txtOtherDeductions.setDisable(!isAdmin);
        btnSave.setDisable(!isAdmin);
        btnSave.setVisible(isAdmin);

        if (!isAdmin) {
            txtName.setText("Current Employee");
            txtName.setDisable(true);
        } else {
            txtName.setDisable(false);
        }
    }

    private void setupMonthComboBox() {
        comboMonth.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        ));
        comboMonth.getSelectionModel().select(LocalDate.now().getMonthValue() - 1);
    }

    private void setupTableColumns() {
        colName.setCellValueFactory(cellData -> cellData.getValue().employeeNameProperty());
        colMonth.setCellValueFactory(cellData -> cellData.getValue().monthProperty());
        colBasic.setCellValueFactory(cellData -> cellData.getValue().basicSalaryProperty().asObject());
        colWorkingHours.setCellValueFactory(cellData -> cellData.getValue().workingHoursProperty().asObject());
        colOvertime.setCellValueFactory(cellData -> cellData.getValue().overtimeHoursProperty().asObject());
        colGross.setCellValueFactory(cellData -> cellData.getValue().grossSalaryProperty().asObject());
        colTax.setCellValueFactory(cellData -> cellData.getValue().taxProperty().asObject());
        colInsurance.setCellValueFactory(cellData -> cellData.getValue().insuranceProperty().asObject());
        colPension.setCellValueFactory(cellData -> cellData.getValue().pensionProperty().asObject());
        colOtherDeductions.setCellValueFactory(cellData -> cellData.getValue().otherDeductionsProperty().asObject());
        colTotalDeductions.setCellValueFactory(cellData -> cellData.getValue().totalDeductionsProperty().asObject());
        colNet.setCellValueFactory(cellData -> cellData.getValue().netSalaryProperty().asObject());
    }

    @FXML
    private void handleCalculate() {
        try {
            if (!validateInputs()) return;

            double basicSalary = Double.parseDouble(txtBasicSalary.getText());
            double workingHours = Double.parseDouble(txtWorkingHours.getText());
            double overtimeHours = Double.parseDouble(txtOvertimeHours.getText());
            double otherDeductions = txtOtherDeductions.getText().isEmpty() ? 0 :
                    Double.parseDouble(txtOtherDeductions.getText());

            // Calculate all components
            CalculationResult result = calculateSalary(basicSalary, workingHours, overtimeHours, otherDeductions);

            // Update UI
            updateCalculationLabels(result);

            if ("Admin".equalsIgnoreCase(userRole)) {
                btnSave.setDisable(false);
            }
        } catch (Exception e) {
            showAlert("Calculation Error", "Error in calculation: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private CalculationResult calculateSalary(double basicSalary, double workingHours,
                                              double overtimeHours, double otherDeductions) {
        double hourlyRate = basicSalary / STANDARD_HOURS_PER_MONTH;
        double regularPay = Math.min(workingHours, STANDARD_HOURS_PER_MONTH) * hourlyRate;
        double overtimePay = overtimeHours * hourlyRate * OVERTIME_RATE_MULTIPLIER;
        double grossSalary = regularPay + overtimePay;
        double tax = calculateTax(grossSalary);
        double insurance = grossSalary * INSURANCE_RATE;
        double pension = grossSalary * PENSION_RATE;
        double totalDeductions = tax + insurance + pension + otherDeductions;
        double netSalary = grossSalary - totalDeductions;

        return new CalculationResult(
                hourlyRate, regularPay, overtimePay, grossSalary,
                tax, insurance, pension, otherDeductions,
                totalDeductions, netSalary
        );
    }

    private void updateCalculationLabels(CalculationResult result) {
        lblHourlyRate.setText(String.format("M%.2f/hour", result.hourlyRate));
        lblRegularPay.setText(String.format("M%.2f", result.regularPay));
        lblOvertimePay.setText(String.format("M%.2f", result.overtimePay));
        lblGrossSalary.setText(String.format("M%.2f", result.grossSalary));
        lblTax.setText(String.format("M%.2f", result.tax));
        lblInsurance.setText(String.format("M%.2f", result.insurance));
        lblPension.setText(String.format("M%.2f", result.pension));
        lblOtherDeductions.setText(String.format("M%.2f", result.otherDeductions));
        lblTotalDeductions.setText(String.format("M%.2f", result.totalDeductions));
        lblNetSalary.setText(String.format("M%.2f", result.netSalary));
    }

    private double calculateTax(double grossSalary) {
        if (grossSalary <= 5000) return grossSalary * 0.10;
        if (grossSalary <= 10000) return 500 + (grossSalary-5000)*0.15;
        if (grossSalary <= 20000) return 1250 + (grossSalary-10000)*0.20;
        if (grossSalary <= 50000) return 3250 + (grossSalary-20000)*0.25;
        return 10750 + (grossSalary-50000)*0.30;
    }

    @FXML
    private void handleSave() {
        try {
            if (!validateInputs()) return;

            SalaryRecord record = createSalaryRecordFromInputs();
            saveSalaryRecord(record);
            loadSalaryData();
            btnSave.setDisable(true);
            showAlert("Success", "Salary record saved successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Save Error", "Failed to save record: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private SalaryRecord createSalaryRecordFromInputs() {
        String name = txtName.getText().trim();
        String month = comboMonth.getValue();
        double basicSalary = Double.parseDouble(txtBasicSalary.getText());
        double workingHours = Double.parseDouble(txtWorkingHours.getText());
        double overtimeHours = Double.parseDouble(txtOvertimeHours.getText());
        double otherDeductions = txtOtherDeductions.getText().isEmpty() ? 0 :
                Double.parseDouble(txtOtherDeductions.getText());

        CalculationResult result = calculateSalary(basicSalary, workingHours, overtimeHours, otherDeductions);

        return new SalaryRecord(
                name, month, basicSalary, workingHours, overtimeHours,
                result.grossSalary, result.tax, result.insurance, result.pension,
                result.otherDeductions, result.totalDeductions, result.netSalary
        );
    }

    private boolean validateInputs() {
        try {
            if (txtName.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("Employee name is required");
            }
            if (comboMonth.getValue() == null) {
                throw new IllegalArgumentException("Please select a month");
            }

            validateNumericField(txtBasicSalary, "Basic salary", true);
            validateNumericField(txtWorkingHours, "Working hours", false);
            validateNumericField(txtOvertimeHours, "Overtime hours", false);

            if (!txtOtherDeductions.getText().isEmpty()) {
                validateNumericField(txtOtherDeductions, "Other deductions", false);
            }

            return true;
        } catch (IllegalArgumentException e) {
            showAlert("Invalid Input", e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void validateNumericField(TextField field, String fieldName, boolean mustBePositive) {
        try {
            double value = Double.parseDouble(field.getText());
            if (mustBePositive && value <= 0) {
                throw new IllegalArgumentException(fieldName + " must be positive");
            }
            if (value < 0) {
                throw new IllegalArgumentException(fieldName + " cannot be negative");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " value");
        }
    }

    private void saveSalaryRecord(SalaryRecord record) throws SQLException {
        String sql = "INSERT INTO salaries (employee_name, month, basic_salary, working_hours, " +
                "overtime_hours, gross_salary, tax, insurance, pension, other_deductions, " +
                "total_deductions, netsalary, date_paid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, record.getEmployeeName());
            pstmt.setString(2, record.getMonth());
            pstmt.setDouble(3, record.getBasicSalary());
            pstmt.setDouble(4, record.getWorkingHours());
            pstmt.setDouble(5, record.getOvertimeHours());
            pstmt.setDouble(6, record.getGrossSalary());
            pstmt.setDouble(7, record.getTax());
            pstmt.setDouble(8, record.getInsurance());
            pstmt.setDouble(9, record.getPension());
            pstmt.setDouble(10, record.getOtherDeductions());
            pstmt.setDouble(11, record.getTotalDeductions());
            pstmt.setDouble(12, record.getNetSalary());
            pstmt.setDate(13, Date.valueOf(LocalDate.now()));

            pstmt.executeUpdate();
        }
    }

    private void loadSalaryData() {
        salaryRecords.clear();
        String query = "SELECT * FROM salaries ORDER BY month, employee_name";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                SalaryRecord record = new SalaryRecord(
                        rs.getString("employee_name"),
                        rs.getString("month"),
                        rs.getDouble("basic_salary"),
                        rs.getDouble("working_hours"),
                        rs.getDouble("overtime_hours"),
                        rs.getDouble("gross_salary"),
                        rs.getDouble("tax"),
                        rs.getDouble("insurance"),
                        rs.getDouble("pension"),
                        rs.getDouble("other_deductions"),
                        rs.getDouble("total_deductions"),
                        rs.getDouble("netsalary")
                );
                salaryRecords.add(record);
            }

            salaryTable.setItems(salaryRecords);
            updateMonthlyTotal();

        } catch (SQLException e) {
            System.err.println("Database error loading salary data:");
            e.printStackTrace();
            throw new RuntimeException("Failed to load salary data", e);
        }
    }

    private void updateMonthlyTotal() {
        if (comboMonth.getValue() != null && lblMonthlyTotal != null) {
            double total = salaryRecords.stream()
                    .filter(r -> r.getMonth().equalsIgnoreCase(comboMonth.getValue()))
                    .mapToDouble(SalaryRecord::getNetSalary)
                    .sum();
            lblMonthlyTotal.setText(String.format("Total for %s: M%.2f", comboMonth.getValue(), total));
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class for calculation results
    private static class CalculationResult {
        final double hourlyRate;
        final double regularPay;
        final double overtimePay;
        final double grossSalary;
        final double tax;
        final double insurance;
        final double pension;
        final double otherDeductions;
        final double totalDeductions;
        final double netSalary;

        CalculationResult(double hourlyRate, double regularPay, double overtimePay,
                          double grossSalary, double tax, double insurance,
                          double pension, double otherDeductions,
                          double totalDeductions, double netSalary) {
            this.hourlyRate = hourlyRate;
            this.regularPay = regularPay;
            this.overtimePay = overtimePay;
            this.grossSalary = grossSalary;
            this.tax = tax;
            this.insurance = insurance;
            this.pension = pension;
            this.otherDeductions = otherDeductions;
            this.totalDeductions = totalDeductions;
            this.netSalary = netSalary;
        }
    }
}