package com.company.quanlyluong.dto;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SalaryRecord {
    private String employeeName;
    private String period;
    private int regularDays;
    private int overtimeDays;
    private double penalty;
    private double insurance;
    private double tax;
    private double finalSalary;
    private int forgotCheckOutCount;
    private double totalWorkedHours;
}