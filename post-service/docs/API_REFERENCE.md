# API Reference

## Базовий URL
```
http://localhost:8081/api/v1
```

## Загальні принципи

### Формат відповіді
Всі API endpoints повертають уніфікований формат відповіді:

```json
{
  "success": true|false,
  "payload": { ... },        // При успіху
  "error": { ... }           // При помилці
}
```

### Коди статусів
- `200` - Успішна операція
- `201` - Ресурс створено
- `204` - Успішна операція без контенту
- `400` - Помилка валідації
- `404` - Ресурс не знайдено
- `500` - Внутрішня помилка сервера

### Пагінація
Для endpoints з пагінацією використовується Spring Data Pageable:

```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "direction": "ASC"
    }
  },
  "totalElements": 100,
  "totalPages": 10
}
```

## Posts API

### Отримати всі пости
```http
GET /posts
```

**Параметри:**
- `page` (optional) - номер сторінки (0-based, default: 0)
- `size` (optional) - розмір сторінки (default: 10)
- `sort` (optional) - сортування у форматі `field,direction` (default: `id,asc`)

**Приклад:**
```bash
curl -X GET "http://localhost:8081/api/v1/posts?page=0&size=5&sort=title,asc"
```

### Отримати пост по ID
```http
GET /posts/{id}
```

**Параметри:**
- `id` (path) - ID поста

**Приклад:**
```bash
curl -X GET "http://localhost:8081/api/v1/posts/1"
```

### Створити новий пост
```http
POST /posts
```

**Body:**
```json
{
  "title": "New Post Title",
  "content": "Post content here",
  "authorId": 1,
  "description": {
    "summary": "Post summary"
  }
}
```

**Приклад:**
```bash
curl -X POST "http://localhost:8081/api/v1/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "New Post",
    "content": "Content here",
    "authorId": 1
  }'
```

### Оновити пост
```http
PUT /posts/{id}
```

**Параметри:**
- `id` (path) - ID поста для оновлення

**Body:**
```json
{
  "title": "Updated Title",
  "content": "Updated content",
  "likes": 10
}
```

**Приклад:**
```bash
curl -X PUT "http://localhost:8081/api/v1/posts/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Title",
    "likes": 15
  }'
```

### Видалити пост (hard delete)
```http
DELETE /posts/{id}
```

**Параметри:**
- `id` (path) - ID поста для видалення

**Приклад:**
```bash
curl -X DELETE "http://localhost:8081/api/v1/posts/1"
```

### Soft delete поста
```http
DELETE /posts/{id}/soft
```

**Параметри:**
- `id` (path) - ID поста для soft delete

**Приклад:**
```bash
curl -X DELETE "http://localhost:8081/api/v1/posts/1/soft"
```

### Отримати пости по автору
```http
GET /posts/author/{authorId}
```

**Параметри:**
- `authorId` (path) - ID автора
- `page` (optional) - номер сторінки
- `size` (optional) - розмір сторінки
- `sort` (optional) - сортування

**Приклад:**
```bash
curl -X GET "http://localhost:8081/api/v1/posts/author/1?page=0&size=10"
```

### Отримати пости по ID списку
```http
GET /posts/ids
```

**Параметри:**
- `ids` (query) - список ID через кому
- `page` (optional) - номер сторінки
- `size` (optional) - розмір сторінки
- `sort` (optional) - сортування

**Приклад:**
```bash
curl -X GET "http://localhost:8081/api/v1/posts/ids?ids=1,2,3&page=0&size=10"
```

### Отримати пости по статусу
```http
GET /posts/status/{status}
```

**Параметри:**
- `status` (path) - статус поста (ACTIVE, INACTIVE, DELETED)
- `page` (optional) - номер сторінки
- `size` (optional) - розмір сторінки
- `sort` (optional) - сортування

**Приклад:**
```bash
curl -X GET "http://localhost:8081/api/v1/posts/status/ACTIVE?page=0&size=10"
```

### Bulk оновлення статусу
```http
PUT /posts/bulk/status
```

