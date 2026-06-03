package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField; // Field for the user's username
    @FXML private TextField emailField; // Field for the user's email
    @FXML private PasswordField passwordField; // Field for the user's password
    @FXML private ComboBox<String> roleComboBox; // ComboBox for selecting user role
    @FXML private Label errorLabel; // Label for displaying error messages

    @FXML
    public void initialize() {
        // Populate the role ComboBox with options
        ObservableList<String> roles = FXCollections.observableArrayList("Admin", "Employee");
        roleComboBox.setItems(roles);
    }

    @FXML // Ensure this is public and annotated with @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || selectedRole == null) {
            errorLabel.setText("Please fill all fields and select a role.");
            errorLabel.setVisible(true);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE name = ? AND email = ? AND password = ? AND role = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.setString(4, selectedRole);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    loadMainDashboard(selectedRole);
                } else {
                    errorLabel.setText("Invalid credentials.");
                    errorLabel.setVisible(true);
                }
            }
        } catch (SQLException sqlEx) {
            errorLabel.setText("Database error: " + sqlEx.getMessage());
            errorLabel.setVisible(true);
            sqlEx.printStackTrace();
        } catch (Exception e) {
            errorLabel.setText("Login failed: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    private void loadMainDashboard(String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/demo1/MainDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            MainDashboardController mainDashboardController = loader.getController();
            mainDashboardController.setLoggedInUserRole(role);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setTitle("Main Dashboard");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Failed to load dashboard: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGoToRegister(ActionEvent event) {
        try {
            Parent signUpRoot = FXMLLoader.load(getClass().getResource("Signup.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene signUpScene = new Scene(signUpRoot);
            stage.setScene(signUpScene);
            stage.setTitle("Sign Up");
            stage.show();
        } catch (IOException e) {
            errorLabel.setText("Failed to load sign-up screen: " + e.getMessage());
            errorLabel.setVisible(true);
        }
    }
}