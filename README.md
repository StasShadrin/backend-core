# M-CRM Spring

[![Java CI with Spotless](https://github.com/StasShadrin/backend-core/actions/workflows/ci.yml/badge.svg)](https://github.com/StasShadrin/backend-core/actions/workflows/ci.yml)

CRM система на Spring Boot...

## Сравнение стеков Servlet vs Spring Boot
### Результаты интеграционного теста


| Метрика            | Servlet | Spring Boot | Комментарий                    |
|--------------------|---------|-------------|--------------------------------|
| Время старта       | ~500 ms | ~2500 ms    | Spring загружает IoC контейнер |
| HTTP 200 на /leads | ✅       | ✅           | Оба работают идентично         |
| Количество лидов   | N       | N           | Данные одинаковые              |
| Строк Java кода    | ~150    | ~30         | Контраст 5:1                   |

### Вывод

Оба стека возвращают идентичные данные, 
но Spring Boot требует в 5 раз меньше кода за счёт auto-configuration. 
Trade-off: Spring стартует медленнее из-за инициализации IoC контейнера.

*Данные получены из `StackComparisonTest.java`*