**Body:**
```json
{
  "ids": [1, 2, 3],
  "newStatus": "INACTIVE"
}
```

**Приклад:**
```bash
curl -X PUT "http://localhost:8081/api/v1/posts/bulk/status" \
  -H "Content-Type: application/json" \
  -d '{
    "ids": [1, 2, 3],
    "newStatus": "INACTIVE"
  }'
```

### RSQL пошук
```http
GET /posts/search
```

**Параметри:**
- `query` (required) - RSQL запит
- `page` (optional) - номер сторінки
- `size` (optional) - розмір сторінки
- `sort` (optional) - сортування

**Приклади:**
```bash
# Пошук по заголовку
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*test*"

# Пошук по JSON полю
curl -X GET "http://localhost:8081/api/v1/posts/search?query=description==*first*"

# Комбінований пошук
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post;status==ACTIVE"

# З пагінацією та сортуванням
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post&page=0&size=5&sort=title,asc"
```

## Health Check API

### Health check
```http
GET /actuator/health
```

**Приклад:**
```bash
curl -X GET "http://localhost:8081/actuator/health"
```

**Відповідь:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    }
  }
}
```

## Моделі даних

### Post Entity
```json
{
  "id": 1,
  "title": "Post Title",
  "content": "Post content",
  "authorId": 1,
  "description": {
    "summary": "Post summary"
  },
  "status": "ACTIVE",
  "likes": 5,
  "commentsIds": [101, 102],
  "created": "2025-08-06T10:00:00",
  "updated": "2025-08-06T10:00:00",
  "createdBy": "system",
  "modifiedBy": "system",
  "userDto": {
    "userId": 1,
    "username": "john_doe"
  }
}
```

### PostRequestDto
```json
{
  "title": "Post Title",
  "content": "Post content",
  "authorId": 1,
  "description": {
    "summary": "Post summary"
  }
}
```

### PostUpdateRequestDto
```json
{
  "title": "Updated Title",
  "content": "Updated content",
  "likes": 10,
  "status": "ACTIVE"
}
```

### BulkStatusUpdateRequestDto
```json
{
  "ids": [1, 2, 3],
  "newStatus": "INACTIVE"
}
```

## Статуси постів

| Статус | Опис |
|--------|------|
| `ACTIVE` | Активний пост |
| `INACTIVE` | Неактивний пост |
| `DELETED` | Видалений пост (soft delete) |

## Помилки

### Загальний формат помилки
```json
{
  "success": false,
  "error": {
    "timestamp": "2025-08-06T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Error description"
  }
}
```

### Типи помилок

#### Validation Error (400)
```json
{
  "success": false,
  "error": {
    "timestamp": "2025-08-06T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Title cannot be empty"
  }
}
```

#### Not Found Error (404)
```json
{
  "success": false,
  "error": {
    "timestamp": "2025-08-06T10:30:00",
    "status": 404,
    "error": "Not Found",
    "message": "Post with id 999 not found"
  }
}
```

#### RSQL Parse Error (400)
```json
{
  "success": false,
  "error": {
    "timestamp": "2025-08-06T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid RSQL query: Field not found: invalidField"
  }
}
```

## Аутентифікація та авторизація

Наразі API не потребує аутентифікації. Всі запити виконуються від імені системного користувача.

## Rate Limiting

API не має обмежень на кількість запитів, але рекомендується:
- Не перевищувати 1000 запитів на хвилину
- Використовувати пагінацію для великих наборів даних
- Кешувати результати на клієнтській стороні

## Версіонування

API використовує версіонування через URL path (`/api/v1/`). При зміні API буде створена нова версія зі збереженням зворотної сумісності.

## Логування

Всі API запити логуються з наступною інформацією:
- HTTP метод та URL
- Параметри запиту
- Час виконання
- Статус відповіді
- Помилки (якщо є)

## Моніторинг

API підтримує Spring Boot Actuator для моніторингу:
- Health checks
- Metrics
- Environment info
- Log levels

Доступ через `/actuator/*` endpoints. 