package com.company.quanlyluong.controller;

import com.company.quanlyluong.entity.User;
import com.company.quanlyluong.repository.UserRepository;
import com.company.quanlyluong.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.Random;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String viewLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String viewRegister() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public String registerUser(@RequestParam String role, @RequestParam String username, @RequestParam String password,
                               @RequestParam String fullName, @RequestParam String email, @RequestParam String phone,
                               @RequestParam(required = false) String companyCode) {
        
        if (role.equals("ADMIN")) {
            if (userRepository.findByUsername(username).isPresent()) return "EXISTED";
            String randomCompCode = "COMP" + (1000 + new Random().nextInt(9000));
            User newAdmin = new User(username, passwordEncoder.encode(password), "ROLE_ADMIN", randomCompCode, fullName, email, phone);
            userRepository.save(newAdmin);
            return "SUCCESS_ADMIN";
        } else {
            return employeeService.registerEmployee(username, passwordEncoder.encode(password), fullName, companyCode, email, phone);
        }
    }

    @GetMapping("/default")
    public String defaultAfterLogin(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }
        return "redirect:/employee/dashboard";
    }
}
