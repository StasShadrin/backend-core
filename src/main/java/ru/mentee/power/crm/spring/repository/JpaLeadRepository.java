package ru.mentee.power.crm.spring.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import jakarta.persistence.LockModeType;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;

/** Контракт на операции по сохранению лидов с использованием CRUD и поиска по email адресу.*/
public interface JpaLeadRepository extends JpaRepository<Lead, UUID> {
    /** Нативный поиск по email */
    @Query(value = "SELECT * FROM leads WHERE email = ?1", nativeQuery = true)
    Optional<Lead> findByEmailNative(String email);

    /** Нативный поиск по статусу */
    @Query(value = "SELECT * FROM leads WHERE status = ?1", nativeQuery = true)
    List<Lead> findByStatusNative(String status);

    /** Поиск по тексту + статусу (аналог findLeads) */
    @Query(value = """
            SELECT l.* FROM leads l
            LEFT JOIN companies c ON l.company_id = c.id
            WHERE (:status IS NULL OR l.status = :status)
              AND (
                :search IS NULL 
                OR LOWER(l.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(l.email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """, nativeQuery = true)
    List<Lead> findLeadsNative(@Param("search") String search, @Param("status") String status);


    // Derived query methods (Spring генерирует SQL автоматически)

    /**
     * Поиск лида по email (точное совпадение).
     * SQL: SELECT * FROM leads WHERE email = ?
     */
    Optional<Lead> findByEmail(String email);

    /**
     * Поиск лидов по статусу.
     * SQL: SELECT * FROM leads WHERE status = ?
     */
    List<Lead> findByStatus(LeadStatus status);

    /**
     *  Поиск по компании.
     *  SQL: SELECT * FROM leads WHERE company = ?
     */
    List<Lead> findByCompany(Company company);

    /**
     *  Подсчёт лидов по статусу.
     *  SQL: SELECT COUNT(*) FROM leads WHERE status = ?
     */
    long countByStatus(LeadStatus status);

    /**
     * Проверка существования по email.
     * SQL: SELECT COUNT(*) > 0 FROM leads WHERE email = ?
     */
    boolean existsByEmail(String email);

    /**
     * Поиск лидов по части email (LIKE запрос).
     * SQL: SELECT * FROM leads WHERE email LIKE '%emailPart%'
     */
    List<Lead> findByEmailContaining(String emailPart);

    /**
     * Поиск по статусу И компании.
     * SQL: SELECT * FROM leads WHERE status = ? AND company = ?
     */
    List<Lead> findByStatusAndCompany(LeadStatus status, Company company);

    /**
     * Поиск с сортировкой.
     * SQL: SELECT * FROM leads WHERE status = ? ORDER BY created_at DESC
     */
    List<Lead> findByStatusOrderByCreatedAtDesc(LeadStatus status);


    // JPQL запросы (объектный язык)

    /**
     * Поиск лидов по списку статусов (JPQL).
     * JPQL: SELECT l FROM Lead l WHERE l.status IN :statuses
     * SQL: SELECT * FROM leads WHERE status IN (?, ?, ...)
     */
    @Query("SELECT l FROM Lead l WHERE l.status IN :statuses")
    List<Lead> findByStatusIn(@Param("statuses") List<LeadStatus> statuses);

    /**
     * Поиск лидов созданных после определённой даты
     * JPQL: SELECT l FROM Lead l WHERE l.createdAt > :date
     * SQL: SELECT * FROM leads WHERE created_at > ?
     */
    @Query("SELECT l FROM Lead l WHERE l.createdAt > :date")
    List<Lead> findCreatedAfter(@Param("date") OffsetDateTime date);

    /**
     * Поиск лидов с фильтрацией и сортировкой (JPQL).
     * JPQL: SELECT l FROM Lead l WHERE l.company = :company ORDER BY l.createdAt DESC
     * SQL: SELECT * FROM leads WHERE company = ? ORDER BY created_at DESC
     */
    @Query("SELECT l FROM Lead l WHERE l.company = :company ORDER BY l.createdAt DESC")
    List<Lead> findByCompanyOrderedByDate(@Param("company") Company company);

    /**
     *  Если у Lead есть связь @ManyToOne с Company.
     */
    @Query("SELECT l FROM Lead l JOIN l.company c WHERE c.name = :companyName")
    List<Lead> findByCompanyName(@Param("companyName") String name);


    // Методы с пагинацией

    /**
     * Поиск всех лидов с пагинацией (переопределяем из JpaRepository).
     * Клиент: PageRequest.of(0, 20) — первая страница, 20 элементов
     */
    @NonNull
    Page<Lead> findAll(@NonNull Pageable pageable);

    /**
     * Поиск по статусу с пагинацией (derived method).
     */
    Page<Lead> findByStatus(LeadStatus status, Pageable pageable);

    /**
     *  Поиск по компании с пагинацией.
     */
    Page<Lead> findByCompany(Company company, Pageable pageable);

    /**
     * JPQL запрос с пагинацией.
     */
    @Query("SELECT l FROM Lead l WHERE l.status IN :statuses")
    Page<Lead> findByStatusInPaged(@Param("statuses") List<LeadStatus> statuses, Pageable pageable);


    // Bulk операции

    /**
     * Массовое обновление статуса лидов.
     * ВАЖНО: требует @Transactional на уровне Service!
     *
     * @return количество обновлённых строк
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Lead l SET l.status = :newStatus WHERE l.status = :oldStatus")
    int updateStatusBulk(
            @Param("oldStatus") LeadStatus oldStatus,
            @Param("newStatus") LeadStatus newStatus
    );

    /**
     * Массовое удаление по статусу
     */
    @Modifying
    @Query("DELETE FROM Lead l WHERE l.status = :status")
    int deleteByStatusBulk(@Param("status") LeadStatus status);

    /**
     * Pessimistic lock для критических операций (конверсия Lead -> Deal)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lead l WHERE l.id = :id")
    Optional<Lead> findByIdForUpdate(@Param("id") UUID id);

    /**
     * Метод с @Lock для блокировки по email
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lead l WHERE l.email = :email")
    Optional<Lead> findByEmailForUpdate(@Param("email") String email);

    /** Метод поиска по email */
    Optional<Lead> findByEmailIgnoreCase(String email);
}