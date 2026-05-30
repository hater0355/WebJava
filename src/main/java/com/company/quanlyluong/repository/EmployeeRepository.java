package com.company.quanlyluong.repository;

import com.company.quanlyluong.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, String> {
    List<Employee> findByAccountUsernameAndStatus(String accountUsername, String status);
    List<Employee> findByAccountUsername(String accountUsername);
    Optional<Employee> findByLoginUsername(String loginUsername);
    List<Employee> findByDepartmentAndAccountUsernameAndStatus(String department, String accountUsername, String status);
}
