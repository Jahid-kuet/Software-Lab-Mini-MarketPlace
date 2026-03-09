# Mini Marketplace

A full-stack web application built with Spring Boot that allows users to buy and sell products online. Supports three roles: Admin, Seller, and Buyer.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Security | Spring Security 6 (BCrypt, Form Login) |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL (production), H2 (tests) |
| Templates | Thymeleaf + thymeleaf-extras-springsecurity6 |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| Testing | JUnit 5, Mockito, MockMvc |

---

## Project Structure

```
src/main/java/com/jahid/minimarketplace/
├── config/
│   └── SecurityConfig.java          # Spring Security configuration
├── controller/
│   ├── AuthController.java          # Register / Login
│   ├── ProductController.java       # Product CRUD
│   ├── OrderController.java         # Order placement and history
│   ├── AdminController.java         # Admin management
│   └── DashboardController.java     # Role-based dashboard redirect
├── dto/
│   ├── RegisterRequest.java
│   ├── LoginRequest.java
│   ├── UserDTO.java
│   ├── ProductRequest.java
│   ├── ProductDTO.java
│   ├── OrderRequest.java
│   ├── OrderDTO.java
│   └── ApiResponse.java             # Generic API response wrapper
├── entity/
│   ├── Role.java                    # RoleName enum: ROLE_ADMIN, ROLE_SELLER, ROLE_BUYER
│   ├── User.java
│   ├── Product.java
│   ├── Order.java                   # OrderStatus enum: PENDING → DELIVERED
│   └── OrderItem.java
├── exception/
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── InsufficientStockException.java
│   └── GlobalExceptionHandler.java  # @RestControllerAdvice
├── repository/
│   ├── UserRepository.java
│   ├── RoleRepository.java
│   ├── ProductRepository.java
│   ├── OrderRepository.java
│   └── OrderItemRepository.java
└── service/
    ├── CustomUserDetailsService.java
    ├── UserService.java
    ├── ProductService.java
    ├── OrderService.java
    ├── AdminService.java
    └── DataInitializer.java         # Seeds roles on startup

src/main/resources/
├── application.properties           # Local config (gitignored)
├── application.properties.example   # Template — copy and fill in your values
└── templates/
    ├── layout.html
    ├── index.html
    ├── auth/         login.html, register.html
    ├── product/      list.html, detail.html, create.html, edit.html, my-products.html
    ├── order/        list.html, detail.html, checkout.html
    ├── admin/        dashboard.html, users.html, products.html, orders.html
    └── error/        403.html, 500.html
```

---

## Role Permissions

| Feature | Admin | Seller | Buyer |
|---|:---:|:---:|:---:|
| View all products | ✅ | ✅ | ✅ |
| Create / edit product | ❌ | ✅ | ❌ |
| Place an order | ❌ | ❌ | ✅ |
| View own orders | ❌ | ❌ | ✅ |
| Manage all users | ✅ | ❌ | ❌ |
| Manage all orders | ✅ | ❌ | ❌ |
| Admin dashboard | ✅ | ❌ | ❌ |

---

## Local Setup (Without Docker)

### Prerequisites
- Java 17
- Maven
- PostgreSQL running locally

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Jahid-kuet/Software-Lab-Mini-MarketPlace.git
cd Software-Lab-Mini-MarketPlace

# 2. Switch to develop branch
git checkout develop

# 3. Create your local application.properties from the example
copy src\main\resources\application.properties.example src\main\resources\application.properties
# Edit application.properties with your PostgreSQL credentials

# 4. Create the database in PostgreSQL
psql -U postgres -c "CREATE DATABASE minimarketplace;"

# 5. Run the application
./mvnw spring-boot:run
```

App runs at: `http://localhost:8080`

> On first startup, `DataInitializer` automatically seeds the three roles (`ROLE_ADMIN`, `ROLE_SELLER`, `ROLE_BUYER`) into the database.

---

## API Endpoints

### Auth
| Method | URL | Description |
|---|---|---|
| GET | `/auth/login` | Login page |
| POST | `/auth/login` | Submit login |
| GET | `/auth/register` | Register page |
| POST | `/auth/register` | Submit registration |
| POST | `/auth/logout` | Logout |

### Products
| Method | URL | Access |
|---|---|---|
| GET | `/products` | Public |
| GET | `/products/{id}` | Public |
| GET | `/products/my-products` | Seller |
| GET | `/products/create` | Seller |
| POST | `/products/create` | Seller |
| GET | `/products/{id}/edit` | Seller (owner) |
| POST | `/products/{id}/edit` | Seller (owner) |
| POST | `/products/{id}/delete` | Seller (owner) |

### Orders
| Method | URL | Access |
|---|---|---|
| GET | `/orders` | Buyer |
| GET | `/orders/{id}` | Buyer (owner) |
| GET | `/orders/checkout` | Buyer |
| POST | `/orders/checkout` | Buyer |

### Admin
| Method | URL | Access |
|---|---|---|
| GET | `/admin/dashboard` | Admin |
| GET | `/admin/users` | Admin |
| POST | `/admin/users/{id}/toggle` | Admin |
| GET | `/admin/products` | Admin |
| POST | `/admin/products/{id}/delete` | Admin |
| GET | `/admin/orders` | Admin |
| POST | `/admin/orders/{id}/status` | Admin |

---

## Git Workflow

```
main          ← production only (owner merges here)
  └── develop ← integration branch (PRs merge here)
        └── feature/project-setup   ← Steps 1–8 (this branch)
        └── feature/step9-*         ← teammate's branches
```

- **Never push directly to `main`**
- All feature branches merge into `develop` via Pull Request
- Only the repo owner merges `develop` → `main`

---

## Branch: `feature/project-setup` — What Was Done

| Step | Description |
|---|---|
| Step 1 | `pom.xml` fixed (Spring Boot 3.2.5), `application.properties` configured, folder structure |
| Step 2 | 5 JPA Entities with relationships (`Role`, `User`, `Product`, `Order`, `OrderItem`) |
| Step 3 | 5 Spring Data JPA Repositories with custom query methods |
| Step 4 | 8 DTOs with Bean Validation annotations |
| Step 5 | 6 Service classes + 3 custom exception classes + `DataInitializer` |
| Step 6 | `SecurityConfig` — BCrypt, form login, role-based URL security, method security |
| Step 7 | 5 Controllers + 15 Thymeleaf templates |
| Step 8 | `GlobalExceptionHandler` — handles 400/401/403/404/409/422/500 |

---

## Remaining Steps (Teammate)

| Step | Description |
|---|---|
| Step 9 | Unit Tests (min 15) — `@ExtendWith(MockitoExtension.class)` |
| Step 10 | Integration Tests (min 3) — `@SpringBootTest`, MockMvc |
| Step 11 | Dockerfile + docker-compose.yml |
| Step 12 | GitHub Actions CI/CD pipeline |
| Step 13 | Final README update |
