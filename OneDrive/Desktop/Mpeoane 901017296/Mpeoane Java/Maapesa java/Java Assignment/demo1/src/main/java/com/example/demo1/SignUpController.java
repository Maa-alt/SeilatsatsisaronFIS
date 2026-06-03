package com.example.demo1;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException; // Import IOException
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SignUpController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> comboRole;
    @FXML private Button btnRegister;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        // Populate the role ComboBox
        comboRole.setItems(FXCollections.observableArrayList("Admin", "Employee"));
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirm = txtConfirmPassword.getText();
        String role = comboRole.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || role == null) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Fill in all fields.");
            return;
        }

        if (!password.equals(confirm)) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Passwords do not match.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if email is already registered
            String checkQuery = "SELECT * FROM users WHERE email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, email);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    lblMessage.setStyle("-fx-text-fill: red;");
                    lblMessage.setText("Email already registered.");
                    return;
                }
            }

            // Save user to MySQL
            String insertQuery = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, name);
                insertStmt.setString(2, email);
                insertStmt.setString(3, password);
                insertStmt.setString(4, role);
                insertStmt.executeUpdate();
            }

            lblMessage.setStyle("-fx-text-fill: green;");
            lblMessage.setText("Registered successfully!");

            txtName.clear();
            txtEmail.clear();
            txtPassword.clear();
            txtConfirmPassword.clear();
            comboRole.getSelectionModel().clearSelection();
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin(ActionEvent event) {
        try {
            // Load the login FXML
            Parent loginRoot = FXMLLoader.load(getClass().getResource("Login.fxml")); // Adjust the path as necessary
            Stage stage = (Stage) btnRegister.getScene().getWindow(); // Get the current stage
            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("Failed to load login screen: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        } catch (Exception e) {
            lblMessage.setStyle("-fx-text-fill: red;");
            lblMessage.setText("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }
}