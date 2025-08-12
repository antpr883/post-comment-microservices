# 🔐 План впровадження Auth Service та інтеграції автентифікації

## 📋 Загальний огляд

Цей документ описує покроковий план створення auth-service та оновлення існуючих сервісів для підтримки централізованої автентифікації з JWT токенами.

## 🎯 Цілі впровадження

### Основні задачі:
1. **Створити auth-service** з JWT автентифікацією
2. **Мігрувати user-service** - винести логіку автентифікації
3. **Оновити всі сервіси** для підтримки JWT валідації
4. **Додати API Gateway** для централізованого routing
5. **Забезпечити security** на всіх рівнях

### Переваги після впровадження:
- ✅ **Single Sign-On (SSO)** через всі сервіси
- ✅ **Централізована security** політика
- ✅ **Stateless authentication** з JWT
- ✅ **Role-based access control** (RBAC)
- ✅ **Token refresh** механізм
- ✅ **Production-ready** security

---

## 🏗️ Архітектурний план

### Поточна архітектура (AS-IS):
```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ user-service│    │post-service │    │comment-     │    │cache-service│
│             │    │             │    │service      │    │             │
│ (auth logic)│    │ (no auth)   │    │ (no auth)   │    │ (no auth)   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
       │                   │                   │                   │
       └───────────────────┼───────────────────┼───────────────────┘
                           │                   │
                    ┌─────────────────────────────┐
                    │    PostgreSQL/MongoDB       │
                    └─────────────────────────────┘
```

### Цільова архітектура (TO-BE):
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Mobile App    │    │   Web Client    │    │   Admin Panel   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │       API Gateway         │
                    │  (JWT validation,         │
                    │   rate limiting)          │
                    └─────────────┬─────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │      auth-service         │
                    │   (JWT issue/validate)    │
                    └─────────────┬─────────────┘
                                  │
                ┌─────────────────┼─────────────────┐
                │                 │                 │
    ┌───────────▼────┐  ┌─────────▼────┐  ┌────────▼─────┐
    │  user-service  │  │ post-service  │  │comment-      │
    │  (user mgmt)   │  │ (JWT auth)    │  │service       │
    └───────────┬────┘  └─────────┬────┘  │ (JWT auth)   │
                │                 │       └────────┬─────┘
    ┌───────────▼────┐            │                │
    │  cache-service │            │                │
    │  (JWT auth)    │            │                │
    └────────────────┘            │                │
                                  │                │
                    ┌─────────────▼─────────────────▼─┐
                    │         Redis Cluster           │
                    │    (JWT blacklist, cache)       │
                    └─────────────────────────────────┘
```

---

## 📅 Поетапний план впровадження

### **Phase 1: Створення auth-service (1-2 тижні)**

#### Week 1: Базова інфраструктура
```yaml
Day 1-2: Створення проекту
  - Spring Boot project setup
  - PostgreSQL + Redis підключення
  - Basic entity models (User, Role, RefreshToken)
  - Docker containers setup

Day 3-4: Core authentication
  - JWT utility класи
  - UserDetailsService імплементація
  - Password encoding (BCrypt)
  - Basic login/logout endpoints

Day 5-7: Token management
  - JWT generation та validation
  - Refresh token mechanism
  - Token blacklist в Redis
  - Security configuration
```

#### Week 2: Advanced features
```yaml
Day 8-10: Advanced auth
  - Password reset flow
  - Email verification
  - Rate limiting для login спроб
  - Account lockout mechanism

Day 11-12: API integration
  - REST API для інших сервісів
  - JWT validation endpoint
  - User info endpoint
  - Health checks

Day 13-14: Testing & Documentation
  - Unit tests для auth logic
  - Integration tests
  - API documentation
  - Security testing
```

### **Phase 2: User Service рефакторінг (1 тиждень)**

#### Мігрування user-service:
```yaml
Day 1-3: Видалення auth logic
  - Винести login/logout endpoints
  - Видалити password management
  - Залишити тільки user profile CRUD
  - Додати JWT validation filter

Day 4-5: Integration з auth-service
  - HTTP client для auth-service
  - User sync механізм
  - Role management integration
  - Testing integration

Day 6-7: Security cleanup
  - Оновити security configuration
  - Додати method-level security
  - Role-based access control
  - Final testing
```

### **Phase 3: Інші сервіси integration (1 тиждень)**

#### Post Service, Comment Service, Cache Service:
```yaml
Day 1-2: JWT validation додавання
  - Spring Security dependency
  - JWT validation filter
  - Security configuration
  - Protected endpoints marking

