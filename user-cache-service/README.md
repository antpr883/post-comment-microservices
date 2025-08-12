# User Cache Service

Spring Boot автоконфігурація для кешування користувачів з підтримкою Redis та PostgreSQL.

## Конфігурація

### Основні файли конфігурації:

- `application.yml` - основна конфігурація з профілями
- `application-dev.yml` - конфігурація для розробки
- `application-test.yml` - конфігурація для тестування  
- `application-prod.yml` - конфігурація для продакшену

### Використання профілів:

```yaml
# application.yml
spring:
  profiles:
    active: dev  # або test, prod

user-cache:
  enabled: true
```

### Запуск з різними профілями:

```bash
# Розробка
java -jar user-cache-service.jar --spring.profiles.active=dev

# Тестування
java -jar user-cache-service.jar --spring.profiles.active=test

# Продакшен
java -jar user-cache-service.jar --spring.profiles.active=prod
```

## Профілі

### Dev Profile
- База даних: `user_cache_dev`
- Redis: database 1
- TTL: 15m (Redis), 2h (DB)
- Логування: DEBUG

### Test Profile  
- База даних: `user_cache_test`
- Redis: database 2
- TTL: 5m (Redis), 1h (DB)
- Логування: INFO

### Prod Profile
- База даних: змінні середовища
- Redis: змінні середовища
- TTL: 60m (Redis), 12h (DB)
- Логування: INFO

## Змінні середовища для Prod

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=user_cache_prod
DB_USERNAME=prod_user
DB_PASSWORD=prod_pass
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
USER_SERVICE_URL=http://user-service:8081
```

## Інтеграція

Додайте залежність до вашого проекту:

```xml
<dependency>
    <groupId>com.andev.cache</groupId>
    <artifactId>user-cache-service</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Сервіс автоматично налаштується при `user-cache.enabled=true`. 