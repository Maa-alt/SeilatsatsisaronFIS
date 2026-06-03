package com.example.demo1;

import javafx.beans.property.*;

public class SalaryRecord {
    private final StringProperty employeeName = new SimpleStringProperty();
    private final StringProperty month = new SimpleStringProperty();
    private final DoubleProperty basicSalary = new SimpleDoubleProperty();
    private final DoubleProperty workingHours = new SimpleDoubleProperty();
    private final DoubleProperty overtimeHours = new SimpleDoubleProperty();
    private final DoubleProperty grossSalary = new SimpleDoubleProperty();
    private final DoubleProperty tax = new SimpleDoubleProperty();
    private final DoubleProperty insurance = new SimpleDoubleProperty();
    private final DoubleProperty pension = new SimpleDoubleProperty();
    private final DoubleProperty otherDeductions = new SimpleDoubleProperty();
    private final DoubleProperty totalDeductions = new SimpleDoubleProperty();
    private final DoubleProperty netSalary = new SimpleDoubleProperty();

    public SalaryRecord(String employeeName, String month, double basicSalary,
                        double workingHours, double overtimeHours, double grossSalary,
                        double tax, double insurance, double pension,
                        double otherDeductions, double totalDeductions, double netSalary) {
        setEmployeeName(employeeName);
        setMonth(month);
        setBasicSalary(basicSalary);
        setWorkingHours(workingHours);
        setOvertimeHours(overtimeHours);
        setGrossSalary(grossSalary);
        setTax(tax);
        setInsurance(insurance);
        setPension(pension);
        setOtherDeductions(otherDeductions);
        setTotalDeductions(totalDeductions);
        setNetSalary(netSalary);
    }

    // Getters and setters
    public String getEmployeeName() { return employeeName.get(); }
    public StringProperty employeeNameProperty() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName.set(employeeName); }

    public String getMonth() { return month.get(); }
    public StringProperty monthProperty() { return month; }
    public void setMonth(String month) { this.month.set(month); }

    public double getBasicSalary() { return basicSalary.get(); }
    public DoubleProperty basicSalaryProperty() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary.set(basicSalary); }

    public double getWorkingHours() { return workingHours.get(); }
    public DoubleProperty workingHoursProperty() { return workingHours; }
    public void setWorkingHours(double workingHours) { this.workingHours.set(workingHours); }

    public double getOvertimeHours() { return overtimeHours.get(); }
    public DoubleProperty overtimeHoursProperty() { return overtimeHours; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours.set(overtimeHours); }

    public double getGrossSalary() { return grossSalary.get(); }
    public DoubleProperty grossSalaryProperty() { return grossSalary; }
    public void setGrossSalary(double grossSalary) { this.grossSalary.set(grossSalary); }

    public double getTax() { return tax.get(); }
    public DoubleProperty taxProperty() { return tax; }
    public void setTax(double tax) { this.tax.set(tax); }

    public double getInsurance() { return insurance.get(); }
    public DoubleProperty insuranceProperty() { return insurance; }
    public void setInsurance(double insurance) { this.insurance.set(insurance); }

    public double getPension() { return pension.get(); }
    public DoubleProperty pensionProperty() { return pension; }
    public void setPension(double pension) { this.pension.set(pension); }

    public double getOtherDeductions() { return otherDeductions.get(); }
    public DoubleProperty otherDeductionsProperty() { return otherDeductions; }
    public void setOtherDeductions(double otherDeductions) { this.otherDeductions.set(otherDeductions); }

    public double getTotalDeductions() { return totalDeductions.get(); }
    public DoubleProperty totalDeductionsProperty() { return totalDeductions; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions.set(totalDeductions); }

    public double getNetSalary() { return netSalary.get(); }
    public DoubleProperty netSalaryProperty() { return netSalary; }
    public void setNetSalary(double netSalary) { this.netSalary.set(netSalary); }
}