Day 3-4: User context integration
  - JWT claims extraction
  - User ID від токена
  - Role-based permissions
  - Audit logging integration

Day 5-7: Testing & validation
  - Integration testing з auth-service
  - End-to-end testing
  - Performance testing
  - Security vulnerability testing
```

### **Phase 4: API Gateway (1 тиждень)**

#### Spring Cloud Gateway setup:
```yaml
Day 1-3: Gateway infrastructure
  - Spring Cloud Gateway project
  - Route configuration
  - Load balancing setup
  - Service discovery integration

Day 4-5: Security integration
  - JWT validation на gateway рівні
  - Rate limiting configuration
  - CORS handling
  - Request/response logging

Day 6-7: Final integration
  - End-to-end testing
  - Performance optimization
  - Production deployment setup
  - Monitoring integration
```

---

## 🔧 Детальна технічна специфікація

### 1. **Auth Service архітектура**

#### Проект структура:
```
auth-service/
├── src/main/java/com/andev/auth/
│   ├── AuthServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── JwtConfig.java
│   │   └── RedisConfig.java
│   ├── model/
│   │   ├── entities/
│   │   │   ├── User.java
│   │   │   ├── Role.java
│   │   │   └── RefreshToken.java
│   │   └── dto/
│   │       ├── LoginRequest.java
│   │       ├── LoginResponse.java
│   │       ├── TokenResponse.java
│   │       └── UserProfile.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   └── RefreshTokenRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JwtService.java
│   │   ├── TokenBlacklistService.java
│   │   └── UserService.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtTokenProvider.java
│   │   └── CustomUserDetailsService.java
│   └── web/
│       ├── AuthController.java
│       ├── UserController.java
│       └── TokenValidationController.java
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    └── db/changelog/
```

#### Core entities:
```java
// User entity (simplified for auth)
@Entity
@Table(name = "users", schema = "v1_auth")
public class User extends BasePersistenceModel {
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @Column(name = "email_verified")
    private boolean emailVerified = false;
    
    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles")
    private Set<Role> roles = new HashSet<>();
}

// RefreshToken entity
@Entity
@Table(name = "refresh_tokens", schema = "v1_auth") 
public class RefreshToken extends BasePersistenceModel {
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "is_revoked")
    private boolean revoked = false;
}
```

#### JWT Configuration:
```java
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {
    
    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties properties) {
        return new JwtTokenProvider(
            properties.getSecret(),
            properties.getAccessTokenExpiration(),
            properties.getRefreshTokenExpiration()
        );
    }
}

// JWT Properties
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtProperties {
    private String secret = "mySecretKey"; // З environment в prod
    private long accessTokenExpiration = 900000; // 15 хвилин
    private long refreshTokenExpiration = 604800000; // 7 днів
    private String issuer = "auth-service";
}
```

#### JWT Service Implementation:
```java
@Service
@Slf4j
public class JwtService {
    
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService blacklistService;
    private final RefreshTokenRepository refreshTokenRepository;
    
    public TokenResponse generateTokens(User user) {
        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);
        
