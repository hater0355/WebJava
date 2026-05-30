package com.company.quanlyluong.controller;

import com.company.quanlyluong.dto.SalaryRecord;
import com.company.quanlyluong.entity.Employee;
import com.company.quanlyluong.entity.Notification;
import com.company.quanlyluong.entity.Schedule;
import com.company.quanlyluong.entity.Timekeeping;
import com.company.quanlyluong.entity.User;
import com.company.quanlyluong.repository.EmployeeRepository;
import com.company.quanlyluong.repository.NotificationRepository;
import com.company.quanlyluong.repository.ScheduleRepository;
import com.company.quanlyluong.repository.TimekeepingRepository;
import com.company.quanlyluong.repository.UserRepository;
import com.company.quanlyluong.service.EmployeeService;
import com.company.quanlyluong.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.web.multipart.MultipartFile;
import com.company.quanlyluong.entity.Department;
import com.company.quanlyluong.repository.DepartmentRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private TimekeepingRepository timekeepingRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private SalaryService salaryService;
    
    @Autowired
    private DepartmentRepository departmentRepository;

    @PostMapping("/nhanvien/delete")
    public String deleteEmployee(@RequestParam String empId, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null) {
            Optional<Employee> opt = employeeRepository.findById(empId);
            if (opt.isPresent() && opt.get().getAccountUsername().equals(admin.getUsername())) {
                employeeRepository.delete(opt.get());
            }
        }
        return "redirect:/admin/nhanvien";
    }

    @PostMapping("/nhanvien/update-salary")
    public String updateSalaryNpt(@RequestParam String empId, @RequestParam Double baseSalary, @RequestParam Integer npt, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null) {
            Optional<Employee> opt = employeeRepository.findById(empId);
            if (opt.isPresent() && opt.get().getAccountUsername().equals(admin.getUsername())) {
                Employee e = opt.get();
                e.setBaseSalary(baseSalary);
                e.setNguoiPhuThuoc(npt);
                employeeRepository.save(e);
            }
        }
        return "redirect:/admin/nhanvien";
    }

    @PostMapping("/nhanvien/import-csv")
    public String importCsv(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null || file.isEmpty()) return "redirect:/admin/nhanvien";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            List<Employee> existingEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // Skip header
                String[] data = line.split(",");
                if (data.length >= 4) {
                    String id = data[0].replace("\"", "").trim();
                    String name = data[1].replace("\"", "").trim();
                    String dep = data[2].replace("\"", "").trim();
                    String pos = data[3].replace("\"", "").trim();
                    double salary = 5000000;
                    if (data.length >= 5) {
                        try { salary = Double.parseDouble(data[4].replace("\"", "").trim()); } catch (Exception e) {}
                    }
                    
                    boolean exist = existingEmps.stream().anyMatch(e -> e.getName().equals(name) && e.getDepartment().equals(dep) && e.getPosition().equals(pos));
                    if (!exist) {
                        Employee emp = new Employee();
                        emp.setId(employeeService.generateEmployeeId());
                        emp.setName(name);
                        emp.setDepartment(dep);
                        emp.setPosition(pos);
                        emp.setBaseSalary(salary);
                        emp.setAccountUsername(admin.getUsername());
                        emp.setLoginUsername(emp.getId());
                        emp.setStatus("APPROVED");
                        employeeRepository.save(emp);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/nhanvien";
    }

    @GetMapping("/phongban")
    public String adminPhongBan(@AuthenticationPrincipal UserDetails userDetails, @RequestParam(required = false) String selectedDep, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);

        List<Department> departments = departmentRepository.findByAccountUsername(admin.getUsername());
        model.addAttribute("departments", departments);

        List<Employee> allEmployees = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        List<Employee> filteredEmployees = new ArrayList<>();
        
        if (selectedDep != null && !selectedDep.isEmpty() && !selectedDep.equals("-- Tất cả phòng ban --")) {
            for (Employee emp : allEmployees) {
                if (selectedDep.equals(emp.getDepartment())) {
                    filteredEmployees.add(emp);
                }
            }
        } else {
            filteredEmployees = allEmployees;
            selectedDep = "-- Tất cả phòng ban --";
        }
        
        model.addAttribute("selectedDep", selectedDep);
        model.addAttribute("employees", filteredEmployees);
        model.addAttribute("allEmployees", allEmployees);
        
        return "admin/phongban";
    }

    @PostMapping("/phongban/add")
    public String addDepartment(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String name) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null && name != null && !name.trim().isEmpty()) {
            if (!departmentRepository.findByNameAndAccountUsername(name.trim(), admin.getUsername()).isPresent()) {
                Department dep = new Department();
                dep.setName(name.trim());
                dep.setAccountUsername(admin.getUsername());
                departmentRepository.save(dep);
            }
        }
        return "redirect:/admin/phongban";
    }
    
    @PostMapping("/phongban/edit")
    public String editDepartment(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String oldName, @RequestParam String newName) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null && newName != null && !newName.trim().isEmpty() && !oldName.equals("Chung")) {
            Optional<Department> depOpt = departmentRepository.findByNameAndAccountUsername(oldName, admin.getUsername());
            if (depOpt.isPresent()) {
                Department dep = depOpt.get();
                dep.setName(newName.trim());
                departmentRepository.save(dep);
                
                // Update employees' department
                List<Employee> emps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
                for (Employee emp : emps) {
                    if (oldName.equals(emp.getDepartment())) {
                        emp.setDepartment(newName.trim());
                        employeeRepository.save(emp);
                    }
                }
            }
        }
        return "redirect:/admin/phongban?selectedDep=" + (newName != null ? newName.trim() : "");
    }
    
    @PostMapping("/phongban/delete")
    public String deleteDepartment(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String name) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null && name != null && !name.equals("Chung")) {
            Optional<Department> depOpt = departmentRepository.findByNameAndAccountUsername(name, admin.getUsername());
            if (depOpt.isPresent()) {
                departmentRepository.delete(depOpt.get());
                
                // Move employees to "Chung"
                List<Employee> emps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
                for (Employee emp : emps) {
                    if (name.equals(emp.getDepartment())) {
                        emp.setDepartment("Chung");
                        employeeRepository.save(emp);
                    }
                }
            }
        }
        return "redirect:/admin/phongban";
    }
    
    @PostMapping("/phongban/change-employee")
    public String changeEmployeeDepartmentPosition(@AuthenticationPrincipal UserDetails userDetails, 
                                                 @RequestParam String empId, 
                                                 @RequestParam String newDepartment, 
                                                 @RequestParam String newPosition,
                                                 @RequestParam(required = false) String selectedDep) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null) {
            Optional<Employee> empOpt = employeeRepository.findById(empId);
            if (empOpt.isPresent() && empOpt.get().getAccountUsername().equals(admin.getUsername())) {
                Employee emp = empOpt.get();
                emp.setDepartment(newDepartment);
                emp.setPosition(newPosition);
                employeeRepository.save(emp);
            }
        }
        return "redirect:/admin/phongban" + (selectedDep != null ? "?selectedDep=" + selectedDep : "");
    }

    @GetMapping("/dashboard")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";

        model.addAttribute("admin", admin);

        List<Employee> listEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        double totalSalary = listEmps.stream().mapToDouble(Employee::getBaseSalary).sum();

        model.addAttribute("totalEmps", listEmps.size());
        model.addAttribute("totalSalary", totalSalary);

        java.util.Map<String, Double> deptSalaryMap = new java.util.HashMap<>();
        for (Employee e : listEmps) {
            String dept = e.getDepartment() != null ? e.getDepartment() : "Chưa phân bổ";
            deptSalaryMap.put(dept, deptSalaryMap.getOrDefault(dept, 0.0) + e.getBaseSalary());
        }
        
        List<String> labels = new ArrayList<>(deptSalaryMap.keySet());
        List<Double> values = new ArrayList<>(deptSalaryMap.values());
        
        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartValues", values);

        return "admin/dashboard";
    }

    @GetMapping("/xetduyet")
    public String adminXetDuyet(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        
        model.addAttribute("admin", admin);
        List<Employee> pendingEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "PENDING");
        model.addAttribute("pendingEmps", pendingEmps);
        
        return "admin/xetduyet";
    }

    @PostMapping("/approve-employee")
    public String approveEmp(@RequestParam String empId, @RequestParam String department,
                             @RequestParam String position, @RequestParam Double baseSalary) {
        employeeService.approveEmployee(empId, department, position, baseSalary);
        return "redirect:/admin/xetduyet";
    }

    @GetMapping("/nhanvien")
    public String adminNhanVien(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        
        model.addAttribute("admin", admin);
        List<Employee> listEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        model.addAttribute("employees", listEmps);
        
        return "admin/nhanvien";
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(@AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        List<Employee> listEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        StringBuilder csvContent = new StringBuilder();
        // UTF-8 BOM
        csvContent.append('\ufeff');
        csvContent.append("Mã NV,Họ tên,Phòng ban,Chức vụ,Lương cơ bản\n");
        for (Employee emp : listEmps) {
            String safeName = "\"" + emp.getName().replace("\"", "\"\"") + "\"";
            String safeDep = "\"" + emp.getDepartment().replace("\"", "\"\"") + "\"";
            String safePos = "\"" + emp.getPosition().replace("\"", "\"\"") + "\"";
            csvContent.append(emp.getId()).append(",")
                      .append(safeName).append(",")
                      .append(safeDep).append(",")
                      .append(safePos).append(",")
                      .append(emp.getBaseSalary()).append("\n");
        }

        byte[] bytes = csvContent.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DanhSachNhanVien.csv");
        headers.set(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/nhanvien/{id}/lich")
    public String adminLichNhanVien(@AuthenticationPrincipal UserDetails userDetails, 
                                    @PathVariable("id") String empId, 
                                    @RequestParam(required = false) Integer month,
                                    @RequestParam(required = false) Integer year,
                                    Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);

        Optional<Employee> empOpt = employeeRepository.findById(empId);
        if (!empOpt.isPresent() || !empOpt.get().getAccountUsername().equals(admin.getUsername())) {
            return "redirect:/admin/nhanvien";
        }
        model.addAttribute("employee", empOpt.get());

        int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
        int currentYear = (year == null) ? LocalDate.now().getYear() : year;
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);

        LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
        int daysInMonth = startDate.lengthOfMonth();
        int startDayOfWeek = startDate.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        model.addAttribute("startDayOfWeek", startDayOfWeek);
        model.addAttribute("daysInMonth", daysInMonth);

        List<String> schedules = new ArrayList<>();
        for (int i = 1; i <= daysInMonth; i++) {
            LocalDate d = LocalDate.of(currentYear, currentMonth, i);
            Schedule sched = scheduleRepository.findByEmployeeIdAndWorkDate(empId, d).orElse(null);
            if (sched != null) {
                schedules.add(sched.getShift());
            } else {
                schedules.add("");
            }
        }
        model.addAttribute("schedules", schedules);

        return "admin/lich";
    }

    @GetMapping("/chamcong")
    public String adminChamCong(@AuthenticationPrincipal UserDetails userDetails, 
                                @RequestParam(required = false) String date, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);

        LocalDate filterDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        model.addAttribute("selectedDate", filterDate);

        List<Employee> listEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        List<Timekeeping> attendanceRecords = new ArrayList<>();
        
        for (Employee emp : listEmps) {
            Timekeeping tk = timekeepingRepository.findByEmployeeIdAndWorkDate(emp.getId(), filterDate)
                .orElse(new Timekeeping(emp.getId(), filterDate, null, null));
            attendanceRecords.add(tk);
        }
        
        model.addAttribute("employees", listEmps);
        model.addAttribute("attendanceRecords", attendanceRecords);

        return "admin/chamcong";
    }

    @GetMapping("/tinhluong")
    public String adminTinhLuong(@AuthenticationPrincipal UserDetails userDetails, 
                                 @RequestParam(required = false) Integer month,
                                 @RequestParam(required = false) Integer year, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);

        int currentMonth = (month == null) ? LocalDate.now().getMonthValue() : month;
        int currentYear = (year == null) ? LocalDate.now().getYear() : year;
        
        model.addAttribute("selectedMonth", currentMonth);
        model.addAttribute("selectedYear", currentYear);

        List<Employee> listEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        List<SalaryRecord> salaryRecords = new ArrayList<>();

        for (Employee emp : listEmps) {
            SalaryRecord record = salaryService.calculateSalary(emp, currentMonth, currentYear, 0.0);
            salaryRecords.add(record);
        }

        model.addAttribute("salaryRecords", salaryRecords);
        model.addAttribute("employees", listEmps);

        return "admin/tinhluong";
    }

    @GetMapping("/thongbao")
    public String adminThongBao(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);
        return "admin/thongbao";
    }

    @PostMapping("/send-notification")
    public String sendNotif(@AuthenticationPrincipal UserDetails userDetails, @RequestParam String message) {
        Notification notif = new Notification();
        notif.setAccountUsername(userDetails.getUsername());
        notif.setMessage(message);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);
        return "redirect:/admin/thongbao";
    }

    @GetMapping("/vangmat")
    public String adminVangMat(@AuthenticationPrincipal UserDetails userDetails, 
                               @RequestParam(required = false) String date, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);

        LocalDate filterDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        model.addAttribute("selectedDate", filterDate);

        List<Employee> listEmps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        List<AbsenceRecord> records = new ArrayList<>();

        for (Employee emp : listEmps) {
            String shift = "Chưa đăng ký";
            Optional<Schedule> schedOpt = scheduleRepository.findByEmployeeIdAndWorkDate(emp.getId(), filterDate);
            if (schedOpt.isPresent()) {
                shift = schedOpt.get().getShift();
            }

            Optional<Timekeeping> tkOpt = timekeepingRepository.findByEmployeeIdAndWorkDate(emp.getId(), filterDate);
            java.time.LocalTime checkIn = tkOpt.isPresent() ? tkOpt.get().getCheckIn() : null;

            if (shift != null && (shift.startsWith("Chờ duyệt nghỉ") || shift.startsWith("Xin nghỉ"))) {
                String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                records.add(new AbsenceRecord(emp.getId(), emp.getName(), "Chờ duyệt", reason));
            } else if (shift != null && shift.startsWith("Đã duyệt nghỉ")) {
                records.add(new AbsenceRecord(emp.getId(), emp.getName(), "Nghỉ CÓ phép", shift.replace("Đã duyệt nghỉ: ", "")));
            } else if (shift != null && shift.startsWith("Từ chối nghỉ")) {
                records.add(new AbsenceRecord(emp.getId(), emp.getName(), "Bị từ chối nghỉ", shift.replace("Từ chối nghỉ: ", "")));
            } else if (!"Nghỉ".equals(shift) && !"Chưa đăng ký".equals(shift)) {
                if (checkIn == null && !filterDate.isAfter(LocalDate.now())) {
                    records.add(new AbsenceRecord(emp.getId(), emp.getName(), "Nghỉ KHÔNG phép", "Bỏ ca: " + shift));
                }
            }
        }
        
        model.addAttribute("absenceRecords", records);
        return "admin/vangmat";
    }

    @PostMapping("/vangmat/approve")
    public String approveLeave(@RequestParam String empId, @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        Optional<Schedule> schedOpt = scheduleRepository.findByEmployeeIdAndWorkDate(empId, d);
        if (schedOpt.isPresent()) {
            Schedule sched = schedOpt.get();
            String shift = sched.getShift();
            if (shift.startsWith("Chờ duyệt nghỉ: ") || shift.startsWith("Xin nghỉ: ")) {
                String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                sched.setShift("Đã duyệt nghỉ: " + reason);
                scheduleRepository.save(sched);
            }
        }
        return "redirect:/admin/vangmat?date=" + date;
    }

    @PostMapping("/vangmat/reject")
    public String rejectLeave(@RequestParam String empId, @RequestParam String date) {
        LocalDate d = LocalDate.parse(date);
        Optional<Schedule> schedOpt = scheduleRepository.findByEmployeeIdAndWorkDate(empId, d);
        if (schedOpt.isPresent()) {
            Schedule sched = schedOpt.get();
            String shift = sched.getShift();
            if (shift.startsWith("Chờ duyệt nghỉ: ") || shift.startsWith("Xin nghỉ: ")) {
                String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                sched.setShift("Từ chối nghỉ: " + reason);
                scheduleRepository.save(sched);
            }
        }
        return "redirect:/admin/vangmat?date=" + date;
    }

    @PostMapping("/vangmat/approve-all")
    public String approveAllLeaves(@RequestParam String date, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";

        LocalDate d = LocalDate.parse(date);
        List<Employee> emps = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        for (Employee emp : emps) {
            Optional<Schedule> schedOpt = scheduleRepository.findByEmployeeIdAndWorkDate(emp.getId(), d);
            if (schedOpt.isPresent()) {
                Schedule sched = schedOpt.get();
                String shift = sched.getShift();
                if (shift.startsWith("Chờ duyệt nghỉ: ") || shift.startsWith("Xin nghỉ: ")) {
                    String reason = shift.replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
                    sched.setShift("Đã duyệt nghỉ: " + reason);
                    scheduleRepository.save(sched);
                }
            }
        }
        return "redirect:/admin/vangmat?date=" + date;
    }

    @GetMapping("/giaoviec")
    public String adminGiaoViec(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";
        model.addAttribute("admin", admin);

        List<Department> departments = departmentRepository.findByAccountUsername(admin.getUsername());
        model.addAttribute("departments", departments);

        List<Employee> employees = employeeRepository.findByAccountUsernameAndStatus(admin.getUsername(), "APPROVED");
        model.addAttribute("employees", employees);

        List<Notification> allNotifs = notificationRepository.findByAccountUsernameOrderByCreatedAtDesc(admin.getUsername());
        List<Notification> taskNotifs = new ArrayList<>();
        int totalTasks = 0;
        int completedTasks = 0;

        for (Notification n : allNotifs) {
            if (n.getMessage().startsWith("[GIAO VIỆC")) {
                taskNotifs.add(n);
                totalTasks++;
                if (n.getMessage().endsWith("[HOÀN THÀNH]")) {
                    completedTasks++;
                }
            }
        }

        model.addAttribute("tasks", taskNotifs);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);

        return "admin/giaoviec";
    }

    @PostMapping("/giaoviec/add")
    public String addGiaoViec(@AuthenticationPrincipal UserDetails userDetails, 
                              @RequestParam String target, 
                              @RequestParam String title, 
                              @RequestParam String deadline, 
                              @RequestParam String description) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin == null) return "redirect:/login";

        String msg = "[GIAO VIỆC - " + target + "] " + title + " (Hạn chót: " + deadline + "):\n" + description;
        
        Notification notif = new Notification();
        notif.setAccountUsername(admin.getUsername());
        notif.setMessage(msg);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);

        return "redirect:/admin/giaoviec";
    }

    @PostMapping("/giaoviec/mark-done")
    public String markGiaoViecDone(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Integer id) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null) {
            Optional<Notification> opt = notificationRepository.findById(id);
            if (opt.isPresent()) {
                Notification n = opt.get();
                if (n.getAccountUsername().equals(admin.getUsername()) && !n.getMessage().endsWith("[HOÀN THÀNH]")) {
                    n.setMessage(n.getMessage() + " [HOÀN THÀNH]");
                    notificationRepository.save(n);
                }
            }
        }
        return "redirect:/admin/giaoviec";
    }

    @PostMapping("/giaoviec/delete")
    public String deleteGiaoViec(@AuthenticationPrincipal UserDetails userDetails, @RequestParam Integer id) {
        User admin = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        if (admin != null) {
            Optional<Notification> opt = notificationRepository.findById(id);
            if (opt.isPresent() && opt.get().getAccountUsername().equals(admin.getUsername())) {
                notificationRepository.delete(opt.get());
            }
        }
        return "redirect:/admin/giaoviec";
    }
}
