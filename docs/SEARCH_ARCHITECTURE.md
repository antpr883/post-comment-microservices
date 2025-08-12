# 🔍 Архітектура пошуку в мікросервісах

## 📋 Огляд

Цей документ описує принципи та підходи до реалізації пошукових можливостей в нашій мікросервісній архітектурі. Кожен сервіс має різні потреби в пошуку, тому використовуються відповідні технології та підходи.

## 🎯 Загальні принципи пошуку

### 1. **KISS (Keep It Simple, Stupid)**
- Не додавати складність без реальної потреби
- Починати з простих методів, ускладнювати тільки при необхідності
- Оптимізувати для найчастіших use cases

### 2. **Безпека та приватність**
- Обмежувати пошук чутливих даних (email, особиста інформація)
- Використовувати role-based access для складних пошуків
- GDPR compliance для пошуку користувачів

### 3. **Performance First**
- Прості запити швидші за складні
- Індекси налаштовані під конкретні потреби
- Кеш не повинен мати складних пошуків

### 4. **Архітектурна відповідність**
- Кожен сервіс має свою специфіку пошуку
- Технологія пошуку відповідає типу даних
- Уникати over-engineering

## 🏗️ Архітектура по сервісах

### 📊 Порівняльна таблиця

| Сервіс | Технологія БД | Підхід до пошуку | RSQL | Складність | Обґрунтування |
|--------|---------------|------------------|------|------------|---------------|
| **Post Service** | PostgreSQL + JSONB | JpaSpecificationExecutor + RSQL | ✅ | Висока | Контент платформа, JSON поля, складні фільтри |
| **Comment Service** | MongoDB | MongoDB Query + RSQL | ✅ | Висока | Hierarchical структура, модерація, NoSQL |
| **User Service** | PostgreSQL | Прості Repository методи | ❌ | Низька | Безпека, прості потреби, GDPR |
| **Cache Service** | Redis + PostgreSQL | Key-Value пошук | ❌ | Мінімальна | Швидкість, простота, кеш призначення |

---

## 🎯 Post Service - Складний пошук

### Технологічний стек
```java
// JPA Specification + RSQL
public interface PostRepository extends 
    JpaRepository<Post, Long>, 
    JpaSpecificationExecutor<Post> {
}

// RSQL парсер
Specification<Post> spec = rsqlParserService.parse(rsqlQuery, Post.class);
Page<Post> posts = postRepository.findAll(spec, pageable);
```

### Можливості пошуку
```bash
# Простий пошук
GET /api/posts/search?query=title==*spring*

# Складні фільтри з AND
GET /api/posts/search?query=title==*spring*;authorId==123;status==ACTIVE

# Пошук з OR
GET /api/posts/search?query=title==*spring*,content==*java*

# JSON поля (PostgreSQL JSONB)
GET /api/posts/search?query=description==*tutorial*
GET /api/posts/search?query=description.category==programming

# Числові порівняння
GET /api/posts/search?query=likes>50;createdAt>=2024-01-01

# Діапазони
GET /api/posts/search?query=likes>=10;likes<=100

# Складні комбінації
GET /api/posts/search?query=(title==*spring*;authorId==123),(status==ACTIVE;likes>50)
```

### Чому саме такий підхід?

#### ✅ Переваги:
- **Контент платформа**: блог/форум потребує гнучкого пошуку
- **JSON поля**: PostgreSQL JSONB з складними запитами
- **SEO потреби**: пошук по заголовках, контенту, тегах
- **Analytics**: фільтрація по різних метриках
- **User experience**: гнучкі фільтри для користувачів

#### 🎯 Use Cases:
- Пошук статей по темах
- Фільтрація по популярності (лайки, коментарі)
- Пошук по авторах та датах
- Модерація контенту
- Рекомендаційна система

---

## 💬 Comment Service - MongoDB RSQL

### Технологічний стек
```java
// MongoDB Query + RSQL
@Component
public class RsqlParserService<T> {
    public Query parse(String query, Class<T> entityClass) {
        Node rootNode = new RSQLParser().parse(query);
        return rootNode.accept(new RsqlVisitorImpl<>(entityClass));
    }
}

// Використання
Query mongoQuery = rsqlParserService.parse(rsqlQuery, Comments.class);
List<Comments> comments = mongoTemplate.find(mongoQuery, Comments.class);
```

