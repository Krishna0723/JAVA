# Migration Guide: Node.js/Express to Spring Boot

This guide explains the mapping between your original Node.js/Express application and the new Spring Boot implementation.

## Architecture Comparison

### Node.js Structure
```
server/
├── index.js              → UserAuthServiceApplication.java
├── controller/
│   ├── controller.auth.js         → AuthController.java
│   └── controller.signup.js       → SignupController.java
├── model/
│   └── model.userDetails.js       → User.java (Entity)
└── view/
    ├── view.auth.js              → AuthController.java (routing)
    └── view.signup.js            → SignupController.java (routing)
```

### Spring Boot Structure
```
springboot-server/
├── src/main/java/com/app/
│   ├── UserAuthServiceApplication.java
│   ├── controller/       → API endpoints
│   ├── service/          → Business logic (NEW)
│   ├── repository/       → Data access (NEW)
│   ├── model/            → Entities
│   ├── security/         → Security & JWT (NEW)
│   └── dto/              → Data Transfer Objects (NEW)
└── src/main/resources/
    └── application.properties → Configuration
```

## Code Mapping

### 1. Main Application Entry Point

**Before (Node.js):**
```javascript
// index.js
const express = require("express");
const app = express();
// ... middleware setup ...
app.listen(4000, () => {
  console.log("started at 4000");
});
```

**After (Spring Boot):**
```java
// UserAuthServiceApplication.java
@SpringBootApplication
public class UserAuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserAuthServiceApplication.class, args);
    }
}
```

### 2. Controller Routing

**Before (Node.js):**
```javascript
// controller.auth.js
exports.login = async (req, res) => { ... };
exports.profile = async (req, res) => { ... };

// view.auth.js
auth.post("/", authController.login);
auth.get("/", authController.profile);

// index.js
app.use("/login", authView);
```

**After (Spring Boot):**
```java
// AuthController.java
@RestController
@RequestMapping("/login")
public class AuthController {
    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) { ... }
    
    @GetMapping
    public ResponseEntity<?> profile() { ... }
}
```

### 3. Database Models

**Before (Node.js - Mongoose):**
```javascript
// model.userDetails.js
const userDet = new mongoose.Schema({
    name: { type: String, required: true },
    email: { type: String, required: true },
    password: { type: String, required: true },
    phonenumber: { type: String, required: true },
}, { collection: "user" });
module.exports = mongoose.model("User", userDet);
```

**After (Spring Boot - Spring Data MongoDB):**
```java
// User.java
@Document(collection = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String phonenumber;
}
```

### 4. Database Access

**Before (Node.js - Mongoose):**
```javascript
const user = await userSchema.findOne({ email: data.email });
const users = await userSchema.find();
const resp = await userSchema.insertOne(data);
```

**After (Spring Boot - Repository):**
```java
// UserRepository.java extends MongoRepository
Optional<User> user = userRepository.findByEmail(email);
List<User> users = userRepository.findAll();
User resp = userRepository.save(user);
```

### 5. Password Handling

**Before (Node.js - bcrypt):**
```javascript
const bcrypt = require("bcrypt");
data.password = await bcrypt.hash(data.password, 10);
const password = await bcrypt.compare(data.password, user.password);
```

**After (Spring Boot - BCryptPasswordEncoder):**
```java
@Autowired
private PasswordEncoder passwordEncoder;

// Hashing
user.setPassword(passwordEncoder.encode(user.getPassword()));

// Verification
passwordEncoder.matches(rawPassword, encodedPassword);
```

### 6. JWT Token Generation

**Before (Node.js - jsonwebtoken):**
```javascript
const jwt = require("jsonwebtoken");
const token = jwt.sign(
  { userId: user._id, email: user.email },
  process.env.SECRET_KEY,
  { expiresIn: "1h" }
);
```

**After (Spring Boot - JJWT):**
```java
public String generateToken(String userId, String email) {
    return Jwts.builder()
            .subject(userId)
            .claim("email", email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
}
```

### 7. JWT Verification

