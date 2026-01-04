## Сравнение: new внутри vs DI через конструктор
                            


### BAD: `new InMemoryLeadRepository()` внутри класса


```java
    public class LeadService {
        // Тесная связанность!
        private final LeadRepository repository =
        new InMemoryLeadRepository();                        
    }
```

#### Проблемы:

- Невозможно подставить mock в тестах  
- Невозможно заменить на PostgreSQL без изменения кода  
- Скрытая зависимость - не видно, что нужно для работы  

### GOOD: DI через конструктор

```java
    public class LeadService {
    private final LeadRepository repository;

        public LeadService(LeadRepository repository) {
            this.repository = repository;
        }
    }
```

#### Преимущества:

- В тестах передаём mock(`LeadRepository.class`)
- В production передаём InMemoryLeadRepository
- В будущем передаём `JpaLeadRepository` (Sprint 7)
- Зависимость явная — видно в конструкторе