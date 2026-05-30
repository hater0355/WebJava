package com.company.quanlyluong.service;

import com.company.quanlyluong.entity.Employee;
import com.company.quanlyluong.entity.User;
import com.company.quanlyluong.entity.Schedule;
import com.company.quanlyluong.repository.EmployeeRepository;
import com.company.quanlyluong.repository.UserRepository;
import com.company.quanlyluong.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String generateEmployeeId() {
        int year = LocalDate.now().getYear() % 100;
        String prefix = String.format("%02d", year);
        Random rand = new Random();
        String newId;
        do {
            newId = prefix + String.format("%03d", rand.nextInt(999) + 1);
        } while (employeeRepository.existsById(newId));
        return newId;
    }

    @Transactional
    public String registerEmployee(String username, String password, String fullName, String companyCode, String email, String phone) {
        User boss = userRepository.findByCompanyCodeAndRole(companyCode, "ROLE_ADMIN").orElse(null);
        if (boss == null) return "Mã công ty không tồn tại!";

        if (userRepository.findByUsername(username).isPresent()) return "Tài khoản đã tồn tại!";
        if (userRepository.findByEmail(email).isPresent()) return "Email đã được sử dụng!";
        if (userRepository.findByPhone(phone).isPresent()) return "Số điện thoại đã được sử dụng!";

        User newUser = new User(username, password, "ROLE_EMPLOYEE", null, fullName, email, phone);
        userRepository.save(newUser);

        String empId = generateEmployeeId();
        Employee emp = new Employee();
        emp.setId(empId);
        emp.setName(fullName);
        emp.setAccountUsername(boss.getUsername());
        emp.setLoginUsername(username);
        emp.setStatus("PENDING");
        employeeRepository.save(emp);

        return "SUCCESS";
    }

    @Transactional
    public void setupFirstTimeProfile(String loginUsername, LocalDate dob, String relationship, String emergencyPhone, int dependents) {
        Employee emp = employeeRepository.findByLoginUsername(loginUsername)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ nhân viên"));
        emp.setNgaySinh(dob);
        emp.setGiaDinh(relationship);
        emp.setLienLacKhan(emergencyPhone);
        emp.setNguoiPhuThuoc(dependents);
        employeeRepository.save(emp);
    }

    @Transactional
    public void approveEmployee(String employeeId, String department, String position, Double baseSalary) {
        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        emp.setDepartment(department);
        emp.setPosition(position);
        emp.setBaseSalary(baseSalary);
        emp.setStatus("APPROVED");
        employeeRepository.save(emp);
    }

    @Transactional
    public void approveLeave(String empId, LocalDate date, boolean isApproved) {
        Schedule sched = scheduleRepository.findByEmployeeIdAndWorkDate(empId, date).orElse(null);
        if (sched != null && (sched.getShift().startsWith("Chờ duyệt nghỉ:") || sched.getShift().startsWith("Xin nghỉ:"))) {
            String reason = sched.getShift().replace("Chờ duyệt nghỉ: ", "").replace("Xin nghỉ: ", "");
            sched.setShift(isApproved ? "Đã duyệt nghỉ: " + reason : "Từ chối nghỉ: " + reason);
            scheduleRepository.save(sched);
        }
    }
}
