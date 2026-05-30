package com.company.quanlyluong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Table(name = "employees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Employee {
    @Id
    @Column(length = 20)
    private String id;
    
    @Column(length = 100)
    private String name;
    
    @Column(length = 100)
    private String department;
    
    @Column(length = 100)
    private String position;
    
    private Double baseSalary;
    
    @Column(name = "account_username", length = 50)
    private String accountUsername;
    
    @Column(name = "login_username", length = 50)
    private String loginUsername;
    
    @Column(length = 20)
    private String status = "PENDING";
    
    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;
    
    @Column(name = "gia_dinh", length = 255)
    private String giaDinh;
    
    @Column(name = "lien_lac_khan", length = 50)
    private String lienLacKhan;
    
    @Column(name = "ngay_vao_lam")
    private LocalDate ngayVaoLam = LocalDate.now();
    
    @Column(name = "nguoi_phu_thuoc")
    private Integer nguoiPhuThuoc = 0;

    public int getTuoi() {
        if (ngaySinh != null) {
            return Period.between(ngaySinh, LocalDate.now()).getYears();
        }
        return 0;
    }

    public int getThamNien() {
        if (ngayVaoLam != null) {
            return Period.between(ngayVaoLam, LocalDate.now()).getYears();
        }
        return 0;
    }
}
