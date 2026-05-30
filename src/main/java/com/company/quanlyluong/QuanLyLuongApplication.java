package com.company.quanlyluong;

import com.company.quanlyluong.entity.User;
import com.company.quanlyluong.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class QuanLyLuongApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuanLyLuongApplication.class, args);
    }  

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Tạo tài khoản Admin mặc định nếu chưa có để test
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                admin.setRole("ROLE_ADMIN");
                admin.setFullName("Quản trị viên");
                admin.setCompanyCode("COMP123"); // Mã công ty để nhân viên đăng ký
                userRepository.save(admin);
                System.out.println(">>> Đã tạo tài khoản Admin mặc định: admin / admin123");
            }

            Thread.currentThread().setName("Main-App");
            System.out.println("\n==========================================");
            System.out.println("   Hệ thống Quản lý lương: ONLINE");
            System.out.println("   Admin test: admin / admin123 (Mã CT: COMP123)");
            System.out.println("==========================================\n");
        };
    }

    // Bean này sẽ báo cho bạn biết khi nào và tại sao ứng dụng bị đóng
    @Bean
    public ApplicationListener<ContextClosedEvent> shutdownListener() {
        return event -> {
            System.out.println("!!! CẢNH BÁO: Ứng dụng đang tự đóng (Context Closed) !!!");
        };
    }
}
