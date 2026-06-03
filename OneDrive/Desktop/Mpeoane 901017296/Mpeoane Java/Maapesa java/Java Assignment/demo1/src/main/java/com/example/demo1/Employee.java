package com.example.demo1;

import javafx.beans.property.*;

public class Employee {
    private final StringProperty employeeId;
    private final StringProperty name;
    private final StringProperty department;
    private final StringProperty position;
    private final DoubleProperty basicSalary;
    private final IntegerProperty workingHours;

    public Employee(String employeeId, String name, String department,
                    String position, double basicSalary, int workingHours) {
        this.employeeId = new SimpleStringProperty(employeeId);
        this.name = new SimpleStringProperty(name);
        this.department = new SimpleStringProperty(department);
        this.position = new SimpleStringProperty(position);
        this.basicSalary = new SimpleDoubleProperty(basicSalary);
        this.workingHours = new SimpleIntegerProperty(workingHours);
    }

    // Getters and setters for employeeId
    public String getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(String id) { this.employeeId.set(id); }
    public StringProperty employeeIdProperty() { return employeeId; }

    // Getters and setters for name
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    // Getters and setters for department
    public String getDepartment() { return department.get(); }
    public void setDepartment(String dept) { this.department.set(dept); }
    public StringProperty departmentProperty() { return department; }

    // Getters and setters for position
    public String getPosition() { return position.get(); }
    public void setPosition(String pos) { this.position.set(pos); }
    public StringProperty positionProperty() { return position; }

    // Getters and setters for basicSalary
    public double getBasicSalary() { return basicSalary.get(); }
    public void setBasicSalary(double salary) { this.basicSalary.set(salary); }
    public DoubleProperty basicSalaryProperty() { return basicSalary; }

    // Getters and setters for workingHours
    public int getWorkingHours() { return workingHours.get(); }
    public void setWorkingHours(int hours) { this.workingHours.set(hours); }
    public IntegerProperty workingHoursProperty() { return workingHours; }

    @Override
    public String toString() {
        return name.get(); // For display in ComboBox
    }
}