package com.company.quanlyluong.repository;

import com.company.quanlyluong.entity.Schedule;
import com.company.quanlyluong.entity.ScheduleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, ScheduleId> {
    Optional<Schedule> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);
    
    @Query("SELECT s FROM Schedule s WHERE s.employeeId = :empId AND s.workDate BETWEEN :start AND :end")
    List<Schedule> findSchedulesInPeriod(@Param("empId") String employeeId, @Param("start") LocalDate startDate, @Param("end") LocalDate endDate);

    @Query("SELECT s FROM Schedule s WHERE s.employeeId = :empId AND (s.shift LIKE 'Chờ duyệt nghỉ:%' OR s.shift LIKE 'Xin nghỉ:%')")
    List<Schedule> findPendingLeavesByEmployee(@Param("empId") String employeeId);
}
