package com.company.quanlyluong.repository;

import com.company.quanlyluong.entity.Timekeeping;
import com.company.quanlyluong.entity.TimekeepingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimekeepingRepository extends JpaRepository<Timekeeping, TimekeepingId> {
    Optional<Timekeeping> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);
    
    @Query("SELECT t FROM Timekeeping t WHERE t.employeeId = :empId AND t.workDate BETWEEN :start AND :end")
    List<Timekeeping> findRecordsInPeriod(@Param("empId") String employeeId, @Param("start") LocalDate startDate, @Param("end") LocalDate endDate);
}
