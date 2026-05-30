package com.company.quanlyluong.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceRecord {
    private String employeeId;
    private String employeeName;
    private String status;
    private String reason;
}
