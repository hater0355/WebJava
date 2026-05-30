package com.company.quanlyluong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "account_username", length = 50)
    private String accountUsername;
    
    @Column(columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
