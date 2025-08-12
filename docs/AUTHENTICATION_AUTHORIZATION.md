# Authentication & Authorization Guide

## 🔐 Overview

This document outlines the authentication and authorization mechanisms implemented across the microservices architecture, focusing on internal service communication and future external authentication integration.

## 🏗️ Current Authentication Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        WEB[Web Application]
        MOB[Mobile App]
        API[API Clients]
    end
    
    subgraph "Authentication Layer"
        INT[Internal Service Auth]
        EXT[External User Auth<br/>(Future)]
    end
    
    subgraph "Services"
        US[User Service<br/>Authentication Provider]
        PS[Post Service]
        CS[Comment Service]
        UCS[User Cache Service]
    end
    
    WEB --> EXT
    MOB --> EXT
    API --> EXT
    
    EXT -.-> US
    INT --> US
    INT --> PS
    INT --> CS
    INT --> UCS
    
    PS --> INT
    CS --> INT
    UCS --> INT
```

## 🔑 Internal Service Authentication

### Authentication Mechanism
Services authenticate with each other using a **shared secret** approach:

```http
X-Service-Name: {calling-service-name}
X-Service-Secret: {shared-secret-key}
```

### Configuration
```yaml
app:
  security:
    internal-secret: internal-service-secret-key-2024
```

### Implementation
```java
@RestController
@RequestMapping("/internal")
public class InternalController {
    
    @Value("${app.security.internal-secret}")
    private String internalSecret;
    
    private boolean isValidInternalCall(HttpServletRequest request) {
        String serviceName = request.getHeader("X-Service-Name");
        String serviceSecret = request.getHeader("X-Service-Secret");
        
        return serviceName != null && 
               serviceSecret != null && 
               internalSecret.equals(serviceSecret);
    }
    
    @PostMapping("/auth/validate-credentials")
    public ResponseEntity<UserDto> validateCredentials(
            @Valid @RequestBody UserCredentialsDto credentials, 
            HttpServletRequest request) {
        
        if (!isValidInternalCall(request)) {
            log.warn("Unauthorized internal API call from: {}", 
                    request.getRemoteAddr());
            return ResponseEntity.status(403).build();
        }
        
        // Validate credentials logic
        // ...
    }
}
```

## 👤 User Management & Authentication

### User Entity Structure
```java
@Entity
@Table(name = "users", schema = "v1_user")
public class User extends PersistenceModel {
    
    private String email;
    private String nickname;
    private String password;  // BCrypt hashed
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;  // ACTIVE, INACTIVE, SUSPENDED, DELETED
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;
    
    private LocalDateTime lastLoginAt;
    private Boolean enabled = true;
    private Boolean locked = false;
}
```

### Password Security
```java
@Service
public class UserServiceImpl {
    
    private final PasswordEncoder passwordEncoder;
    
    public UserDto createUser(UserRequestDto requestDto) {
        User user = userMapper.toEntity(requestDto);
        
        // Hash password using BCrypt
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }
}
```

### Credential Validation
```java
@PostMapping("/internal/auth/validate-credentials")
public ResponseEntity<UserDto> validateCredentials(
        @Valid @RequestBody UserCredentialsDto credentials) {
    
    // Find user by email
    Optional<UserDto> userOpt = userService.findByEmailForAuth(credentials.getEmail());
    
    if (userOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    
    UserDto user = userOpt.get();
    
    // Check if user is active
    if (!user.getEnabled() || user.getLocked()) {
        return ResponseEntity.status(403).build();
    }
    
    // Validate password
    if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
        return ResponseEntity.status(401).build();
    }
    
    // Remove password from response
    user.setPassword(null);
    
    return ResponseEntity.ok(user);
}
```

## 🎭 Role-Based Access Control (RBAC)

### Role Entity Structure
```java
@Entity
@Table(name = "roles", schema = "v1_user")
public class Role extends PersistenceModel {
    
    @Column(unique = true)
    private String name;
    