### Можливості пошуку
```bash
# Пошук по контенту
GET /api/comments/search?query=content==*відповідь*

# Пошук по автору
GET /api/comments/search?query=authorId==user123

# Пошук по кількості лайків
GET /api/comments/search?query=likesCount>5

# Пошук відповідей на коментар
GET /api/comments/search?query=parentCommentId==comment-001

# Пошук по посту з фільтрами
GET /api/comments/search?query=postId==post-001;likesCount>=2;likesCount<=10

# Складні запити
GET /api/comments/search?query=postId==post-001;(authorId==user1,likesCount>10)
```

### Чому MongoDB + RSQL?

#### ✅ Переваги:
- **NoSQL гнучкість**: MongoDB для hierarchical структур
- **Nested comments**: дерево коментарів різної глибини
- **Модерація**: складні фільтри для модерації контенту
- **Швидкість**: MongoDB оптимізований для read-heavy навантаження
- **Масштабування**: горизонтальне масштабування коментарів

#### 🎯 Use Cases:
- Hierarchical коментарі (відповіді на відповіді)
- Модерація коментарів по різних критеріях
- Пошук токсичних коментарів
- Analytics по активності користувачів
- Тредінг та популярні обговорення

---

## 👤 User Service - Прості методи

### Технологічний стек
```java
// Прості Spring Data JPA методи
public interface UserRepository extends CustomJpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByNickname(String nickname);
    List<User> findByStatus(UserStatus status);
    Page<User> findByStatus(UserStatus status, Pageable pageable);
    
    // Pattern пошук (LIKE)
    @Query("SELECT u FROM User u WHERE u.email LIKE %:pattern%")
    List<User> findByEmailPattern(@Param("pattern") String pattern);
}
```

### Поточні можливості
```bash
# Точний пошук
GET /api/users/email?email=john@example.com
GET /api/users/nickname?nickname=john123

# Пошук по статусу
GET /api/users/status/ACTIVE?page=0&size=10

# Pattern пошук
GET /api/users/search/email?pattern=john
GET /api/users/search/nickname?pattern=dev
```

### Чому НЕ RSQL?

#### 🚫 Причини:
- **Безпека**: email та особиста інформація не повинні бути легко доступні
- **GDPR compliance**: обмежений доступ до персональних даних
- **Прості потреби**: 90% запитів - це пошук по email/nickname
- **Performance**: індекси оптимізовані під конкретні поля
- **Privacy by design**: мінімальна exposure користувацьких даних

#### 🎯 Use Cases:
- Автентифікація (findByEmail)
- Пошук друзів (findByNickname pattern)
- Адмін панель (findByStatus)
- Простий CRUD для користувачів

#### 🔒 Можлива майбутня розширення (тільки для адмінів):
```java
// Обмежений RSQL для адмін панелі
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/search")
public ResponseEntity<Page<UserDto>> adminSearch(@RequestParam String query) {
    // Дозволити тільки: status, role.name, createdAt
    // Заборонити: email, nickname, password
}
```

---

## 🔄 Cache Service - Key-Value пошук

### Технологічний стек
```java
// Redis + простий DAO
public interface UserCacheService {
    Optional<Map<String, Object>> getUserById(Long userId);
    Optional<Map<String, Object>> getUserByUsername(String username);
    void cacheUser(Map<String, Object> userData);
    void evictUser(Long userId);
}
```

### Можливості пошуку
```java
// O(1) операції
cacheService.getUserById(123L);           // Redis GET user:123
cacheService.getUserByUsername("john");   // Redis GET username:john
cacheService.evictUser(123L);             // Redis DEL user:123
```

### Чому НЕ складний пошук?

#### 🚫 Причини:
- **Призначення кешу**: швидкий доступ, не пошук
- **Performance**: складні запити повільні для кешу
- **Архітектура**: пошуки повинні йти через основні сервіси
- **TTL**: дані в кеші тимчасові, не для аналітики
- **Memory usage**: зберігати тільки необхідне

#### 🎯 Use Cases:
- Кешування користувацьких сесій
- Швидкий доступ до профілів
- Зменшення навантаження на User Service
- Temporary data storage

---

## 🛠️ Технічна реалізація RSQL

### Компоненти RSQL системи

#### 1. **RsqlParserService**
```java
@Component
public class RsqlParserService<T> {
    /**
     * Конвертує RSQL рядок в JPA Specification або MongoDB Query
     */
    public Specification<T> parse(String query, Class<T> entityClass);
}
```

#### 2. **RsqlVisitorImpl**
```java
public class RsqlVisitorImpl<T> implements RSQLVisitor<Specification<T>, Void> {
    /**
     * Відвідувач для traverse RSQL parse tree
     */
    @Override
    public Specification<T> visit(AndNode node, Void param);
    
    @Override  
    public Specification<T> visit(OrNode node, Void param);
    
    @Override
    public Specification<T> visit(ComparisonNode node, Void param);
}
```

