package com.example.demo1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainDashboardController {

    @FXML
    private Button btnEmployeeManagement;
    @FXML
    private Button btnPayrollReportsCharts;
    @FXML
    private Button btnPayslipGeneration;
    @FXML
    private Button btnSalaryCalculation;
    @FXML
    private Button btnLogout;
    @FXML
    private Label dashboardTitle;
    @FXML
    private VBox mainContent;
    @FXML
    private StackPane mainContentArea;
    @FXML
    private VBox sideBar;

    private String userRole;

    public void setLoggedInUserRole(String role) {
        this.userRole = role;
        System.out.println("User role set to: " + role);
        applyRolePermissions();
    }

    private void applyRolePermissions() {
        if ("Admin".equalsIgnoreCase(userRole)) {
            btnEmployeeManagement.setVisible(true);
            btnPayrollReportsCharts.setVisible(true);
            btnSalaryCalculation.setVisible(true);
            btnPayslipGeneration.setVisible(true);
            btnLogout.setVisible(true);
            dashboardTitle.setText("Admin Dashboard");
        } else if ("Employee".equalsIgnoreCase(userRole)) {
            btnEmployeeManagement.setVisible(false);
            btnPayrollReportsCharts.setVisible(false);
            btnSalaryCalculation.setVisible(false);
            btnPayslipGeneration.setVisible(true);
            btnLogout.setVisible(true);
            dashboardTitle.setText("Employee Dashboard");
        } else {
            btnEmployeeManagement.setVisible(false);
            btnPayrollReportsCharts.setVisible(false);
            btnSalaryCalculation.setVisible(false);
            btnPayslipGeneration.setVisible(false);
            btnLogout.setVisible(false);
            dashboardTitle.setText("Unknown Role");
        }
    }

    private void loadPage(String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/" + fxmlFile));
            Parent content = loader.load();

            // Pass role to controllers if needed
            if (fxmlFile.equals("salary.fxml")) {
                SalaryController salaryController = loader.getController();
                salaryController.setUserRole(userRole);
            }

            mainContentArea.getChildren().clear();
            mainContentArea.getChildren().add(content);
            dashboardTitle.setText(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleManagement(ActionEvent event) {
        loadPage("employee.fxml", "Employee Management");
    }

    @FXML
    void handlePayslipGeneration(ActionEvent event) {
        loadPage("payslip.fxml", "Payslip Generation");
    }

    @FXML
    void handleReports(ActionEvent event) {
        loadPage("payroll_reports.fxml", "Payroll Reports & Charts");
    }

    @FXML
    void handleSalaryCalculation(ActionEvent event) {
        loadPage("salary.fxml", "Salary Calculation");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/demo1/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}