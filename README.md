# Hệ Thống Quản Lý Lương (Salary Management System)

## 📋 Mô Tả Dự Án
Ứng dụng web quản lý lương toàn diện, xây dựng bằng **Spring Boot 3.2**, **Thymeleaf**, **MySQL**, với các tính năng:
- **Quản lý nhân viên & phòng ban**
- **Quản lý lương & bảng chấm công**
- **Quản lý vắng mặt & xin nghỉ phép**
- **Giao công việc & quản lý task**
- **Dashboard & thống kê**
- **Kiểm soát truy cập & bảo mật**

---

## 🛠️ Công Nghệ Sử Dụng

| Công Nghệ | Phiên Bản | Mục Đích |
|-----------|----------|---------|
| **Java** | 17 | Ngôn ngữ lập trình |
| **Spring Boot** | 3.2.0 | Framework chính |
| **Spring Data JPA** | - | ORM & Database |
| **Spring Security** | - | Xác thực & Phân quyền |
| **Thymeleaf** | - | Template Engine |
| **MySQL** | 8.0+ | Database |
| **Lombok** | - | Giảm boilerplate code |
| **Apache POI** | 5.2.3 | Excel import/export |
| **ModelMapper** | 3.1.1 | DTO Mapping |
| **Guava** | 32.1.3 | Utilities |

---

## 📦 Yêu Cầu Hệ Thống

- **Java**: 17 trở lên
- **Maven**: 3.8.0+
- **MySQL**: 8.0+
- **IDE**: IntelliJ IDEA / VS Code / Eclipse

---

## 🚀 Cài Đặt & Chạy

### 1. Clone Repository
```bash
git clone https://github.com/hater0355/WebJava.git
cd WebJava
```

### 2. Cấu Hình Database
```bash
# Tạo database
CREATE DATABASE quanlyluong CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Cập Nhật `application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quanlyluong
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

### 4. Chạy Ứng Dụng
```bash
mvn clean install
mvn spring-boot:run
```

### 5. Truy Cập Ứng Dụng
- **Web App**: [http://localhost:8080](http://localhost:8080)
- **Swagger API**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Actuator**: [http://localhost:8080/actuator](http://localhost:8080/actuator)

---

## 📁 Cấu Trúc Thư Mục

```
src/
├── main/
│   ├── java/com/company/quanlyluong/
│   │   ├── config/              # Spring configuration
│   │   ├── controller/          # REST/Web controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # Data access layer (JPA)
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Data transfer objects
│   │   ├── exception/           # Custom exceptions
│   │   ├── util/                # Utility classes
│   │   └── QuanlyluongApplication.java
│   └── resources/
│       ├── templates/           # Thymeleaf templates
│       ├── static/              # CSS, JS, images
│       ├── application.yml      # Main configuration
│       └── application-dev.yml  # Development config
└── test/java/                   # Unit & integration tests
```

---

## 🔑 Các Tính Năng Chính

### Giai Đoạn A: Quản Lý Cốt Lõi (Admin)
- **A1**: Quản lý Phòng ban (CRUD)
- **A2**: Duyệt nghỉ & Quản lý vắng mặt
- **A3**: Giao việc cho nhân viên/phòng ban
- **A4**: Cải tiến Quản lý nhân viên (Excel import)

### Giai Đoạn C: Nghiệp Vụ Cá Nhân (Employee)
- **C1**: Đăng ký lịch làm & Xin nghỉ
- **C2**: Danh bạ đồng nghiệp
- **C3**: Quản lý công việc & Thông báo
- **C4**: Quản lý Thưởng/Phạt (Trưởng phòng)
- **C5**: Cập nhật hồ sơ & Đổi mật khẩu

### Giai Đoạn B: Nâng Cao & Dashboard
- **B1**: Biểu đồ thống kê (Admin & Employee)
- **B2**: Bảng chấm công dạng lưới
- **B3**: Cảnh báo hệ thống tự động
- **B4**: Chi tiết Phiếu Lương

---

## 🔐 Bảo Mật

- ✅ Spring Security với role-based access control (RBAC)
- ✅ Password encoding (BCrypt)
- ✅ CSRF protection
- ✅ Input validation & sanitization
- ✅ SQL injection prevention (JPA parameterized queries)

---

## 📊 API Documentation

Tất cả API được tài liệu hóa bằng **Swagger/OpenAPI** tại:
```
http://localhost:8080/swagger-ui.html
```

---

## 🧪 Testing

Chạy unit tests:
```bash
mvn test
```

Kiểm tra coverage:
```bash
mvn test jacoco:report
```

---

## 📝 Chú Ý

- Đảm bảo MySQL đang chạy trước khi start application
- Cấu hình charset MySQL thành `utf8mb4` để hỗ trợ tiếng Việt
- Đặt `JAVA_HOME` và `MAVEN_HOME` trong environment variables

---

## 👨‍💼 Tác Giả & Liên Hệ

- **Dự Án**: Hệ Thống Quản Lý Lương
- **Repository**: [hater0355/WebJava](https://github.com/hater0355/WebJava)
- **Phiên Bản**: 0.0.1-SNAPSHOT

---

## 📄 License

Dự án này được cấp phép dưới MIT License

---

## 🤝 Đóng Góp

1. Fork repository
2. Tạo branch mới (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Mở Pull Request

---

**Last Updated**: 2026-05-30
