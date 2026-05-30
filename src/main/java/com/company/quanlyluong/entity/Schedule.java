package com.company.quanlyluong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@IdClass(ScheduleId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Schedule {
    @Id
    @Column(name = "employee_id", length = 20)
    private String employeeId;
    
    @Id
    @Column(name = "work_date")
    private LocalDate workDate;
    
    @Column(length = 100)
    private String shift;
}
