package com.example.demo1;

public class SalaryCalculationResult {
    private int employeeId;
    private String month;
    private double basicSalary;
    private double grossSalary;
    private double totalDeductions;
    private double netSalary;

    public SalaryCalculationResult(int employeeId, String month, double basicSalary, double grossSalary, double totalDeductions, double netSalary) {
        this.employeeId = employeeId;
        this.month = month;
        this.basicSalary = basicSalary;
        this.grossSalary = grossSalary;
        this.totalDeductions = totalDeductions;
        this.netSalary = netSalary;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public String getMonth() {
        return month;
    }

    public double getBasicSalary() {
        return basicSalary;
    }

    public double getGrossSalary() {
        return grossSalary;
    }

    public double getTotalDeductions() {
        return totalDeductions;
    }

    public double getNetSalary() {
        return netSalary;
    }
}
