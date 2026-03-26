# Quick Start Guide

Get the Spring Boot application running in 5 minutes.

## Prerequisites Check

```bash
# Check Java version (should be 17+)
java -version

# Check Maven version (should be 3.9+)
mvn -version
```

If missing, install from:
- Java: https://www.oracle.com/java/technologies/downloads/
- Maven: https://maven.apache.org/download.cgi

## Step 1: Start MongoDB

### Option A: Local MongoDB
```bash
mongod
```

### Option B: Docker MongoDB
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

## Step 2: Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Update MongoDB URI if needed
spring.data.mongodb.uri=mongodb://localhost:27017/user-auth-db

# Change JWT secret (IMPORTANT!)
jwt.secret=your-secure-random-secret-key
```

## Step 3: Build the Project

```bash
# Clean and build
mvn clean install

# Or with tests
mvn clean install -DskipTests
```

## Step 4: Run the Application

```bash
# Using Maven
mvn spring-boot:run

# Or run the built JAR
java -jar target/user-auth-service-1.0.0.jar
```

**Expected Output:**
```
Started UserAuthServiceApplication in X seconds
```

## Step 5: Test the API

### Test Signup
```bash
curl -X POST http://localhost:4000/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "phonenumber": "1234567890"
  }'
```

**Expected Response:**
```json
{
  "id": "...",
  "name": "John Doe",
  "email": "john@example.com",
  "phonenumber": "1234567890",
  "password": ""
}
```

### Test Login
```bash
curl -X POST http://localhost:4000/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "user": {
    "id": "...",
    "name": "John Doe",
    "email": "john@example.com",
    "phonenumber": "1234567890",
    "password": ""
  },
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Test Protected Endpoint
```bash
# Replace <TOKEN> with the token from login response
curl -X GET http://localhost:4000/login \
  -H "Authorization: Bearer <TOKEN>"
```

**Expected Response:**
```json
{
  "message": "Protected profile",
  "userId": "...",
  "email": "john@example.com"
}
```

### Get All Users
```bash
curl -X GET http://localhost:4000/signup
```

## Development Tips

### IDE Setup

#### IntelliJ IDEA
1. Open project folder
2. Right-click → "Configure" → "Convert to Maven Project" (if needed)
3. Enable Lombok: Settings → Build → Compiler → Annotation Processors → Enable annotation processing

#### VS Code
1. Install "Extension Pack for Java"
2. Install "Spring Boot Extension Pack"
3. Open terminal: `Ctrl+` or View → Terminal

### Hot Reload (Auto-restart on Changes)
Spring Boot DevTools is included. Just:
1. Make code changes
2. Press `Ctrl+Shift+F9` (or Save in IDE) to trigger recompilation and restart

### View Application Logs
Logs are output to console. Set log level in `application.properties`:

```properties
logging.level.com.app=DEBUG
logging.level.org.springframework=INFO
```

### Database Connection Testing

```bash
# Using MongoDB CLI
mongosh
> use user-auth-db
> db.user.find()
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Port 4000 already in use | Change `server.port=8080` in application.properties |
| Cannot connect to MongoDB | Ensure MongoDB is running on port 27017 |
| Maven build fails | Run `mvn clean -X install` to see detailed error logs |
| Lombok errors | Enable annotation processing in your IDE settings |
| JWT token invalid | Ensure `jwt.secret` is set and consistent |

## Project Structure Reference

```
springboot-server/
├── pom.xml                 # Dependencies and build config
├── src/
│   ├── main/
│   │   ├── java/com/app/
│   │   │   ├── UserAuthServiceApplication.java  # Entry point
│   │   │   ├── controller/   # REST endpoints
│   │   │   ├── service/      # Business logic
│   │   │   ├── model/        # Data entities
│   │   │   ├── repository/   # Database access
│   │   │   ├── security/     # Security & JWT
│   │   │   └── dto/          # Data transfer objects
│   │   └── resources/
│   │       └── application.properties  # Configuration
│   └── test/
│       └── java/com/app/     # Unit tests
├── README.md               # Full documentation
├── MIGRATION_GUIDE.md      # Node.js to Spring Boot mapping
└── QUICK_START.md          # This file

```

## Next Steps

1. **Read Documentation**: Check [README.md](README.md) for full API documentation
2. **Review Changes**: Check [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) to understand mapping from Node.js
3. **Add Tests**: Create tests in `src/test/java/` directory
4. **Deploy**: Use Docker or cloud platform for production deployment
5. **Monitor**: Set up logging and monitoring

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) for Node.js to Spring Boot mapping
3. Check Spring Boot documentation: https://spring.io/projects/spring-boot
4. Check Spring Data MongoDB docs: https://spring.io/projects/spring-data-mongodb

---

**Happy coding!** 🚀