    private String description;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "role_authorities")
    private Set<Authority> authorities = new HashSet<>();
}
```

### Authority Enumeration
```java
public enum Authority {
    // User Management
    USER_READ,
    USER_WRITE,
    USER_DELETE,
    
    // Post Management
    POST_READ,
    POST_WRITE,
    POST_DELETE,
    POST_MODERATE,
    
    // Comment Management
    COMMENT_READ,
    COMMENT_WRITE,
    COMMENT_DELETE,
    COMMENT_MODERATE,
    
    // Administrative
    ADMIN_USER_MANAGEMENT,
    ADMIN_CONTENT_MANAGEMENT,
    ADMIN_SYSTEM_MANAGEMENT
}
```

### Role Examples
```sql
-- Standard User Role
INSERT INTO roles (name, description) VALUES 
('USER', 'Standard user with basic permissions');

INSERT INTO role_authorities (role_id, authorities) VALUES 
((SELECT id FROM roles WHERE name = 'USER'), 'USER_READ'),
((SELECT id FROM roles WHERE name = 'USER'), 'POST_READ'),
((SELECT id FROM roles WHERE name = 'USER'), 'POST_WRITE'),
((SELECT id FROM roles WHERE name = 'USER'), 'COMMENT_READ'),
((SELECT id FROM roles WHERE name = 'USER'), 'COMMENT_WRITE');

-- Moderator Role
INSERT INTO roles (name, description) VALUES 
('MODERATOR', 'Content moderator with additional permissions');

INSERT INTO role_authorities (role_id, authorities) VALUES 
((SELECT id FROM roles WHERE name = 'MODERATOR'), 'POST_MODERATE'),
((SELECT id FROM roles WHERE name = 'MODERATOR'), 'COMMENT_MODERATE');

-- Administrator Role
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'System administrator with full permissions');

INSERT INTO role_authorities (role_id, authorities) VALUES 
((SELECT id FROM roles WHERE name = 'ADMIN'), 'ADMIN_USER_MANAGEMENT'),
((SELECT id FROM roles WHERE name = 'ADMIN'), 'ADMIN_CONTENT_MANAGEMENT'),
((SELECT id FROM roles WHERE name = 'ADMIN'), 'ADMIN_SYSTEM_MANAGEMENT');
```

## 🔒 Future JWT Implementation Architecture

### JWT Token Structure
```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "user123",
    "email": "user@example.com",
    "fullName": "John Doe",
    "roles": ["USER"],
    "authorities": ["USER_READ", "POST_READ", "POST_WRITE"],
    "tokenType": "ACCESS",
    "jti": "token-id-123",
    "iat": 1643723400,
    "exp": 1643727000
  }
}
```

### JWT Service Implementation (Ready for Integration)
```java
@Service
public class JwtService {
    
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    
    public String generateAccessToken(UserDto user) {
        return generateToken(user, accessTokenExpiration, "ACCESS");
    }
    
    public String generateRefreshToken(UserDto user) {
        return generateToken(user, refreshTokenExpiration, "REFRESH");
    }
    
