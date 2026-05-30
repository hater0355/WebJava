package com.company.quanlyluong.service;

import com.company.quanlyluong.dto.SalaryRecord;
import com.company.quanlyluong.entity.Employee;
import com.company.quanlyluong.entity.Schedule;
import com.company.quanlyluong.entity.Timekeeping;
import com.company.quanlyluong.repository.RewardPenaltyRepository;
import com.company.quanlyluong.repository.ScheduleRepository;
import com.company.quanlyluong.repository.TimekeepingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class SalaryService {

    private static final int STANDARD_WORK_DAYS = 22;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private TimekeepingRepository timekeepingRepository;

    @Autowired
    private RewardPenaltyRepository rewardPenaltyRepository;

    public SalaryRecord calculateSalary(Employee employee, int month, int year, double manualBonus) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Timekeeping> attendanceRecords = timekeepingRepository.findRecordsInPeriod(employee.getId(), startDate, endDate);

        int regularDays = 0;
        int overtimeDays = 0;

        for (Timekeeping tk : attendanceRecords) {
            if (tk.getCheckIn() != null) {
                DayOfWeek day = tk.getWorkDate().getDayOfWeek();
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    overtimeDays++;
                } else {
                    regularDays++;
                }
            }
        }

        int finalRegularDays = regularDays;
        int finalOvertimeDays = overtimeDays;

        if (regularDays < STANDARD_WORK_DAYS) {
            int daysShort = STANDARD_WORK_DAYS - regularDays;
            if (overtimeDays <= daysShort) {
                finalRegularDays += overtimeDays;
                finalOvertimeDays = 0;
            } else {
                finalRegularDays = STANDARD_WORK_DAYS;
                finalOvertimeDays = overtimeDays - daysShort;
            }
        }

        double dailyRate = employee.getBaseSalary() / STANDARD_WORK_DAYS;
        double hourlyRate = dailyRate / 8.0;

        double penalty = 0.0;
        int forgotCheckOutCount = 0;
        long totalWorkedMinutes = 0;

        for (Timekeeping tk : attendanceRecords) {
            if (tk.getCheckIn() == null) continue;

            Schedule sched = scheduleRepository.findByEmployeeIdAndWorkDate(employee.getId(), tk.getWorkDate()).orElse(null);
            String shift = (sched != null) ? sched.getShift() : "";

            LocalTime expectedStart = null;
            if (shift.startsWith("Ca 1")) expectedStart = LocalTime.of(8, 0);
            else if (shift.startsWith("Ca 2")) expectedStart = LocalTime.of(12, 0);

            if (expectedStart != null) {
                long lateMinutes = Duration.between(expectedStart, tk.getCheckIn()).toMinutes();
                if (lateMinutes > 5) {
                    penalty += (lateMinutes / 60.0) * hourlyRate;
                }
            }

            if (tk.getCheckOut() == null) {
                if (tk.getWorkDate().isBefore(LocalDate.now())) {
                    penalty += 200000.0;
                    forgotCheckOutCount++;
                }
            } else {
                if (tk.getCheckOut().equals(LocalTime.of(23, 59, 0))) {
                    penalty += 200000.0;
                    forgotCheckOutCount++;
                } else {
                    long workedMinutes = Duration.between(tk.getCheckIn(), tk.getCheckOut()).toMinutes();
                    if (workedMinutes > 0) {
                        if (workedMinutes < 480) {
                            long missingMinutes = 480 - workedMinutes;
                            penalty += (missingMinutes / 60.0) * hourlyRate;
                        }
                        totalWorkedMinutes += workedMinutes;
                    }
                }
            }
        }

        Double deptRewardPenalty = rewardPenaltyRepository.sumAmountByEmployeeIdAndMonthAndYear(employee.getId(), month, year);
        if (deptRewardPenalty == null) deptRewardPenalty = 0.0;

        double grossSalary = (dailyRate * finalRegularDays)
                           + ((dailyRate * 2) * finalOvertimeDays)
                           + manualBonus
                           + deptRewardPenalty;

        double insurance = employee.getBaseSalary() * 0.105;
        double tax = calculatePersonalIncomeTax(grossSalary, insurance, employee.getNguoiPhuThuoc());
        double finalSalary = grossSalary - penalty - insurance - tax;

        if (finalSalary < 0) finalSalary = 0;

        return new SalaryRecord(
            employee.getName(),
            month + "/" + year,
            finalRegularDays,
            finalOvertimeDays,
            penalty,
            insurance,
            tax,
            finalSalary,
            forgotCheckOutCount,
            totalWorkedMinutes / 60.0
        );
    }

    private double calculatePersonalIncomeTax(double income, double insurance, int dependents) {
        double personalDeduction = 11000000.0;
        double dependentDeduction = 4400000.0 * dependents;
        double taxableIncome = income - personalDeduction - dependentDeduction - insurance;

        if (taxableIncome <= 0) return 0.0;

        if (taxableIncome <= 5000000) {
            return taxableIncome * 0.05;
        } else if (taxableIncome <= 10000000) {
            return taxableIncome * 0.10 - 250000;
        } else if (taxableIncome <= 18000000) {
            return taxableIncome * 0.15 - 750000;
        } else if (taxableIncome <= 32000000) {
            return taxableIncome * 0.20 - 1650000;
        } else if (taxableIncome <= 52000000) {
            return taxableIncome * 0.25 - 3250000;
        } else if (taxableIncome <= 80000000) {
            return taxableIncome * 0.30 - 5850000;
        } else {
            return taxableIncome * 0.35 - 9850000;
        }
    }
}
