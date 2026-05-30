package com.company.quanlyluong.entity;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TimekeepingId implements Serializable {
    private String employeeId;
    private LocalDate workDate;
}