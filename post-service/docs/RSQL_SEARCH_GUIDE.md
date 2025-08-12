# RSQL Search Guide

## Огляд

RSQL (RESTful Service Query Language) - це мова запитів для REST API, яка дозволяє виконувати складні пошукові запити через URL параметри. Наш сервіс підтримує повний набір RSQL операторів з автоматичним визначенням JSON полів.

## Базові принципи

### URL Endpoint
```
GET /api/v1/posts/search?query=<RSQL_QUERY>&page=<PAGE>&size=<SIZE>&sort=<SORT>
```

### Параметри
- `query` - RSQL запит (обов'язковий)
- `page` - номер сторінки (0-based, за замовчуванням 0)
- `size` - розмір сторінки (за замовчуванням 10)
- `sort` - сортування у форматі `field,direction` (за замовчуванням `id,asc`)

## Підтримувані оператори

| Оператор | Опис | Приклад |
|----------|------|---------|
| `==` | Рівність (з підтримкою wildcards) | `title==*test*` |
| `!=` | Не рівність | `status!=DELETED` |
| `=in=` | В списку значень | `status=in=(ACTIVE,INACTIVE)` |
| `=out=` | Не в списку значень | `status=out=(DELETED)` |
| `>` | Більше ніж | `likes>10` |
| `>=` | Більше або дорівнює | `likes>=5` |
| `<` | Менше ніж | `likes<100` |
| `<=` | Менше або дорівнює | `likes<=50` |

## Логічні оператори

| Оператор | Опис | Приклад |
|----------|------|---------|
| `;` | AND (логічне І) | `title==*test*;authorId==1` |
| `,` | OR (логічне АБО) | `status==ACTIVE,status==INACTIVE` |

## Пошук по звичайних полях

### Текстові поля з wildcards
```bash
# Містить слово "test"
GET /api/v1/posts/search?query=title==*test*

# Починається з "First"
GET /api/v1/posts/search?query=title==First*

# Закінчується на "Post"
GET /api/v1/posts/search?query=title==*Post

# Точний збіг
GET /api/v1/posts/search?query=title==First Post
```

### Числові поля
```bash
# Порівняння
GET /api/v1/posts/search?query=likes>10
GET /api/v1/posts/search?query=authorId==1
GET /api/v1/posts/search?query=likes>=5;likes<=50
```

### Enum поля
```bash
# Статус поста
GET /api/v1/posts/search?query=status==ACTIVE
GET /api/v1/posts/search?query=status=in=(ACTIVE,INACTIVE)
GET /api/v1/posts/search?query=status!=DELETED
```

## Пошук по JSON полях

### Автоматичний JSON пошук
Наш сервіс автоматично визначає JSON поля та шукає в них без необхідності вказувати шлях:

```bash
# Автоматично шукає в description.summary
GET /api/v1/posts/search?query=description==*first*

# Автоматично шукає в description.summary
GET /api/v1/posts/search?query=description==*summary*

# Точний збіг в JSON
GET /api/v1/posts/search?query=description==Brief
```

### Явний JSON шлях
Можна також вказувати точний шлях до JSON поля:

```bash
# Явний шлях до JSON поля
GET /api/v1/posts/search?query=description.summary==*first*
GET /api/v1/posts/search?query=description.summary==*summary*
```

### JSON структура
```json
{
  "description": {
    "summary": "This is a summary of the first post."
  }
}
```

## Комбіновані запити

### AND операції (;)
```bash
# Пошук по заголовку І автору
GET /api/v1/posts/search?query=title==*test*;authorId==1

# Пошук по JSON І статусу
GET /api/v1/posts/search?query=description==*first*;status==ACTIVE

# Множинні умови
GET /api/v1/posts/search?query=title==*Post;authorId==1;likes>5
```

### OR операції (,)
```bash
# Пошук по статусу АБО заголовку
GET /api/v1/posts/search?query=status==ACTIVE,title==*Important*

# Множинні OR умови
GET /api/v1/posts/search?query=status==ACTIVE,status==INACTIVE,likes>10
```

### Комбінація AND та OR
```bash
# (status==ACTIVE OR status==INACTIVE) AND title==*Post
GET /api/v1/posts/search?query=(status==ACTIVE,status==INACTIVE);title==*Post
```

## Сортування

### Базове сортування
```bash
# Сортування по заголовку (за зростанням)
GET /api/v1/posts/search?query=title==*Post&sort=title,asc

# Сортування по заголовку (за спаданням)
GET /api/v1/posts/search?query=title==*Post&sort=title,desc

# Сортування по автору
GET /api/v1/posts/search?query=title==*Post&sort=authorId,desc
```

### Динамічний мапінг полів
Сервіс автоматично мапить camelCase поля в snake_case колонки:
- `authorId` → `author_id`
- `postStatus` → `post_status`
- `createdAt` → `created_at`

## Пагінація

### Базова пагінація
```bash
# Перша сторінка, 10 елементів
GET /api/v1/posts/search?query=title==*Post&page=0&size=10

# Друга сторінка, 20 елементів
GET /api/v1/posts/search?query=title==*Post&page=1&size=20
```

### Повний приклад
```bash
GET /api/v1/posts/search?query=description==*first*;status==ACTIVE&page=0&size=5&sort=title,asc
```

## Приклади реальних запитів

### Пошук активних постів з "test" в заголовку
```bash
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*test*;status==ACTIVE"
```

### Пошук постів з високим рейтингом
```bash
curl -X GET "http://localhost:8081/api/v1/posts/search?query=likes>10&sort=likes,desc"
```

### Пошук по JSON опису
```bash
curl -X GET "http://localhost:8081/api/v1/posts/search?query=description==*first*"
```

### Комплексний пошук
```bash
curl -X GET "http://localhost:8081/api/v1/posts/search?query=(status==ACTIVE,status==INACTIVE);(title==*Post,description==*summary*)&page=0&size=10&sort=created,desc"
```

## Відповідь API

### Успішна відповідь
```json
{
  "success": true,
  "payload": {
    "content": [
      {
        "id": 1,
        "title": "First Post",
        "content": "This is the content of the first post.",
        "description": {
          "summary": "This is a summary of the first post."
        },
        "status": "ACTIVE",
        "likes": 5,
        "authorId": 1
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "direction": "ASC"
      }
    },
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### Помилка
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

## Технічна реалізація

### Автоматичне визначення JSON полів
Сервіс використовує рефлексію для автоматичного визначення JSON полів:
- Перевіряє тип `JsonNode`
- Перевіряє анотації `@JdbcTypeCode(SqlTypes.JSON)`
- Перевіряє `columnDefinition` на "jsonb"

### SQL запити
```sql
-- Для description==*first*
SELECT * FROM posts 
WHERE jsonb_extract_path_text(description, 'summary') LIKE '%first%'

-- Для title==*test*
SELECT * FROM posts 
WHERE LOWER(title) LIKE '%test%'

-- Для комбінованого пошуку
SELECT * FROM posts 
WHERE jsonb_extract_path_text(description, 'summary') LIKE '%first%' 
  AND author_id = 1
```

### Кешування
- Результати визначення JSON полів кешуються
- Мапінг полів для сортування кешується
- Покращує продуктивність при повторних запитах

## Обмеження

1. **JSON шляхи**: Підтримуються тільки 1-рівневі шляхи (наприклад, `description.summary`)
2. **Wildcards**: Підтримуються тільки в текстових полях
3. **Порівняння**: JSON поля підтримують тільки текстові операції
4. **Сортування**: JSON поля не можна сортувати

## Найкращі практики

1. **Використовуйте індекси** для полів, по яких часто шукають
2. **Обмежуйте розмір сторінки** для великих наборів даних
3. **Використовуйте точні збіги** замість wildcards коли можливо
4. **Кешуйте результати** на клієнтській стороні
5. **Валідуйте запити** перед відправкою

## Діагностика

### Логування
Сервіс логує всі RSQL запити:
```
INFO - Searching posts with RSQL query: description==*first*
INFO - Found 1 posts matching RSQL query: description==*first*
```

### Помилки
Найпоширеніші помилки:
- `Field not found` - поле не існує в сутності
- `Invalid operator` - непідтримуваний оператор
- `Parse error` - неправильний синтаксис RSQL
- `Type mismatch` - невідповідність типів даних 