#### 3. **GenericRsqlSpecification**
```java
public class GenericRsqlSpecification<T> implements Specification<T> {
    /**
     * Створює JPA Predicate для конкретного поля та умови
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
```

### Підтримувані оператори

| Оператор | Опис | Приклад |
|----------|------|---------|
| `==` | Дорівнює | `title==spring` |
| `!=` | Не дорівнює | `status!=DELETED` |
| `>` | Більше | `likes>10` |
| `>=` | Більше або дорівнює | `likes>=5` |
| `<` | Менше | `likes<100` |
| `<=` | Менше або дорівнює | `likes<=50` |
| `=in=` | В списку | `status=in=(ACTIVE,PENDING)` |
| `=out=` | Не в списку | `status=out=(DELETED,SUSPENDED)` |
| `*` | Wildcard для строк | `title==*spring*` |
| `;` | AND логіка | `title==spring;likes>10` |
| `,` | OR логіка | `title==spring,content==java` |

### JSON поля підтримка (Post Service)

```java
// PostgreSQL JSONB запити
public class JsonSupport {
    /**
     * Створює запити для JSON полів
     */
    public static Predicate buildJsonPredicate(
        Root<?> root, 
        CriteriaBuilder cb, 
        String jsonPath, 
        ComparisonOperator operator, 
        List<String> arguments
    );
}

// Приклади JSON запитів:
// description==*tutorial*           -> jsonb_extract_path_text(description, 'summary') LIKE '%tutorial%'
// description.category==programming -> json_extract_path_text(description, 'category') = 'programming'
// description.tags=in=(java,spring) -> description->'tags' ?| array['java','spring']
```

---

## 📈 Метрики та моніторинг

### KPI для оцінки потреби в складному пошуку

1. **Частота використання**
   - Якщо >80% запитів прості → залишити прості методи
   - Якщо >20% запитів складні → розглянути RSQL

2. **Кількість різних комбінацій**
   - <5 різних запитів → додати окремі методи
   - >10 різних комбінацій → RSQL виправданий

3. **Performance impact**
   - Прості запити: <10ms
   - RSQL запити: <100ms
   - Складні JSON запити: <500ms

4. **Безпека та compliance**
   - Персональні дані → обмежити пошук
   - Публічний контент → дозволити гнучкий пошук

### Логування та дебагінг

```java
// Логування RSQL запитів
@Slf4j
public class RsqlParserService<T> {
    public Specification<T> parse(String query, Class<T> entityClass) {
        log.debug("Parsing RSQL query: {} for entity: {}", query, entityClass.getSimpleName());
        
        try {
            // Parse and convert
            Specification<T> spec = parseInternal(query, entityClass);
            log.debug("Successfully parsed RSQL query: {}", query);
            return spec;
        } catch (Exception e) {
            log.error("Failed to parse RSQL query: {} - Error: {}", query, e.getMessage());
            throw new IllegalArgumentException("Invalid RSQL query: " + query, e);
        }
    }
}
```

---

## 🚀 Рекомендації по використанню

### ✅ Коли використовувати RSQL:
- **Контент платформи** (блоги, форуми, новини)
- **E-commerce** каталоги з фільтрами
- **Analytics** дашборди з багатьма фільтрами
- **CMS** системи для контент менеджерів
- **Data exploration** інструменти

### ❌ Коли НЕ використовувати RSQL:
- **Персональні дані** користувачів
- **High-performance** real-time API
- **Простий CRUD** без складних фільтрів
- **Security-sensitive** операції
- **Cache layers** та швидкі lookup

### 🔄 Міграційна стратегія:
1. **Початок**: прості repository методи
2. **Зростання**: додавання окремих складних методів
3. **Масштабування**: впровадження RSQL при потребі
4. **Оптимізація**: моніторинг та тюнінг запитів

---

## 📚 Додаткові ресурси

- [RSQL Parser документація](https://github.com/jirutka/rsql-parser)
- [Spring Data JPA Specifications](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#specifications)
- [MongoDB Query Language](https://docs.mongodb.com/manual/tutorial/query-documents/)
- [PostgreSQL JSONB операції](https://www.postgresql.org/docs/current/functions-json.html)

---

**📝 Версія документу**: 1.0  
**📅 Останнє оновлення**: 2025-01-01  
**👥 Автори**: Microservices Architecture Team