        // Зберегти refresh token в БД
        saveRefreshToken(user, refreshToken);
        
        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getAccessTokenExpiration())
            .build();
    }
    
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token) && 
               !blacklistService.isBlacklisted(token);
    }
    
    public TokenResponse refreshTokens(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
            
        if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token expired or revoked");
        }
        
        // Генерувати нові токени
        return generateTokens(token.getUser());
    }
    
    public void logout(String accessToken, String refreshToken) {
        // Додати access token в blacklist
        blacklistService.blacklistToken(accessToken);
        
        // Відкликати refresh token
        refreshTokenRepository.findByToken(refreshToken)
            .ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
    }
}
```

#### Auth Controller:
```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final JwtService jwtService;
    
    @PostMapping("/login")
    public ResponseEntity<AppResponse<TokenResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("Login attempt for email: {}", request.getEmail());
        
        User user = authService.authenticate(request.getEmail(), request.getPassword());
        TokenResponse tokens = jwtService.generateTokens(user);
        
        return ResponseEntity.ok(AppResponse.success("Login successful", tokens));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AppResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        TokenResponse tokens = jwtService.refreshTokens(request.getRefreshToken());
        return ResponseEntity.ok(AppResponse.success("Tokens refreshed", tokens));
    }
    
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody LogoutRequest request) {
        
        String accessToken = authHeader.substring(7); // Remove "Bearer "
        jwtService.logout(accessToken, request.getRefreshToken());
        
        return ResponseEntity.ok(AppResponse.success("Logout successful"));
    }
    
    @GetMapping("/validate")
    public ResponseEntity<AppResponse<UserProfile>> validateToken(
            @RequestHeader("Authorization") String authHeader) {
        
        String token = authHeader.substring(7);
        
        if (!jwtService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AppResponse.error("Invalid token"));
        }
        
        UserProfile userProfile = authService.getUserProfileFromToken(token);
        return ResponseEntity.ok(AppResponse.success("Token valid", userProfile));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<AppResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        authService.initiateForgotPassword(request.getEmail());
        return ResponseEntity.ok(AppResponse.success("Password reset email sent"));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<AppResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(AppResponse.success("Password reset successful"));
    }
}
```

### 2. **Оновлення існуючих сервісів**

#### Загальний Security Configuration для всіх сервісів:
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> 
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/*/health", "/api/*/actuator/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/search").permitAll()  // Public search
                .requestMatchers(HttpMethod.GET, "/api/comments/post/**").permitAll()  // Public comments
                
                // Protected endpoints
                .requestMatchers(HttpMethod.POST, "/api/posts/**").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/posts/**").hasRole("USER") 
                .requestMatchers(HttpMethod.DELETE, "/api/posts/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/comments/**").hasRole("USER")
                .requestMatchers("/api/users/**").hasRole("USER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

#### JWT Authentication Filter:
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final AuthServiceClient authServiceClient;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractTokenFromRequest(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // Валідація токена через auth-service
                UserProfile userProfile = authServiceClient.validateToken(token);
                
                if (userProfile != null) {
                    // Створити аутентифікацію
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userProfile, null, userProfile.getAuthorities());
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

#### Auth Service Client (Feign):
```java
@FeignClient(name = "auth-service", url = "${app.auth-service.url}")
public interface AuthServiceClient {
    
    @GetMapping("/api/auth/validate")
    UserProfile validateToken(@RequestHeader("Authorization") String token);
    
    @GetMapping("/api/auth/user/{userId}")
    UserProfile getUserById(@PathVariable Long userId);
}

// Configuration
@Configuration
@EnableFeignClients
public class FeignConfig {
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            // Додати service-to-service authentication
            template.header("X-Service-Name", "post-service");
            template.header("X-Service-Secret", "${app.service.secret}");
        };
    }
}
```

### 3. **User Service рефакторінг**

#### Видалення auth endpoints:
```java
// ❌ Видалити ці endpoints з UserController:
// POST /api/users/login
// POST /api/users/logout  
// POST /api/users/refresh
// POST /api/users/forgot-password
// POST /api/users/reset-password

// ✅ Залишити тільки user profile management:
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppResponse<UserDto>> getCurrentUserProfile() {
        // Отримати current user з SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserProfile userProfile = (UserProfile) auth.getPrincipal();
        
        UserDto user = userService.findById(userProfile.getId());
        return ResponseEntity.ok(AppResponse.success(user));
    }
    
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppResponse<UserDto>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request) {
        
        UserProfile currentUser = getCurrentUser();
        UserDto updated = userService.updateProfile(currentUser.getId(), request);
        
        return ResponseEntity.ok(AppResponse.success(updated));
    }
    
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AppResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        
        UserProfile currentUser = getCurrentUser();
        userService.changePassword(currentUser.getId(), request);
        
        return ResponseEntity.ok(AppResponse.success("Password changed successfully"));
    }
}
```

#### User Service Integration з Auth Service:
```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl {
    
    private final UserRepository userRepository;
    private final AuthServiceClient authServiceClient;
    
    public UserDto updateProfile(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);
        
        // Оновити локальні дані
        user.setNickname(request.getNickname());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user = userRepository.save(user);
        
        // Синхронізувати з auth-service якщо потрібно
        syncWithAuthService(user);
        
        return userMapper.toDto(user);
    }
    
    private void syncWithAuthService(User user) {
        try {
            // Опціонально - синхронізувати зміни з auth-service
            authServiceClient.updateUserProfile(user.getId(), 
                UserSyncRequest.from(user));
        } catch (Exception e) {
            log.warn("Failed to sync user {} with auth-service: {}", 
                user.getId(), e.getMessage());
        }
    }
}
```

---

## 🔒 Security Best Practices

### 1. **JWT Security**
```yaml
JWT Configuration:
  - Короткий access token lifetime (15 хвилин)
  - Довший refresh token lifetime (7 днів)  
  - Strong secret key (256-bit)
  - Token blacklist в Redis
  - Issuer validation
  - Algorithm validation (HS256)

