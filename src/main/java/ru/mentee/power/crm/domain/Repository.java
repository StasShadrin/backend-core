package ru.mentee.power.crm.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contract for an entity repository.
 * Defines basic CRUD operations: create, read, and delete.
 *
 * @param <T> the type of entity managed by this repository
 */
public interface Repository<T> {
    /**
     * Adds a new entity to the repository.
     * Duplicates (as determined by {@code equals()}) are ignored.
     */
    void add(T entity);

    /**
     * Removes an entity from the repository by its unique identifier.
     */
    void remove(UUID id);

    /**
     * Finds an entity by its unique identifier.
     *
     * @return an {@code Optional} containing the entity if found, or {@code Optional.empty()} if not found
     */
    Optional<T> findById(UUID id);

    /**
     * Returns all entities in the repository.
     * A defensive copy of the internal list is returned to prevent external modification.
     */
    List<T> findAll();
}