# Quick Start Guide

## Швидкий старт з RSQL пошуком

Цей гід допоможе вам швидко почати використовувати RSQL пошук в нашому API.

## 🚀 Запуск сервісу

### 1. Запуск додатку
```bash
mvn spring-boot:run
```

### 2. Перевірка доступності
```bash
curl -X GET "http://localhost:8081/actuator/health"
```

## 📝 Перші кроки

### 1. Отримати всі пости
```bash
curl -X GET "http://localhost:8081/api/v1/posts"
```

### 2. Отримати пост по ID
```bash
curl -X GET "http://localhost:8081/api/v1/posts/1"
```

### 3. Створити новий пост
```bash
curl -X POST "http://localhost:8081/api/v1/posts" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Post",
    "content": "This is my first post content",
    "authorId": 1,
    "description": {
      "summary": "A brief summary of my first post"
    }
  }'
```

## 🔍 RSQL Пошук - Основні приклади

### Пошук по заголовку
```bash
# Знайти пости з "Post" в заголовку
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*"

# Знайти пости, що починаються з "First"
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==First*"
```

### Пошук по JSON полю (description)
```bash
# Знайти пости з "first" в описі
curl -X GET "http://localhost:8081/api/v1/posts/search?query=description==*first*"

# Знайти пости з "summary" в описі
curl -X GET "http://localhost:8081/api/v1/posts/search?query=description==*summary*"
```

### Пошук по числовим полям
```bash
# Знайти пости з більше ніж 5 лайками
curl -X GET "http://localhost:8081/api/v1/posts/search?query=likes>5"

# Знайти пости автора з ID 1
curl -X GET "http://localhost:8081/api/v1/posts/search?query=authorId==1"
```

### Пошук по статусу
```bash
# Знайти активні пости
curl -X GET "http://localhost:8081/api/v1/posts/search?query=status==ACTIVE"

# Знайти неактивні пости
curl -X GET "http://localhost:8081/api/v1/posts/search?query=status==INACTIVE"
```

## 🔗 Комбіновані запити

### AND операції (;)
```bash
# Пошук активних постів з "Post" в заголовку
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*;status==ACTIVE"

# Пошук постів автора 1 з більше ніж 5 лайками
curl -X GET "http://localhost:8081/api/v1/posts/search?query=authorId==1;likes>5"
```

### OR операції (,)
```bash
# Пошук активних або неактивних постів
curl -X GET "http://localhost:8081/api/v1/posts/search?query=status==ACTIVE,status==INACTIVE"
```

## 📄 Пагінація та сортування

### Базова пагінація
```bash
# Перша сторінка, 5 елементів
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*&page=0&size=5"

# Друга сторінка, 10 елементів
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*&page=1&size=10"
```

### Сортування
```bash
# Сортування по заголовку (за зростанням)
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*&sort=title,asc"

# Сортування по лайках (за спаданням)
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*&sort=likes,desc"
```

### Повний приклад
```bash
# Пошук активних постів з "Post" в заголовку, сортування по лайках, перша сторінка
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*;status==ACTIVE&page=0&size=5&sort=likes,desc"
```

## 🎯 Практичні приклади

### Приклад 1: Пошук популярних постів
```bash
# Знайти активні пости з більше ніж 10 лайками, сортування по лайках
curl -X GET "http://localhost:8081/api/v1/posts/search?query=status==ACTIVE;likes>10&sort=likes,desc"
```

### Приклад 2: Пошук по автору та контенту
```bash
# Знайти пости автора 1 з "content" в тексті
curl -X GET "http://localhost:8081/api/v1/posts/search?query=authorId==1;content==*content*"
```

### Приклад 3: Пошук по JSON опису
```bash
# Знайти пости з "summary" в JSON описі
curl -X GET "http://localhost:8081/api/v1/posts/search?query=description==*summary*"
```

### Приклад 4: Комплексний пошук
```bash
# Знайти активні пости з "Post" в заголовку або "first" в описі, сортування по даті створення
curl -X GET "http://localhost:8081/api/v1/posts/search?query=status==ACTIVE;(title==*Post*,description==*first*)&sort=created,desc"
```

## 📊 Робота з відповідями

### Структура відповіді
```json
{
  "success": true,
  "payload": {
    "content": [
      {
        "id": 1,
        "title": "First Post",
        "content": "Post content",
        "description": {
          "summary": "Post summary"
        },
        "status": "ACTIVE",
        "likes": 5,
        "authorId": 1
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Обробка помилок
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

## 🛠 Корисні команди

### Перевірка доступності сервісу
```bash
curl -X GET "http://localhost:8081/actuator/health"
```

### Отримання всіх постів з пагінацією
```bash
curl -X GET "http://localhost:8081/api/v1/posts?page=0&size=10"
```

### Тестування JSON пошуку
```bash
# Створіть тестовий скрипт
cat > test_search.sh << 'EOF'
#!/bin/bash
echo "🔍 Тестування RSQL пошуку"
echo "=========================="

BASE_URL="http://localhost:8081/api/v1/posts/search"

echo "1. Пошук по заголовку:"
curl -s "$BASE_URL?query=title==*Post*" | jq '.payload.content | length'

echo "2. Пошук по JSON полю:"
curl -s "$BASE_URL?query=description==*first*" | jq '.payload.content[0].title'

echo "3. Комбінований пошук:"
curl -s "$BASE_URL?query=title==*Post*;status==ACTIVE" | jq '.payload.content | length'

echo "✅ Тестування завершено!"
EOF

chmod +x test_search.sh
./test_search.sh
```

## 🚨 Поширені помилки

### 1. Помилка "Field not found"
```bash
# ❌ Неправильно
curl -X GET "http://localhost:8081/api/v1/posts/search?query=invalidField==*test*"

# ✅ Правильно
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*test*"
```

### 2. Помилка "Invalid operator"
```bash
# ❌ Неправильно
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title=*test*"

# ✅ Правильно
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*test*"
```

### 3. Помилка "Parse error"
```bash
# ❌ Неправильно
curl -X GET "http://localhost:8081/api/v1/posts/search?query="

# ✅ Правильно
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*test*"
```

## 📚 Наступні кроки

1. **Вивчіть повну документацію**: `docs/RSQL_SEARCH_GUIDE.md`
2. **Перегляньте API довідник**: `docs/API_REFERENCE.md`
3. **Експериментуйте з різними запитами**
4. **Створіть власні тестові скрипти**

## 🆘 Підтримка

Якщо у вас виникли питання:
1. Перевірте логи додатку
2. Використовуйте health check endpoint
3. Перегляньте повну документацію
4. Перевірте правильність RSQL синтаксису 