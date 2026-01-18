package ru.mentee.power.crm.service;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.mentee.power.crm.model.Lead;
import ru.mentee.power.crm.model.LeadBuilder;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.repository.LeadRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeadServiceMockTest {

    @Mock
    private LeadRepository mockRepository;

    private LeadService service;

    @BeforeEach
    void setUp() {
        service = new LeadService(mockRepository);
    }

    @Test
    void shouldCallRepositorySave_whenAddingNewLead() {
        // Given: Repository возвращает пустой Optional (email уникален)
        when(mockRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // When: настраиваем save чтобы возвращал переданный Lead
        when(mockRepository.save(any(Lead.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: вызываем бизнес-метод
        Lead result = service.addLead(LeadBuilder.builder()
                        .name("Test")
                .email("new@example.com")
                .phone("+71235")
                .company("Company")
                .status(LeadStatus.NEW)
                .build());

        // Then: проверяем что Repository.save() был вызван ровно 1 раз
        verify(mockRepository, times(1)).save(any(Lead.class));

        // Then: проверяем результат
        assertThat(result.email()).isEqualTo("new@example.com");
    }

    @Test
    void shouldNotCallSave_whenEmailExists() {
        // Given: Repository возвращает существующий Lead
        Lead existingLead = LeadBuilder.builder()
                .id(UUID.randomUUID())
                .email("existing@example.com")
                .phone("+71235")
                .company("Existing Company")
                .status( LeadStatus.CONTACTED)
                .build();
        when(mockRepository.findByEmail("existing@example.com"))
                .thenReturn(Optional.of(existingLead));

        // When/Then: ожидаем исключение
        assertThatThrownBy(() ->
                service.addLead(LeadBuilder.builder()
                        .name("Test")
                        .email("existing@example.com")
                        .phone("+71235")
                        .company("New Company")
                        .status(LeadStatus.NEW)
                        .build())
        ).isInstanceOf(IllegalStateException.class);

        // Then: save() НЕ должен быть вызван
        verify(mockRepository, never()).save(any(Lead.class));
    }

    @Test
    void shouldCallFindByEmail_beforeSave() {
        // Given
        when(mockRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(mockRepository.save(any(Lead.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        service.addLead(LeadBuilder.builder()
                .name("Test")
                .email("test@example.com")
                .phone("+71235")
                .company("Company")
                .status(LeadStatus.NEW)
                .build());

        // Then: проверяем порядок вызовов
        var inOrder = inOrder(mockRepository);
        inOrder.verify(mockRepository).findByEmail("test@example.com");
        inOrder.verify(mockRepository).save(any(Lead.class));
    }
}