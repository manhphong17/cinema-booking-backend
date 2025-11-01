# 🎬 CineShow - Cinema Booking System Backend

Backend API cho hệ thống đặt vé xem phim trực tuyến. Ứng dụng được xây dựng bằng Spring Boot 3.5.5 và Java 17.

## 📋 Mục lục

- [Giới thiệu](#giới-thiệu)
- [Tính năng chính](#tính-năng-chính)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt và chạy](#cài-đặt-và-chạy)
- [Cấu hình môi trường](#cấu-hình-môi-trường)
- [Chạy Tests](#chạy-tests)
- [API Documentation](#api-documentation)
- [Kiến trúc](#kiến-trúc)
- [Troubleshooting](#troubleshooting)

## 🎯 Giới thiệu

CineShow là một hệ thống quản lý và đặt vé xem phim toàn diện, hỗ trợ:

- Quản lý phim, suất chiếu, phòng chiếu
- Đặt vé trực tuyến với chọn ghế real-time
- Quản lý người dùng và phân quyền
- Xử lý thanh toán và đơn hàng
- Quản lý combo và khuyến mãi

## ✨ Tính năng chính

### 🎥 Quản lý Phim

- CRUD phim (thêm, sửa, xóa, xem chi tiết)
- Phân loại theo thể loại, ngôn ngữ, quốc gia
- Hình ảnh banner và poster
- Trạng thái phim (đang chiếu, sắp chiếu, ngưng chiếu)

### 🎫 Đặt vé

- Chọn phim, suất chiếu, ghế ngồi
- WebSocket real-time cho chọn ghế
- Tạm giữ ghế trong khoảng thời gian nhất định
- Hỗ trợ combo đồ ăn, nước uống
- Áp dụng voucher/khuyến mãi

### 💳 Thanh toán

- Tích hợp VNPay
- Quản lý trạng thái đơn hàng
- Lịch sử giao dịch
- Hủy đơn và hoàn tiền

### 🎭 Quản lý Phòng chiếu

- CRUD phòng chiếu, loại phòng
- Quản lý ghế và loại ghế
- Giá vé theo loại ghế, suất chiếu
- Lịch chiếu chi tiết

### 👥 Quản lý Người dùng

- Đăng ký, đăng nhập (JWT)
- OAuth2 (Google)
- Phân quyền theo vai trò
- Quản lý profile

### 🛍️ Bán hàng

- Quản lý combo/concession
- Đặt hàng đồ ăn/nước uống
- Quản lý voucher/khuyến mãi

## 🛠️ Công nghệ sử dụng

### Core Framework

- **Spring Boot 3.5.5**
- **Java 17**
- **Maven** - Dependency management

### Database & Cache

- **MySQL 8** - Database chính
- **Redis** - Cache và session management
- **H2** - In-memory database cho testing

### Security & Authentication

- **Spring Security**
- **JWT** (Access Token + Refresh Token)
- **OAuth2** (Google)

### Real-time Communication

- **WebSocket** - Chọn ghế real-time

### Cloud Services

- **AWS S3** - Lưu trữ file (poster, banner, avatar)
- **AWS CloudFront** - CDN
- **SendGrid** - Email service

### API Documentation

- **SpringDoc OpenAPI 3** - Swagger UI

### Other Libraries

- **Lombok** - Code generation
- **Jackson** - JSON processing
- **Validation** - Input validation
- **Actuator** - Monitoring

## 💻 Yêu cầu hệ thống

### Cần có

- **Java 17+**
- **Maven 3.6+**
- **MySQL 8+**
- **Redis 6+** (hoặc Redis Cloud)

### Tùy chọn

- Docker & Docker Compose
- IDE: IntelliJ IDEA / Eclipse / VS Code

## 🚀 Cài đặt và chạy

### 1. Clone repository

```bash
git clone <repository-url>
cd cinema-booking-backend/CineShow
```

### 2. Cấu hình database

Tạo database MySQL:

```sql
CREATE DATABASE cineshow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Hoặc database sẽ tự động được tạo nếu `createDatabaseIfNotExist=true`.

### 3. Cài đặt dependencies

```bash
mvn clean install
```

### 4. Chạy ứng dụng

#### Option A: Maven

```bash
# Development
mvn spring-boot:run

# Hoặc với profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build JAR và chạy
mvn clean package
java -jar target/CineShow-0.0.1-SNAPSHOT.jar
```

#### Option B: IntelliJ IDEA

1. Mở project trong IntelliJ
2. Chạy `CineShowApplication` class
3. Hoặc configure Run Configuration với Active Profile = `dev`

### 5. Truy cập ứng dụng

- **API Base URL**: `http://localhost:8885`
- **Swagger UI**: `http://localhost:8885/swagger-ui.html`
- **API Docs**: `http://localhost:8885/v3/api-docs`
- **Health Check**: `http://localhost:8885/actuator/health`

## ⚙️ Cấu hình môi trường

### Profiles

Ứng dụng hỗ trợ 4 profiles:

| Profile | File                    | Mục đích        |
| ------- | ----------------------- | --------------- |
| `dev`   | `application-dev.yaml`  | Development     |
| `dev2`  | `application-dev2.yaml` | Dev alternative |
| `test`  | `application-test.yaml` | Testing         |
| `prod`  | `application-prod.yaml` | Production      |

### Environment Variables

Thiết lập các biến môi trường sau:

```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=cineshow
export DB_USER=root
export DB_PASS=your_password

# Google OAuth2
export GOOGLE_CLIENT_ID=your_client_id
export GOOGLE_CLIENT_SECRET=your_client_secret

# AWS
export AWS_ACCESS_KEY=your_access_key
export AWS_SECRET_KEY=your_secret_key

# SendGrid
export SENDGRID_API_KEY=your_api_key
```

### Redis Configuration

Redis được sử dụng cho:

- Session storage
- Seat holding (tạm giữ ghế)
- Cache

Cấu hình trong `application-dev.yaml`:

```yaml
spring:
  data:
    redis:
      host: redis-16466.c1.ap-southeast-1-1.ec2.redns.redis-cloud.com
      port: 16466
      username: default
      password: your_password
```

**Hoặc sử dụng Redis local:**

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 🧪 Chạy Tests

### Chạy tất cả tests

```bash
mvn clean test
```

### Chạy test với profile cụ thể

```bash
mvn clean test -Ptest
```

### Chạy test class cụ thể

```bash
mvn test -Dtest=MovieControllerTest
```

### Test coverage

```bash
mvn clean test jacoco:report
```

Xem báo cáo: `target/site/jacoco/index.html`

**Lưu ý**: Tests sử dụng H2 in-memory database và Redis Cloud (database index 1).

Xem thêm: [TEST_ENVIRONMENT.md](./TEST_ENVIRONMENT.md)

## 📚 API Documentation

### Swagger UI

Truy cập: `http://localhost:8885/swagger-ui.html`

### Các controllers chính

- **AuthenticationController** (`/auth`) - Login, Register, Refresh Token
- **UserController** (`/users`) - User management
- **MovieController** (`/movies`) - Movie CRUD
- **ShowTimeController** (`/showtimes`) - Showtime management
- **BookingController** (`/bookings`) - Booking flow
- **OrderController** (`/orders`) - Order management
- **RoomController** (`/rooms`) - Room management
- **SeatController** (`/seats`) - Seat management
- **ConcessionController** (`/concessions`) - Food & beverages
- **TicketPriceController** (`/ticket-prices`) - Pricing

### Authentication

Hầu hết các endpoints yêu cầu JWT token:

```bash
Authorization: Bearer <access_token>
```

### Ví dụ request

**Login:**

```bash
POST /auth/log-in
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**

```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "email": "user@example.com",
    "roleName": "CUSTOMER"
  }
}
```

**Get Movies:**

```bash
GET /movies/list-with-filter-many-column-and-sortBy?pageNo=1&pageSize=10
Authorization: Bearer <token>
```

## 🏗️ Kiến trúc

### Project Structure

```
src/
├── main/
│   ├── java/vn/cineshow/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   │   └── impl/        # Service implementations
│   │   ├── repository/      # Data access layer
│   │   ├── model/           # Entity models
│   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── request/     # Request DTOs
│   │   │   └── response/    # Response DTOs
│   │   ├── enums/           # Enumerations
│   │   ├── exception/       # Exception handling
│   │   └── utils/           # Utilities
│   └── resources/
│       ├── application.yml  # Base configuration
│       ├── application-dev.yaml
│       ├── application-test.yaml
│       └── *.sql            # SQL scripts
└── test/
    └── java/vn/cineshow/
        ├── controller/      # Controller tests
        └── *.java           # Integration tests
```

### Design Patterns

- **MVC** - Model-View-Controller
- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Data transfer objects
- **Service Layer** - Business logic separation
- **Singleton** - Configuration beans
- **Strategy** - Payment processing

### Security Flow

```
User Request
    ↓
SecurityFilterChain
    ↓
JWT Authentication
    ↓
Role-based Authorization
    ↓
Controller → Service → Repository → Database
```

### Real-time Seat Selection

```
Client → WebSocket Handshake
    ↓
SeatWebSocketController
    ↓
Redis (Hold Seat)
    ↓
Broadcast to All Clients
    ↓
TTL Expires → Auto Release
```

## 🔧 Troubleshooting

### Port already in use

```bash
# Windows
netstat -ano | findstr :8885
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8885
kill -9 <PID>
```

### Database connection error

- Kiểm tra MySQL đang chạy
- Kiểm tra credentials trong config
- Kiểm tra network/firewall

### Redis connection error

- Kiểm tra Redis đang chạy
- Kiểm tra host, port, password
- Test connection: `redis-cli ping`

### JWT token expired

- Request `/auth/refresh-token` với refresh token
- Hoặc login lại để lấy token mới

### Missing environment variables

- Kiểm tra `.env` hoặc IDE environment settings
- Các biến môi trường được đọc từ system/env

### H2 Console not accessible

- Kiểm tra `spring.h2.console.enabled=true`
- Truy cập: `http://localhost:8885/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`

## 📝 Additional Resources

- [TEST_ENVIRONMENT.md](./TEST_ENVIRONMENT.md) - Test environment setup
- [API Documentation](http://localhost:8885/swagger-ui.html) - Swagger docs
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security Docs](https://spring.io/projects/spring-security)

## 👥 Contributors

Nhóm phát triển CineShow

## 📄 License

Copyright © 2024. All rights reserved.

## 🙏 Acknowledgments

- Spring Boot Team
- Redis Labs
- AWS
- SendGrid

---

**Happy Coding! 🎉**
