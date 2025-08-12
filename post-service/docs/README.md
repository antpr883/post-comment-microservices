# Post Service Documentation

## 📚 Огляд документації

Ця папка містить повну документацію для Post Service з RSQL пошуком та JSON підтримкою.

## 📖 Структура документації

### 🚀 [Quick Start Guide](QUICK_START.md)
**Для початківців** - швидкий старт з основними прикладами та командами.

**Що містить:**
- Запуск сервісу
- Перші кроки
- Основні приклади RSQL пошуку
- Пагінація та сортування
- Практичні приклади
- Поширені помилки

### 🔍 [RSQL Search Guide](RSQL_SEARCH_GUIDE.md)
**Детальна документація** по RSQL пошуку з усіма можливостями.

**Що містить:**
- Повний опис RSQL операторів
- Пошук по звичайних та JSON полях
- Комбіновані запити (AND/OR)
- Технічна реалізація
- Обмеження та найкращі практики
- Діагностика помилок

### 📋 [API Reference](API_REFERENCE.md)
**Повний довідник API** з усіма endpoints та моделями даних.

**Що містить:**
- Всі доступні endpoints
- Моделі даних (DTO)
- Приклади запитів та відповідей
- Коди помилок
- Аутентифікація та моніторинг

## 🎯 Швидкий старт

### 1. Запуск сервісу
```bash
mvn spring-boot:run
```

### 2. Перевірка доступності
```bash
curl -X GET "http://localhost:8081/actuator/health"
```

### 3. Перший RSQL запит
```bash
# Пошук по заголовку
curl -X GET "http://localhost:8081/api/v1/posts/search?query=title==*Post*"

# Пошук по JSON полю
curl -X GET "http://localhost:8081/api/v1/posts/search?query=description==*first*"
```

## 🔧 Ключові можливості

### ✅ RSQL Пошук
- Повний набір операторів (`==`, `!=`, `>`, `<`, `=in=`, `=out=`)
- Логічні операції (AND/OR)
- Wildcards підтримка (`*test*`, `test*`, `*test`)

### ✅ JSON Підтримка
- Автоматичне визначення JSON полів
- Пошук без явного вказання шляху
- Підтримка `jsonb_extract_path_text`

### ✅ Динамічне сортування
- Автоматичний мапінг camelCase → snake_case
- Підтримка всіх полів сутності
- Кешування результатів

### ✅ Пагінація
- Spring Data Pageable
- Гнучкі параметри сторінкування
- Метадані пагінації

## 📊 Приклади використання

### Базовий пошук
```bash
# Пошук по заголовку
GET /api/v1/posts/search?query=title==*test*

# Пошук по JSON полю
GET /api/v1/posts/search?query=description==*first*

# Комбінований пошук
GET /api/v1/posts/search?query=title==*Post*;status==ACTIVE
```

### Пагінація та сортування
```bash
# З пагінацією
GET /api/v1/posts/search?query=title==*Post*&page=0&size=10

# З сортуванням
GET /api/v1/posts/search?query=title==*Post*&sort=likes,desc

# Повний приклад
GET /api/v1/posts/search?query=status==ACTIVE&page=0&size=5&sort=created,desc
```

## 🏗 Архітектура

### Компоненти RSQL пошуку
```
RsqlParserService
    ↓
RsqlVisitorImpl
    ↓
GenericRsqlSpecBuilder
    ↓
GenericRsqlSpecification
    ↓
JsonSupport / JsonFieldUtils / SortingHelper
```

### Ключові класи
- **`RsqlParserService`** - головний сервіс парсингу
- **`JsonFieldUtils`** - визначення JSON полів через рефлексію
- **`SortingHelper`** - динамічний мапінг полів для сортування
- **`JsonSupport`** - побудова SQL запитів для JSON полів

## 🚨 Обмеження

1. **JSON шляхи**: Тільки 1-рівневі шляхи (`description.summary`)
2. **Wildcards**: Тільки в текстових полях
3. **JSON порівняння**: Тільки текстові операції
4. **JSON сортування**: Не підтримується

## 🔍 Діагностика

### Логування
```bash
# Перегляд логів
tail -f logs.txt

# Фільтрація RSQL запитів
grep "RSQL query" logs.txt
```

### Health Check
```bash
# Перевірка стану сервісу
curl -X GET "http://localhost:8081/actuator/health"
```

### Тестування
```bash
# Запуск тестів
mvn test

# Запуск конкретного тесту
mvn test -Dtest=RsqlSearchTest
```

## 📈 Продуктивність

### Кешування
- Результати визначення JSON полів
- Мапінг полів для сортування
- Покращує продуктивність при повторних запитах

### Рекомендації
1. Використовуйте індекси для полів, по яких часто шукають
2. Обмежуйте розмір сторінки для великих наборів даних
3. Використовуйте точні збіги замість wildcards коли можливо
4. Кешуйте результати на клієнтській стороні

## 🛠 Розробка

### Додавання нових полів
1. Додайте поле в `Post` entity
2. Додайте анотації якщо потрібно
3. Оновіть тести
4. Документація оновиться автоматично

### Розширення RSQL функціональності
1. Модифікуйте `JsonSupport` для нових операторів
2. Оновіть `GenericRsqlSpecification`
3. Додайте тести
4. Оновіть документацію

## 📞 Підтримка

### Корисні команди
```bash
# Запуск з дебагом
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dlogging.level.com.andev.post=DEBUG"

# Перевірка портів
netstat -tlnp | grep 8081

# Очищення та перезапуск
mvn clean spring-boot:run
```

### Логи
- **INFO** - основні операції
- **DEBUG** - детальна інформація
- **WARN** - попередження
- **ERROR** - помилки

### Моніторинг
- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`
- Environment: `/actuator/env`

## 📝 Оновлення документації

При внесенні змін в код:
1. Оновіть відповідні розділи документації
2. Додайте нові приклади якщо потрібно
3. Перевірте актуальність всіх посилань
4. Оновіть версію документації

---

**Версія документації**: 1.0  
**Останнє оновлення**: 2025-08-06  
**Автор**: Post Service Team 