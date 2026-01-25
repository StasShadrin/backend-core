package ru.mentee.power.crm.spring.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.mentee.power.crm.entity.Lead;

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
            SELECT * FROM leads 
            WHERE (:status IS NULL OR status = :status)
              AND (
                :search IS NULL 
                OR LOWER(name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(email) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(company) LIKE LOWER(CONCAT('%', :search, '%'))
              )
            """, nativeQuery = true)
    List<Lead> findLeadsNative(@Param("search") String search, @Param("status") String status);

}
