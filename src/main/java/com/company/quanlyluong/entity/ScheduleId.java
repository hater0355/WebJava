package com.company.quanlyluong.entity;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class ScheduleId implements Serializable {
    private String employeeId;
    private LocalDate workDate;
}
