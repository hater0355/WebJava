package com.company.quanlyluong.controller;

import com.company.quanlyluong.dto.SalaryRecord;
import com.company.quanlyluong.entity.Employee;
import com.company.quanlyluong.entity.Timekeeping;
import com.company.quanlyluong.repository.EmployeeRepository;
import com.company.quanlyluong.repository.TimekeepingRepository;
import com.company.quanlyluong.service.EmployeeService;
import com.company.quanlyluong.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;

import com.company.quanlyluong.entity.Schedule;
import com.company.quanlyluong.repository.ScheduleRepository;
import com.company.quanlyluong.entity.RewardPenalty;
import com.company.quanlyluong.repository.RewardPenaltyRepository;
import com.company.quanlyluong.entity.User;
import com.company.quanlyluong.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimekeepingRepository timekeepingRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SalaryService salaryService;

    @Autowired
    private com.company.quanlyluong.repository.NotificationRepository notificationRepository;

    @Autowired
    private RewardPenaltyRepository rewardPenaltyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String employeeDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp == null) return "redirect:/login";

        if (emp.getLienLacKhan() == null || emp.getLienLacKhan().trim().isEmpty()) {
            model.addAttribute("employeeId", emp.getId());
            return "employee/setup"; 
        }

        model.addAttribute("employee", emp);
        
        Timekeeping todayTk = timekeepingRepository.findByEmployeeIdAndWorkDate(emp.getId(), LocalDate.now()).orElse(null);
        model.addAttribute("todayTk", todayTk);

        // Chart Data: Last 6 months salary
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartValues = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate m = now.minusMonths(i);
            SalaryRecord sr = salaryService.calculateSalary(emp, m.getMonthValue(), m.getYear(), 0.0);
            chartLabels.add(m.getMonthValue() + "/" + m.getYear());
            chartValues.add(sr.getFinalSalary());
        }
        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartValues", chartValues);

        return "employee/dashboard";
    }

    @PostMapping("/setup")
    public String setupProfile(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String dob, @RequestParam String relationship,
                               @RequestParam String emergencyPhone, @RequestParam Integer dependents) {
        LocalDate dateOfBirth = LocalDate.parse(dob);
        employeeService.setupFirstTimeProfile(userDetails.getUsername(), dateOfBirth, relationship, emergencyPhone, dependents);
        return "redirect:/employee/dashboard";
    }

    @PostMapping("/checkin")
    public String doCheckIn(@AuthenticationPrincipal UserDetails userDetails) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp != null) {
            Timekeeping tk = timekeepingRepository.findByEmployeeIdAndWorkDate(emp.getId(), LocalDate.now())
                    .orElse(new Timekeeping(emp.getId(), LocalDate.now(), null, null));
            if (tk.getCheckIn() == null) {
                tk.setCheckIn(LocalTime.now());
                timekeepingRepository.save(tk);
            }
        }
        return "redirect:/employee/dashboard";
    }

    @PostMapping("/checkout")
    public String doCheckOut(@AuthenticationPrincipal UserDetails userDetails) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp != null) {
            Timekeeping tk = timekeepingRepository.findByEmployeeIdAndWorkDate(emp.getId(), LocalDate.now()).orElse(null);
            if (tk != null && tk.getCheckIn() != null && tk.getCheckOut() == null) {
                tk.setCheckOut(LocalTime.now());
                timekeepingRepository.save(tk);
            }
        }
        return "redirect:/employee/dashboard";
    }

    @GetMapping("/salary")
    public String getSalaryDetails(@AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam(required = false) Integer month,
                                   @RequestParam(required = false) Integer year, Model model) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp == null) return "redirect:/login";
        model.addAttribute("employee", emp);
        
        int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
        int currentYear = (year == null) ? LocalDate.now().getYear() : year;

        if (emp != null) {
            SalaryRecord record = salaryService.calculateSalary(emp, currentMonth, currentYear, 0.0);
            model.addAttribute("salaryRecord", record);
            model.addAttribute("selectedMonth", currentMonth);
            model.addAttribute("selectedYear", currentYear);
        }

        return "employee/salary";
    }

    @GetMapping("/lich")
    public String employeeLich(@AuthenticationPrincipal UserDetails userDetails, 
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false) Integer year,
                               Model model) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp == null) return "redirect:/login";
        model.addAttribute("employee", emp);

        int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
        int currentYear = (year == null) ? LocalDate.now().getYear() : year;
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);

        LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
        int daysInMonth = startDate.lengthOfMonth();
        int startDayOfWeek = startDate.getDayOfWeek().getValue();
        model.addAttribute("startDayOfWeek", startDayOfWeek);
        model.addAttribute("daysInMonth", daysInMonth);

        List<String> schedules = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate d = LocalDate.of(currentYear, currentMonth, i);
            Schedule sched = scheduleRepository.findByEmployeeIdAndWorkDate(emp.getId(), d).orElse(null);
            if (sched != null && sched.getShift() != null) {
                schedules.add(sched.getShift());
            } else {
                schedules.add("");
            }
        }
        model.addAttribute("schedules", schedules);

        return "employee/lich";
    }

    @PostMapping("/lich/dangky")
    public String dangKyLich(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String date,
                             @RequestParam String shift,
                             @RequestParam(required = false) Integer month,
                             @RequestParam(required = false) Integer year) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp != null) {
            LocalDate d = LocalDate.parse(date);
            if (!d.isBefore(LocalDate.now())) {
                Schedule sched = scheduleRepository.findByEmployeeIdAndWorkDate(emp.getId(), d)
                        .orElse(new Schedule(emp.getId(), d, ""));
                
                String currentShift = sched.getShift();
                if (currentShift != null && (currentShift.startsWith("Chờ duyệt") || currentShift.startsWith("Xin nghỉ"))) {
                    // Do not allow changes while pending
                } else {
                    sched.setShift(shift);
                    scheduleRepository.save(sched);
                }
            }
        }
        
        String redirectUrl = "/employee/lich";
        if (month != null && year != null) {
            redirectUrl += "?month=" + month + "&year=" + year;
        }
        return "redirect:" + redirectUrl;
    }

    @PostMapping("/lich/xinnghi")
    public String xinNghi(@AuthenticationPrincipal UserDetails userDetails,
                          @RequestParam String date,
                          @RequestParam String reason,
                          @RequestParam(required = false) Integer month,
                          @RequestParam(required = false) Integer year) {
        Employee emp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (emp != null) {
            LocalDate d = LocalDate.parse(date);
            if (!d.isBefore(LocalDate.now())) {
                Schedule sched = scheduleRepository.findByEmployeeIdAndWorkDate(emp.getId(), d)
                        .orElse(new Schedule(emp.getId(), d, ""));
                
                String currentShift = sched.getShift();
                if (currentShift != null && (currentShift.startsWith("Chờ duyệt") || currentShift.startsWith("Xin nghỉ"))) {
                    // Already requested
                } else {
                    sched.setShift("Chờ duyệt nghỉ: " + reason);
                    scheduleRepository.save(sched);
                }
            }
        }
        
        String redirectUrl = "/employee/lich";
        if (month != null && year != null) {
            redirectUrl += "?month=" + month + "&year=" + year;
        }
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/dongnghiep")
    public String viewColleagues(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp == null) return "redirect:/login";
        model.addAttribute("employee", myEmp);

        List<Employee> allColleagues = employeeRepository.findByAccountUsernameAndStatus(myEmp.getAccountUsername(), "APPROVED");
        List<Employee> myColleagues = new ArrayList<>();
        
        for (Employee e : allColleagues) {
            if (e.getDepartment() != null && e.getDepartment().equals(myEmp.getDepartment()) && !e.getId().equals(myEmp.getId())) {
                myColleagues.add(e);
            }
        }
        model.addAttribute("colleagues", myColleagues);

        return "employee/dongnghiep";
    }

    @GetMapping("/nhiemvu")
    public String viewTasks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp == null) return "redirect:/login";
        model.addAttribute("employee", myEmp);

        List<Notification> allNotifs = notificationRepository.findByAccountUsernameOrderByCreatedAtDesc(myEmp.getAccountUsername());
        List<Notification> myTasks = new ArrayList<>();
        List<Notification> companyNotifs = new ArrayList<>();

        for (Notification n : allNotifs) {
            String msg = n.getMessage();
            if (msg.startsWith("[BÁO CÁO HOÀN THÀNH]")) continue;
            
            if (msg.startsWith("[GIAO VIỆC") || msg.startsWith("[Tăng ca") || msg.startsWith("[Quyết định")) {
                boolean forMe = msg.contains(myEmp.getId() + " - ") || msg.contains(myEmp.getName());
                boolean forMyDept = msg.contains("[Phòng ban] " + myEmp.getDepartment() + "]");
                if (forMe || forMyDept) {
                    myTasks.add(n);
                }
            } else {
                companyNotifs.add(n);
            }
        }

        model.addAttribute("tasks", myTasks);
        model.addAttribute("notifs", companyNotifs);

        return "employee/nhiemvu";
    }

    @PostMapping("/nhiemvu/mark-done")
    public String markTaskDone(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Integer id) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp != null) {
            Optional<Notification> opt = notificationRepository.findById(id);
            if (opt.isPresent()) {
                Notification n = opt.get();
                if (n.getAccountUsername().equals(myEmp.getAccountUsername()) && !n.getMessage().endsWith("[HOÀN THÀNH]")) {
                    n.setMessage(n.getMessage() + " [HOÀN THÀNH]");
                    notificationRepository.save(n);
                    
                    // Send notification to Admin
                    String titlePart = n.getMessage().split("\\(Hạn chót")[0];
                    String notifyMsg = "[BÁO CÁO HOÀN THÀNH] Nhân viên " + myEmp.getName() + " (" + myEmp.getId() + ") đã hoàn thành: " + titlePart.substring(titlePart.lastIndexOf("]") + 1).trim();
                    Notification adminNotif = new Notification();
                    adminNotif.setAccountUsername(myEmp.getAccountUsername());
                    adminNotif.setMessage(notifyMsg);
                    adminNotif.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(adminNotif);
                }
            }
        }
        return "redirect:/employee/nhiemvu";
    }

    @GetMapping("/thuongphat")
    public String viewThuongPhat(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year,
                                 Model model) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp == null || !"Trưởng phòng".equals(myEmp.getPosition())) return "redirect:/employee/dashboard";
        
        model.addAttribute("employee", myEmp);

        int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
        int currentYear = (year == null) ? LocalDate.now().getYear() : year;
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);

        List<Employee> allEmployees = employeeRepository.findByAccountUsernameAndStatus(myEmp.getAccountUsername(), "APPROVED");
        List<Employee> deptEmployees = new ArrayList<>();
        for (Employee e : allEmployees) {
            if (e.getDepartment() != null && e.getDepartment().equals(myEmp.getDepartment())) {
                deptEmployees.add(e);
            }
        }
        model.addAttribute("deptEmployees", deptEmployees);

        List<RewardPenalty> records = rewardPenaltyRepository.findByDepartmentAndMonthAndYear(myEmp.getDepartment(), currentMonth, currentYear);
        model.addAttribute("records", records);

        return "employee/thuongphat";
    }

    @PostMapping("/thuongphat/add")
    public String addThuongPhat(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String empId,
                                @RequestParam Double amount,
                                @RequestParam String reason,
                                @RequestParam(required = false) Integer month,
                                @RequestParam(required = false) Integer year) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp != null && "Trưởng phòng".equals(myEmp.getPosition())) {
            int targetMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
            int targetYear = (year == null) ? LocalDate.now().getYear() : year;

            RewardPenalty rp = new RewardPenalty();
            rp.setEmployeeId(empId);
            rp.setMonth(targetMonth);
            rp.setYear(targetYear);
            rp.setAmount(amount);
            rp.setReason(reason);
            rp.setRecordedBy(myEmp.getId());
            rewardPenaltyRepository.save(rp);

            String typeStr = (amount >= 0) ? "Thưởng" : "Phạt";
            String notifyMsg = "[Quyết định " + typeStr + "] Nhân viên " + empId + " - " + typeStr + ": " + String.format("%,.0f", amount) + " VND. Lý do: " + reason;
            
            com.company.quanlyluong.entity.Notification notif = new com.company.quanlyluong.entity.Notification();
            notif.setAccountUsername(myEmp.getAccountUsername());
            notif.setMessage(notifyMsg);
            notif.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(notif);
        }
        return "redirect:/employee/thuongphat" + (month != null ? "?month=" + month + "&year=" + year : "");
    }

    @PostMapping("/thuongphat/delete")
    public String deleteThuongPhat(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Integer id) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp != null && "Trưởng phòng".equals(myEmp.getPosition())) {
            Optional<RewardPenalty> opt = rewardPenaltyRepository.findById(id);
            if (opt.isPresent()) {
                RewardPenalty rp = opt.get();
                if (rp.getRecordedBy().equals(myEmp.getId())) {
                    rewardPenaltyRepository.delete(rp);
                }
            }
        }
        return "redirect:/employee/thuongphat";
    }

    @GetMapping("/profile")
    public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp == null) return "redirect:/login";
        model.addAttribute("employee", myEmp);
        
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("email", user.getEmail());
            model.addAttribute("phone", user.getPhone());
        }

        return "employee/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String dob,
                                @RequestParam String relationship,
                                @RequestParam String emergencyPhone,
                                @RequestParam Integer dependents) {
        Employee myEmp = employeeRepository.findByLoginUsername(userDetails.getUsername()).orElse(null);
        if (myEmp != null) {
            myEmp.setNgaySinh(LocalDate.parse(dob));
            myEmp.setGiaDinh(relationship);
            myEmp.setLienLacKhan(emergencyPhone);
            myEmp.setNguoiPhuThuoc(dependents);
            employeeRepository.save(myEmp);
        }
        return "redirect:/employee/profile?success=true";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (user != null) {
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return "redirect:/employee/profile?pwdSuccess=true";
            } else {
                return "redirect:/employee/profile?pwdError=true";
            }
        }
        return "redirect:/employee/profile";
    }
}