Security Headers:
  - X-Content-Type-Options: nosniff
  - X-Frame-Options: DENY
  - X-XSS-Protection: 1; mode=block
  - Strict-Transport-Security: max-age=31536000
```

### 2. **Rate Limiting**
```java
@Component
public class AuthRateLimitingFilter implements Filter {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        if ("/api/auth/login".equals(httpRequest.getRequestURI())) {
            String clientIp = getClientIp(httpRequest);
            String key = "login_attempts:" + clientIp;
            
            String attempts = redisTemplate.opsForValue().get(key);
            int attemptCount = attempts != null ? Integer.parseInt(attempts) : 0;
            
            if (attemptCount >= 5) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.getWriter().write("Too many login attempts");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
```

### 3. **Account Security**
```java
@Service
public class AccountSecurityService {
    
    public void handleFailedLogin(String email, String ip) {
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            // Lock account after 5 failed attempts
            if (user.getFailedLoginAttempts() >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusHours(1));
                log.warn("Account locked for user: {} due to failed login attempts", email);
            }
            
            userRepository.save(user);
        }
        
        // Rate limit by IP
        incrementIpLoginAttempts(ip);
    }
    
    public void handleSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
```

---

## 🧪 Testing Strategy

### 1. **Auth Service Tests**
```java
@SpringBootTest
@TestPropertySource(properties = {
    "app.jwt.secret=testSecret",
    "spring.redis.host=localhost"
})
class AuthServiceIntegrationTest {
    
    @Test
    void shouldAuthenticateValidUser() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        
        // When
        ResponseEntity<AppResponse<TokenResponse>> response = 
            authController.login(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPayload().getAccessToken()).isNotNull();
    }
    
    @Test
    void shouldRejectInvalidCredentials() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");
        
        // When & Then
        assertThatThrownBy(() -> authController.login(request))
            .isInstanceOf(BadCredentialsException.class);
    }
    
    @Test
    void shouldRefreshValidToken() {
        // Test refresh token flow
    }
    
    @Test
    void shouldBlacklistTokenOnLogout() {
        // Test token blacklist functionality
    }
}
```

### 2. **Integration Tests**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostServiceAuthIntegrationTest {
    
    @MockBean
    private AuthServiceClient authServiceClient;
    
    @Test
    void shouldAllowAuthenticatedUserToCreatePost() {
        // Given
        when(authServiceClient.validateToken(anyString()))
            .thenReturn(createMockUserProfile());
        
        // When
        ResponseEntity<AppResponse<PostDto>> response = testRestTemplate
            .exchange("/api/posts", HttpMethod.POST, 
                createRequestWithAuth(createPostRequest()), 
                new ParameterizedTypeReference<AppResponse<PostDto>>() {});
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
    
    @Test
    void shouldRejectUnauthenticatedRequest() {
        // Test без Authorization header
    }
}
```

---

## 📊 Monitoring та Metrics

### 1. **Auth Service Metrics**
```java
@Component
public class AuthMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Timer tokenValidationTimer;
    
    public AuthMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.loginSuccessCounter = Counter.builder("auth.login.success")
            .description("Successful login attempts")
            .register(meterRegistry);
        this.loginFailureCounter = Counter.builder("auth.login.failure")
            .description("Failed login attempts")
            .register(meterRegistry);
        this.tokenValidationTimer = Timer.builder("auth.token.validation")
            .description("Token validation time")
            .register(meterRegistry);
    }
    
    public void recordSuccessfulLogin() {
        loginSuccessCounter.increment();
    }
    
    public void recordFailedLogin(String reason) {
        loginFailureCounter.increment(Tags.of("reason", reason));
    }
    
    public Timer.Sample startTokenValidation() {
        return Timer.start(meterRegistry);
    }
}
```

### 2. **Health Checks**
```java
@Component
public class AuthServiceHealthIndicator implements HealthIndicator {
    
    private final JwtTokenProvider tokenProvider;
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // Test JWT functionality
            String testToken = tokenProvider.generateTestToken();
            boolean isValid = tokenProvider.validateToken(testToken);
            
            if (!isValid) {
                return builder.down().withDetail("jwt", "Token validation failed").build();
            }
            
            // Test Redis connectivity
            redisTemplate.opsForValue().set("health_check", "ok", Duration.ofSeconds(10));
            String value = redisTemplate.opsForValue().get("health_check");
            
            if (!"ok".equals(value)) {
                return builder.down().withDetail("redis", "Redis connectivity failed").build();
            }
            
            return builder.up()
                .withDetail("jwt", "ok")
                .withDetail("redis", "ok")
                .build();
                
        } catch (Exception e) {
            return builder.down().withException(e).build();
        }
    }
}
```

