package com.company.quanlyluong.repository;

import com.company.quanlyluong.entity.RewardPenalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RewardPenaltyRepository extends JpaRepository<RewardPenalty, Integer> {
    List<RewardPenalty> findByEmployeeIdAndMonthAndYear(String employeeId, Integer month, Integer year);
    
    @Query("SELECT SUM(rp.amount) FROM RewardPenalty rp WHERE rp.employeeId = :empId AND rp.month = :m AND rp.year = :y")
    Double sumAmountByEmployeeIdAndMonthAndYear(@Param("empId") String employeeId, @Param("m") Integer month, @Param("y") Integer year);

    @Query("SELECT rp FROM RewardPenalty rp JOIN Employee e ON rp.employeeId = e.id WHERE e.department = :dept AND rp.month = :m AND rp.year = :y ORDER BY rp.id DESC")
    List<RewardPenalty> findByDepartmentAndMonthAndYear(@Param("dept") String department, @Param("m") Integer month, @Param("y") Integer year);
}