**Before (Node.js):**
```javascript
const verifyToken = async (req, res, next) => {
  const authHeader = await req.headers.authorization;
  if (!authHeader || !authHeader.startsWith("Bearer ")) {
    return res.status(401).json({ message: "No token provided" });
  }
  const token = authHeader.split(" ")[1];
  try {
    const decoded = jwt.verify(token, process.env.SECRET_KEY);
    return decoded;
  } catch (err) {
    return res.status(403).json({ message: "Invalid or expired token" });
  }
};
```

**After (Spring Boot - Filter):**
```java
// JwtFilter.java extends OncePerRequestFilter
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            String userId = jwtUtil.getUserIdFromToken(token);
            // Set in SecurityContext
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userId, null, null);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }
    filterChain.doFilter(request, response);
}
```

## Key Configuration Changes

### Environment Variables

**Before (Node.js):**
```env
DB=mongodb://localhost:27017/user-auth-db
SECRET_KEY=your-secret-key
```

**After (Spring Boot):**
```properties
# application.properties
spring.data.mongodb.uri=mongodb://localhost:27017/user-auth-db
jwt.secret=your-secret-key
jwt.expiration=3600000
```

## API Endpoint Changes

All endpoints remain functionally the same, but response format may differ slightly:

| Endpoint | Method | Before | After | Status |
|----------|--------|--------|-------|--------|
| /login | POST | Returns `{user, token}` | Returns JSON object with `user` and `token` | ✅ Same |
| /login | GET | Returns user profile | Returns JSON with profile info | ✅ Same |
| /signup | POST | Creates user | Creates user | ✅ Same |
| /signup | GET | Lists all users | Lists all users | ✅ Same |

## Error Handling

**Before (Node.js):**
```javascript
if (!user) {
    res.status(400).json({ message: "User not found" });
} else {
    res.status(200).send(resp);
}
```

**After (Spring Boot):**
```java
if (user.isEmpty()) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
        Map.of("message", "User not found")
    );
} else {
    return ResponseEntity.status(HttpStatus.OK).body(resp);
}
```

## Development Workflow

### Node.js
```bash
npm install
npm start
```

### Spring Boot
```bash
mvn clean install
mvn spring-boot:run
```

## Testing API Calls

### Using cURL

```bash
# Login
curl -X POST http://localhost:4000/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Get Profile (with token)
curl -X GET http://localhost:4000/login \
  -H "Authorization: Bearer <token>"

# Signup
curl -X POST http://localhost:4000/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"john@example.com","password":"pass","phonenumber":"123"}'

# Get All Users
curl -X GET http://localhost:4000/signup
```

### Using Postman

1. Import the collection from your API documentation
2. Update the base URL to `http://localhost:4000`
3. Use the `Bearer <token>` format for protected endpoints

## Performance Considerations

### Spring Boot Advantages
1. **Compiled Bytecode**: Faster execution than interpreted JavaScript
2. **Built-in Caching**: Spring caching framework
3. **Connection Pooling**: Automatic DB connection pooling
4. **Type Safety**: Compile-time error detection

### Potential Differences
1. **Startup Time**: Spring Boot takes longer to start than Node.js
2. **Memory Usage**: JVM uses more base memory
3. **First Request**: May be slower due to JIT compilation

## Common Issues & Solutions

### Issue: MongoDB Connection Failed
```
Solution: Ensure MongoDB is running on localhost:27017 or update connection string
```

### Issue: JWT Token Invalid
```
Solution: Check that jwt.secret in application.properties matches expectations
```

### Issue: CORS Errors
```
Solution: Modify corsConfigurationSource() in SecurityConfig.java
```

### Issue: Password Mismatch
```
Solution: Ensure passwords are encoded before comparison using PasswordEncoder
```

## Rollback Plan

If you need to revert to Node.js:
1. All data remains in MongoDB (compatible format)
2. API endpoints are identical
3. Simply restart Node.js application

## Next Steps

1. **Testing**: Run unit and integration tests
2. **Performance Testing**: Load test the new Spring Boot app
3. **Gradual Migration**: Consider running both versions in parallel
4. **Database Backup**: Backup MongoDB before full cutover
5. **Monitoring**: Set up logging and monitoring for production

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data MongoDB](https://spring.io/projects/spring-data-mongodb)
- [JJWT Library](https://github.com/jwtk/jjwt)
- [Spring Security](https://spring.io/projects/spring-security)
