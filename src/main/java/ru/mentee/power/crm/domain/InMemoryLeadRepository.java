package ru.mentee.power.crm.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory implementation of {@link Repository} for {@link Lead} entities.
 * Uses an {@link ArrayList} as internal storage with defensive copying and duplicate prevention.
 */
public class InMemoryLeadRepository implements Repository<Lead> {

    private final List<Lead> storage = new ArrayList<>();

    @Override
    public void add(Lead entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Lead must not be null");
        }
        if (!storage.contains(entity)) {
            storage.add(entity);
        }
    }

    @Override
    public void remove(UUID id) {
        storage.removeIf(lead -> lead.getId().equals(id));

    }

    @Override
    public Optional<Lead> findById(UUID id) {
        return storage.stream()
                .filter(lead -> lead.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Lead> findAll() {
        return new ArrayList<>(storage);
    }
}
