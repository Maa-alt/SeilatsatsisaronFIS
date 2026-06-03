package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class EmployeeController {

    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colDepartment;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, Double> colBasicSalary;
    @FXML private TableColumn<Employee, Integer> colWorkingHours;

    @FXML private TextField txtEmployeeId;
    @FXML private TextField txtName;
    @FXML private TextField txtDepartment;
    @FXML private TextField txtPosition;
    @FXML private TextField txtBasicSalary;
    @FXML private TextField txtWorkingHours;

    private final ObservableList<Employee> employeeList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> data.getValue().employeeIdProperty());
        colName.setCellValueFactory(data -> data.getValue().nameProperty());
        colDepartment.setCellValueFactory(data -> data.getValue().departmentProperty());
        colPosition.setCellValueFactory(data -> data.getValue().positionProperty());
        colBasicSalary.setCellValueFactory(data -> data.getValue().basicSalaryProperty().asObject());
        colWorkingHours.setCellValueFactory(data -> data.getValue().workingHoursProperty().asObject());

        loadEmployeesFromDatabase();

        employeeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateForm(newSelection);
            }
        });
    }

    private void loadEmployeesFromDatabase() {
        employeeList.clear();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollsystem", "root", "123456")) {
            String query = "SELECT * FROM employees";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                employeeList.add(new Employee(
                        rs.getString("employee_id"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("position"),
                        rs.getDouble("basic_salary"),
                        rs.getInt("working_hours")
                ));
            }

            employeeTable.setItems(employeeList);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database error: " + e.getMessage());
        }
    }

    private void populateForm(Employee emp) {
        txtEmployeeId.setText(emp.getEmployeeId());
        txtName.setText(emp.getName());
        txtDepartment.setText(emp.getDepartment());
        txtPosition.setText(emp.getPosition());
        txtBasicSalary.setText(String.valueOf(emp.getBasicSalary()));
        txtWorkingHours.setText(String.valueOf(emp.getWorkingHours()));
    }

    @FXML
    private void handleAdd() {
        if (validateInputs()) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollsystem", "root", "123456")) {
                String insert = "INSERT INTO employees (employee_id, name, department, position, basic_salary, working_hours) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(insert);

                stmt.setString(1, txtEmployeeId.getText());
                stmt.setString(2, txtName.getText());
                stmt.setString(3, txtDepartment.getText());
                stmt.setString(4, txtPosition.getText());
                stmt.setDouble(5, Double.parseDouble(txtBasicSalary.getText()));
                stmt.setInt(6, Integer.parseInt(txtWorkingHours.getText()));

                stmt.executeUpdate();
                loadEmployeesFromDatabase();
                clearForm();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Database error: " + e.getMessage());
            } catch (NumberFormatException e) {
                showAlert("Please enter valid numbers for Salary and Working Hours.");
            }
        }
    }

    @FXML
    private void handleUpdate() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (validateInputs()) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollsystem", "root", "123456")) {
                    String update = "UPDATE employees SET name=?, department=?, position=?, basic_salary=?, working_hours=? WHERE employee_id=?";
                    PreparedStatement stmt = conn.prepareStatement(update);

                    stmt.setString(1, txtName.getText());
                    stmt.setString(2, txtDepartment.getText());
                    stmt.setString(3, txtPosition.getText());
                    stmt.setDouble(4, Double.parseDouble(txtBasicSalary.getText()));
                    stmt.setInt(5, Integer.parseInt(txtWorkingHours.getText()));
                    stmt.setString(6, selected.getEmployeeId());

                    stmt.executeUpdate();
                    loadEmployeesFromDatabase();
                    clearForm();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database error: " + e.getMessage());
                } catch (NumberFormatException e) {
                    showAlert("Please enter valid numbers for Salary and Working Hours.");
                }
            }
        } else {
            showAlert("Please select an employee to update.");
        }
    }

    @FXML
    private void handleDelete() {
        Employee selected = employeeTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (showConfirmation("Are you sure you want to delete this employee?")) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/vehiclerentalsystem", "root", "123456")) {
                    String delete = "DELETE FROM employees WHERE employee_id=?";
                    PreparedStatement stmt = conn.prepareStatement(delete);

                    stmt.setString(1, selected.getEmployeeId());
                    stmt.executeUpdate();
                    loadEmployeesFromDatabase();
                    clearForm();
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert("Database error: " + e.getMessage());
                }
            }
        } else {
            showAlert("Please select an employee to delete.");
        }
    }

    private boolean showConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }

    private void clearForm() {
        txtEmployeeId.clear();
        txtName.clear();
        txtDepartment.clear();
        txtPosition.clear();
        txtBasicSalary.clear();
        txtWorkingHours.clear();
    }

    private boolean validateInputs() {
        if (txtEmployeeId.getText().isEmpty() || txtName.getText().isEmpty() || txtDepartment.getText().isEmpty() ||
                txtPosition.getText().isEmpty() || txtBasicSalary.getText().isEmpty() || txtWorkingHours.getText().isEmpty()) {
            showAlert("Please fill in all fields.");
            return false;
        }
        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}