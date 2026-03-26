# User Authentication Service - Spring Boot Refactoring

This is a Spring Boot refactoring of the original Node.js/Express backend application. It provides user authentication and management functionality using MongoDB.

## Project Structure

```
springboot-server/
├── src/main/
│   ├── java/com/app/
│   │   ├── controller/        # REST endpoints
│   │   │   ├── AuthController.java      # Login & profile endpoints
│   │   │   └── SignupController.java    # Signup & user list endpoints
│   │   ├── model/             # Entity classes
│   │   │   └── User.java
│   │   ├── repository/        # Data access layer
│   │   │   └── UserRepository.java
│   │   ├── service/           # Business logic
│   │   │   ├── AuthService.java
│   │   │   └── SignupService.java
│   │   ├── security/          # Security & JWT implementation
│   │   │   ├── JwtUtil.java           # JWT token generation/validation
│   │   │   ├── JwtFilter.java         # JWT authentication filter
│   │   │   └── SecurityConfig.java    # Spring Security configuration
│   │   ├── dto/               # Data Transfer Objects
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   └── SignupRequest.java
│   │   └── UserAuthServiceApplication.java  # Main application class
│   └── resources/
│       └── application.properties  # Environment configuration
└── pom.xml                    # Maven configuration

```

## Prerequisites

- Java 17 or higher
- Maven 3.9+
- MongoDB 4.0+

## Getting Started

### 1. Clone or Setup MongoDB

Make sure MongoDB is running:

```bash
# For local MongoDB
mongod
```

### 2. Configure Database Connection

Update `src/main/resources/application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/user-auth-db
```

### 3. Configure JWT Secret

Update the JWT secret in `application.properties` (IMPORTANT for production):

```properties
jwt.secret=your-super-secret-key-change-this-in-production-environment
jwt.expiration=3600000
```

### 4. Build the Application

```bash
mvn clean install
```

### 5. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:4000`

## API Endpoints

### Authentication

**Login**
```http
POST /login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response (200 OK):
{
  "user": {
    "id": "...",
    "name": "...",
    "email": "...",
    "phonenumber": "...",
    "password": ""
  },
  "token": "eyJhbGc..."
}
```

**Profile (Protected)**
```http
GET /login
Authorization: Bearer <token>

Response (200 OK):
{
  "message": "Protected profile",
  "userId": "...",
  "email": "..."
}
```

### User Management

**Signup**
```http
POST /signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "phonenumber": "1234567890"
}

Response (200 OK):
{
  "id": "...",
  "name": "John Doe",
  "email": "john@example.com",
  "phonenumber": "1234567890",
  "password": ""
}
```

**Get All Users**
```http
GET /signup

Response (200 OK):
[
  {
    "id": "...",
    "name": "...",
    "email": "...",
    "phonenumber": "...",
    "password": ""
  }
]
```

## Key Differences from Node.js Version

| Feature | Node.js | Spring Boot |
|---------|---------|------------|
| Framework | Express.js | Spring Boot Web |
| DB Wrapper | Mongoose | Spring Data MongoDB |
| Password Hashing | bcrypt | BCryptPasswordEncoder |
| JWT | jsonwebtoken | JJWT |
| Dependency Injection | Manual | Spring IoC Container |
| Configuration | .env file | application.properties |
| Type Safety | Dynamic | Static Typing |
| Server Port | 4000 | 4000 (configurable) |

## Security Features

1. **Password Hashing**: Uses BCryptPasswordEncoder for secure password storage
2. **JWT Authentication**: Time-limited tokens (default 1 hour) for API endpoints
3. **CORS Support**: Configured to allow cross-origin requests
4. **Spring Security**: Integrated for authentication and authorization

## Configuration Options

### MongoDB

```properties
# Connection string method (recommended)
spring.data.mongodb.uri=mongodb://localhost:27017/user-auth-db

# OR individual properties
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
spring.data.mongodb.database=user-auth-db
spring.data.mongodb.username=user
spring.data.mongodb.password=password
```

### JWT

```properties
jwt.secret=your-secret-key
jwt.expiration=3600000  # 1 hour in milliseconds
```

### CORS

CORS is enabled for all origins by default. To restrict in production:

Edit `SecurityConfig.java` and modify `corsConfigurationSource()` method.

## Testing

Run tests with:

```bash
mvn test
```

## Deployment

### Build for Production

```bash
mvn clean package -DskipTests
```

This creates an executable JAR in `target/user-auth-service-1.0.0.jar`

### Run JAR

```bash
java -jar target/user-auth-service-1.0.0.jar
```

### Docker (Optional)

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/user-auth-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```bash
docker build -t user-auth-service .
docker run -p 4000:4000 user-auth-service
```

## Migration Notes

1. **Password Security**: Passwords are hashed using BCrypt (stronger than original if using outdated version)
2. **Type Safety**: Spring's type system prevents many runtime errors
3. **Dependency Injection**: Automatic with Spring IoC container
4. **Error Handling**: More structured with ResponseEntity and HTTP status codes
5. **Logging**: Uses SLF4J with Logback for better logging

## Troubleshooting

**Cannot connect to MongoDB**
- Ensure MongoDB is running: `mongod`
- Check connection string in application.properties
- Verify MongoDB port (default 27017)

**JWT token invalid**
- Ensure `jwt.secret` is the same across restarts
- Check token expiration time

**CORS errors**
- Verify CORS configuration in SecurityConfig.java
- Check allowed origins and methods

## Next Steps

1. Add input validation with Spring Validation
2. Implement custom error responses
3. Add logging and monitoring
4. Set up unit and integration tests
5. Implement refresh token mechanism
6. Add email verification
7. Add password reset functionality
8. Implement role-based access control (RBAC)
