package com.company.quanlyluong.repository;

import com.company.quanlyluong.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    List<Department> findByAccountUsername(String accountUsername);
    Optional<Department> findByNameAndAccountUsername(String name, String accountUsername);
}