    private String generateToken(UserDto user, long expiration, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        Set<String> roles = user.getRoles().stream()
                .map(RoleDto::getName)
                .collect(Collectors.toSet());
                
        Set<String> authorities = user.getRoles().stream()
                .flatMap(role -> role.getAuthorities().stream())
                .collect(Collectors.toSet());
        
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("fullName", user.getFullName())
                .claim("roles", roles)
                .claim("authorities", authorities)
                .claim("tokenType", tokenType)
                .claim("jti", UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }
}
```

### JWT Authentication Filter (Future)
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractTokenFromRequest(request);
        
        if (token != null && jwtService.isTokenValid(token)) {
            Long userId = jwtService.extractUserId(token);
            Set<String> authorities = jwtService.extractAuthorities(token);
            
            // Create authentication object
            Authentication authentication = createAuthentication(userId, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
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

## 🛡️ Security Configuration

### Current Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/users/register").permitAll()
                .requestMatchers("/internal/**").permitAll() // Internal endpoints
                .anyRequest().authenticated()
            );
            
        return http.build();
    }
}
```

### Future Enhanced Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
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
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/internal/**").hasRole("INTERNAL_SERVICE")
                .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").hasAuthority("POST_READ")
                .requestMatchers(HttpMethod.POST, "/api/v1/posts").hasAuthority("POST_WRITE")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").hasAuthority("POST_DELETE")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}
```

## 🔍 Authorization Examples

### Method-Level Security
```java
@Service
@PreAuthorize("hasRole('USER')")
public class PostServiceImpl implements PostService {
    
    @PreAuthorize("hasAuthority('POST_WRITE')")
    public AppResponse<PostDto> createPost(PostRequestDto requestDto) {
        // Implementation
    }
    
    @PreAuthorize("hasAuthority('POST_DELETE') or @postService.isOwner(#id, authentication.name)")
    public AppResponse<PostDto> deletePost(Long id) {
        // Implementation
    }
    
    @PreAuthorize("hasAuthority('POST_MODERATE')")
    public void moderatePost(Long id, ModerationAction action) {
        // Implementation
    }
}
```

### Custom Security Expressions
```java
@Component("postService")
public class PostSecurityService {
    
    private final PostRepository postRepository;
    
    public boolean isOwner(Long postId, String username) {
        return postRepository.findById(postId)
                .map(post -> post.getAuthor().getUsername().equals(username))
                .orElse(false);
    }
    
    public boolean canModerate(String username) {
        // Check if user has moderation rights
        return userService.hasAuthority(username, "POST_MODERATE");
    }
}
```

## 📊 Audit & Security Logging

### Security Event Logging
```java
@Component
public class SecurityEventLogger {
    
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("SECURITY");
    
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Map<String, Object> securityData = Map.of(
            "event", "AUTHENTICATION_SUCCESS",
            "username", event.getAuthentication().getName(),
            "timestamp", LocalDateTime.now(),
            "source", "LOGIN"
        );
        SECURITY_LOGGER.info("SECURITY_EVENT: {}", toJson(securityData));
    }
    
    @EventListener
    public void handleAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Map<String, Object> securityData = Map.of(
            "event", "AUTHENTICATION_FAILURE",
            "username", event.getAuthentication().getName(),
            "reason", event.getException().getMessage(),
            "timestamp", LocalDateTime.now(),
            "threatLevel", "HIGH"
        );
        SECURITY_LOGGER.warn("SECURITY_EVENT: {}", toJson(securityData));
    }
}
```

### Authorization Audit
```java
@Aspect
@Component
public class AuthorizationAuditAspect {
    
    @Around("@annotation(preAuthorize)")
    public Object auditAuthorization(ProceedingJoinPoint joinPoint, PreAuthorize preAuthorize) {
        String expression = preAuthorize.value();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> auditData = Map.of(
            "event", "AUTHORIZATION_CHECK",
            "expression", expression,
            "username", auth.getName(),
            "authorities", auth.getAuthorities(),
            "method", joinPoint.getSignature().getName()
        );
        
        try {
            Object result = joinPoint.proceed();
            auditData.put("authorized", true);
            return result;
        } catch (AccessDeniedException e) {
            auditData.put("authorized", false);
            auditData.put("reason", e.getMessage());
            throw e;
        } finally {
            SECURITY_LOGGER.info("AUTHORIZATION_AUDIT: {}", toJson(auditData));
        }
    }
}
```

## 🚀 Migration Path

### Phase 1: Current State ✅
- Internal service authentication
- Password hashing and validation
- Basic role structure
- Audit logging foundation

### Phase 2: JWT Integration (Next)
- JWT token generation and validation
- Authentication endpoints
- Token refresh mechanism
- Security filter integration

### Phase 3: Advanced Authorization
- Method-level security
- Custom security expressions
- Advanced audit logging
- Rate limiting and throttling

### Phase 4: Enterprise Features
- Single Sign-On (SSO)
- Multi-factor authentication (MFA)
- OAuth2/OpenID Connect
- Advanced threat detection

---

This authentication and authorization architecture provides a solid foundation for secure microservices communication while being designed for easy extension to support external user authentication and advanced security features.
