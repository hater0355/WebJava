package com.company.quanlyluong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rewards_penalties")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RewardPenalty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "employee_id", length = 50)
    private String employeeId;
    
    private Integer month;
    private Integer year;
    private Double amount;
    
    @Column(columnDefinition = "TEXT")
    private String reason;
    
    @Column(name = "recorded_by", length = 50)
    private String recordedBy;
}
