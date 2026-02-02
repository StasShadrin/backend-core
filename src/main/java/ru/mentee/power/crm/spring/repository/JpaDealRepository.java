package ru.mentee.power.crm.spring.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.mentee.power.crm.entity.Deal;

/** Репозиторий для работы Deal с БД*/
public interface JpaDealRepository extends JpaRepository<Deal, UUID> {
}
