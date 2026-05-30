package com.company.quanlyluong.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @Column(length = 50)
    private String username;
    
    private String password;
    
    @Column(length = 20)
    private String role; // ROLE_ADMIN, ROLE_EMPLOYEE
    
    @Column(name = "company_code", length = 20)
    private String companyCode;
    
    @Column(name = "full_name", length = 100)
    private String fullName;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
}
