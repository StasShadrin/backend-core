package ru.mentee.power.crm.spring.service;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LeadLockingServiceTest {

    @Autowired
    private LeadLockingService leadLockingService;

    @Autowired
    private JpaLeadRepository leadRepository;

    @SuppressWarnings("resource")
    @Test
    void shouldPreventLostUpdate_whenPessimisticLockUsed() throws Exception {
        // Given: Lead с начальным статусом
        Lead lead = Lead.builder()
                .name("Lead 1")
                .email("concurrent@test.com")
                .phone("123456789")
                .company("company A")
                .status(LeadStatus.NEW)
                .build();
        lead = leadRepository.save(lead);
        UUID leadId = lead.getId();

        // When: Два потока одновременно обновляют Lead с pessimistic lock
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        Future<LeadStatus> task1 = executor.submit(() -> {
            startLatch.await(); // Синхронизируем старт
            Lead updated = leadLockingService.convertLeadToDealWithLock(leadId, LeadStatus.CONVERTED);
            doneLatch.countDown();
            return updated.getStatus();
        });

        Future<LeadStatus> task2 = executor.submit(() -> {
            startLatch.await();
            Lead updated = leadLockingService.convertLeadToDealWithLock(leadId, LeadStatus.QUALIFIED);
            doneLatch.countDown();
            return updated.getStatus();
        });

        startLatch.countDown(); // Запускаем оба потока одновременно
        doneLatch.await(10, TimeUnit.SECONDS); // Ждём завершения

        // Then: Оба обновления успешны, вторая транзакция ждала первую
        LeadStatus status1 = task1.get();
        LeadStatus status2 = task2.get();

        assertThat(status1).isIn(LeadStatus.CONVERTED, LeadStatus.QUALIFIED);
        assertThat(status2).isIn(LeadStatus.CONVERTED, LeadStatus.QUALIFIED);
        assertThat(status1).isNotEqualTo(status2); // Разные статусы (не должны быть)

        // Финальный статус — последняя commit'нутая транзакция
        Lead finalLead = leadRepository.findById(leadId).orElseThrow();
        assertThat(finalLead.getStatus()).isIn(LeadStatus.CONVERTED, LeadStatus.QUALIFIED);

        executor.shutdown();
    }

    @SuppressWarnings("resource")
    @Test
    void shouldThrowOptimisticLockException_whenConcurrentUpdateWithoutLock() throws Exception {
        // Given: Lead с optimistic locking через @Version
        Lead lead = Lead.builder()
                .name("Lead 1")
                .phone("123456789")
                .email("optimistic@test.com")
                .company("company A")
                .status(LeadStatus.NEW)
                .build();
        lead = leadRepository.save(lead);
        UUID leadId = lead.getId();

        // When: Два потока одновременно обновляют БЕЗ pessimistic lock
        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch startLatch = new CountDownLatch(1);

        Future<?> task1 = executor.submit(() -> {
            startLatch.await();
            leadLockingService.updateLeadStatusOptimistic(leadId, LeadStatus.CONVERTED);
            return null;
        });

        Future<?> task2 = executor.submit(() -> {
            startLatch.await();
            leadLockingService.updateLeadStatusOptimistic(leadId, LeadStatus.QUALIFIED);
            return null;
        });

        startLatch.countDown();

        // Then: Одна транзакция успешна, вторая выбрасывает OptimisticLockException
        boolean exceptionThrown = false;
        try {
            task1.get(5, TimeUnit.SECONDS);
            task2.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            // Одна из транзакций должна выбросить OptimisticLockException
            assertThat(e.getCause())
                    .isInstanceOfAny(ObjectOptimisticLockingFailureException.class);
            exceptionThrown = true;
        }

        assertThat(exceptionThrown).isTrue();
        executor.shutdown();
    }

    @SuppressWarnings("resource")
    @Test
    void shouldDetectDeadlock_whenLeadsLockedInDifferentOrder() throws Exception {
        // Given: два лида
        Lead lead1 = Lead.builder()
                .name("Lead 1")
                .email("lead1@test.com")
                .phone("123")
                .company("Company A")
                .status(LeadStatus.NEW)
                .build();
        Lead lead2 = Lead.builder()
                .name("Lead 2")
                .email("lead2@test.com")
                .phone("456")
                .company("Company B")
                .status(LeadStatus.NEW)
                .build();

        lead1 = leadRepository.save(lead1);
        lead2 = leadRepository.save(lead2);

        UUID id1 = lead1.getId();
        UUID id2 = lead2.getId();

        // When: два потока блокируют лиды в разном порядке
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        // Поток A: блокирует id1, затем id2
        Future<?> threadA = executor.submit(() -> {
            startLatch.await();
            leadLockingService.processTwoLeadsInOrder(id1, id2);
            return null;
        });

        // Поток B: блокирует id2, затем id1
        Future<?> threadB = executor.submit(() -> {
            startLatch.await();
            leadLockingService.processTwoLeadsInOrder(id2, id1);
            return null;
        });

        startLatch.countDown();

        // Then: один из потоков должен получить CannotAcquireLockException (deadlock)
        boolean deadlockDetected = false;
        try {
            threadA.get(5, TimeUnit.SECONDS);
            threadB.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException _) {
            deadlockDetected = true;
        }

        assertThat(deadlockDetected).isTrue();

        executor.shutdownNow();
    }
}