package com.example.demo1;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart; // Import for XYChart
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class PayrollReportsController {

    // UI Components
    @FXML private BarChart<String, Number> barChart;
    @FXML private PieChart pieChart;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private TableView<MonthlySalary> reportTable;
    @FXML private TableColumn<MonthlySalary, String> colMonth;
    @FXML private TableColumn<MonthlySalary, String> colSalary;
    @FXML private Label totalPayrollLabel;
    @FXML private Button backButton;
    @FXML private Button btnExportCSV;

    // Data
    private final Map<String, Double> monthlySalaries = new LinkedHashMap<>();
    private Connection connection;

    @FXML
    public void initialize() {
        try {
            // Initialize database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/payrollsystem", "root", "123456");

            // Load data from MySQL
            loadSalaryData();

            // Setup UI components
            setupTableColumns();
            initializeCharts();

            // Update displays
            updateDisplays();

        } catch (Exception e) {
            showAlert("Initialization Error",
                    "Failed to initialize reports: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void loadSalaryData() {
        String query = "SELECT month, SUM(netSalary) AS total FROM salaries GROUP BY month ORDER BY month";
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String month = rs.getString("month");
                Double total = rs.getDouble("total");
                if (month != null && total != null) {
                    monthlySalaries.put(month, total);
                }
            }
        } catch (Exception e) {
            showAlert("Data Load Error", "Failed to load salary data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTableColumns() {
        colMonth.setCellValueFactory(new PropertyValueFactory<>("month"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("formattedSalary"));
    }

    private void initializeCharts() {
        barChart.setTitle("Monthly Salary Report");
        pieChart.setTitle("Salary Distribution");
        lineChart.setTitle("Salary Trend");
    }

    private void updateDisplays() {
        updateTable();
        updateCharts();
        updateTotalLabel();
    }

    private void updateTable() {
        ObservableList<MonthlySalary> data = FXCollections.observableArrayList();

        monthlySalaries.forEach((month, salary) ->
                data.add(new MonthlySalary(month, salary))
        );

        double total = monthlySalaries.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        data.add(new MonthlySalary("TOTAL", total));

        reportTable.setItems(data);
    }

    private void updateCharts() {
        // Bar Chart
        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Monthly Salary");
        monthlySalaries.forEach((month, salary) ->
                barSeries.getData().add(new XYChart.Data<>(month, salary))
        );
        barChart.getData().setAll(barSeries);

        // Pie Chart
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        monthlySalaries.forEach((month, salary) ->
                pieData.add(new PieChart.Data(
                        String.format("%s (M%.2f)", month, salary),
                        salary))
        );
        pieChart.setData(pieData);

        // Line Chart
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Salary Trend");
        monthlySalaries.forEach((month, salary) ->
                lineSeries.getData().add(new XYChart.Data<>(month, salary))
        );
        lineChart.getData().setAll(lineSeries);
    }

    private void updateTotalLabel() {
        double total = monthlySalaries.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        totalPayrollLabel.setText(String.format("Total Payroll: M%.2f", total));
    }

    @FXML
    private void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Salary Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Month,Salary\n");
                for (MonthlySalary entry : reportTable.getItems()) {
                    writer.write(String.format("%s,%s\n",
                            entry.getMonth(),
                            entry.getFormattedSalary()));
                }
                showAlert("Success", "Report exported successfully", Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                showAlert("Export Error", "Failed to export: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            if (connection != null) {
                connection.close();
            }
            Stage stage = (Stage) backButton.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to go back: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class MonthlySalary {
        private final StringProperty month;
        private final DoubleProperty salary;

        public MonthlySalary(String month, Double salary) {
            this.month = new SimpleStringProperty(month);
            this.salary = new SimpleDoubleProperty(salary);
        }

        public String getMonth() { return month.get(); }
        public double getSalary() { return salary.get(); }
        public String getFormattedSalary() { return String.format("M%.2f", salary.get()); }

        public StringProperty monthProperty() { return month; }
        public DoubleProperty salaryProperty() { return salary; }
    }
}