---

## 🚀 Deployment та Production

### 1. **Environment Configuration**
```yaml
# application-prod.yml
app:
  jwt:
    secret: ${JWT_SECRET} # From environment/vault
    access-token-expiration: 900000  # 15 minutes
    refresh-token-expiration: 604800000  # 7 days
    issuer: "auth-service-prod"
    
  security:
    rate-limit:
      login-attempts: 5
      lockout-duration: 3600000  # 1 hour
      
  redis:
    cluster:
      nodes: ${REDIS_CLUSTER_NODES}
      password: ${REDIS_PASSWORD}
      
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### 2. **Docker Configuration**
```dockerfile
# auth-service/Dockerfile
FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} auth-service.jar

# Security: run as non-root user
RUN groupadd -r authuser && useradd -r -g authuser authuser
USER authuser

EXPOSE 8084

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/auth-service.jar"]
```

### 3. **Docker Compose Update**
```yaml
# docker-compose.yml
version: '3.8'

services:
  auth-service:
    build: ./auth-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=auth_postgres
      - DB_PORT=5432
      - DB_NAME=auth_service
      - DB_USERNAME=auth_user
      - DB_PASSWORD=auth_password
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JWT_SECRET=myVerySecretKeyForJWTTokenGeneration
    depends_on:
      - auth_postgres
      - redis
    networks:
      - microservices-network

  auth_postgres:
    image: postgres:15
    environment:
      - POSTGRES_DB=auth_service
      - POSTGRES_USER=auth_user
      - POSTGRES_PASSWORD=auth_password
    volumes:
      - auth_postgres_data:/var/lib/postgresql/data
    networks:
      - microservices-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - microservices-network

volumes:
  auth_postgres_data:
  redis_data:

networks:
  microservices-network:
    driver: bridge
```

---

## 📋 Checklist для впровадження

### ✅ **Pre-implementation**
- [ ] Backup існуючих даних користувачів
- [ ] Підготувати Redis infrastructure
- [ ] Налаштувати environment variables
- [ ] Підготувати migration scripts

### ✅ **Auth Service Development**
- [ ] Створити Spring Boot проект
- [ ] Налаштувати PostgreSQL + Redis
- [ ] Імплементувати JWT utilities
- [ ] Створити auth endpoints
- [ ] Додати security configuration
- [ ] Написати unit tests
- [ ] Додати integration tests
- [ ] Налаштувати health checks

### ✅ **User Service Migration**
- [ ] Backup auth-related endpoints
- [ ] Видалити login/logout logic
- [ ] Додати JWT validation filter
- [ ] Оновити security configuration
- [ ] Створити auth-service client
- [ ] Тестувати integration
- [ ] Оновити API documentation

### ✅ **Other Services Update**
- [ ] Додати Spring Security dependencies
- [ ] Створити JWT validation filters
- [ ] Налаштувати security configurations
- [ ] Додати auth-service clients
- [ ] Оновити method-level security
- [ ] Тестувати end-to-end flow

### ✅ **Production Deployment**
- [ ] Налаштувати production environment
- [ ] Deployment scripts готові
- [ ] Load balancer configuration
- [ ] Monitoring та alerting
- [ ] Backup strategies
- [ ] Rollback procedures

---

## 🎯 Очікувані результати

### Після повного впровадження:

1. **🔐 Security**
   - JWT-based stateless authentication
   - Centralized authorization logic
   - Rate limiting та account lockout
   - Production-ready security headers

2. **🚀 Performance**
   - Stateless authentication (horizontal scaling)
   - Redis caching для token validation
   - Reduced database queries
   - Better resource utilization

3. **🛠️ Maintainability**
   - Single point of auth logic
   - Consistent security across services
   - Easier testing та debugging
   - Clear separation of concerns

4. **📈 Scalability**
   - Horizontal scaling ready
   - Service-to-service auth
   - Load balancer friendly
   - Cloud deployment ready

---

**📝 Версія плану**: 1.0  
**📅 Дата створення**: 2025-01-01  
**⏱️ Очікуваний час впровадження**: 4-5 тижнів  
**👥 Команда**: Microservices Architecture Team

