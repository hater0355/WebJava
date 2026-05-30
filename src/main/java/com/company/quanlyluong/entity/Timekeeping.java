package com.company.quanlyluong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timekeeping")
@IdClass(TimekeepingId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Timekeeping {
    @Id
    @Column(name = "employee_id", length = 20)
    private String employeeId;
    
    @Id
    @Column(name = "work_date")
    private LocalDate workDate;
    
    @Column(name = "check_in")
    private LocalTime checkIn;
    
    @Column(name = "check_out")
    private LocalTime checkOut;
}
