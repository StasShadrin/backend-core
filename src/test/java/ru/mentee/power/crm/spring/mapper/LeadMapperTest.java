package ru.mentee.power.crm.spring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.model.LeadStatus;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;

@SpringBootTest
class LeadMapperTest {

  @Autowired private LeadMapper leadMapper;

  @Test
  void shouldMapCreateRequestToEntity_whenValidData() {
    UUID companyId = UUID.randomUUID();
    CreateLeadRequest request =
        new CreateLeadRequest("Иван Иванов", "ivan@example.com", "+79991234567", companyId);

    Lead lead = leadMapper.toEntity(request);

    assertThat(lead).isNotNull();
    assertThat(lead.getId()).isNull();
    assertThat(lead.getName()).isEqualTo("Иван Иванов");
    assertThat(lead.getEmail()).isEqualTo("ivan@example.com");
    assertThat(lead.getPhone()).isEqualTo("+79991234567");
    assertThat(lead.getCompany()).isNull();
    assertThat(lead.getStatus()).isNull();
    assertThat(lead.getCreatedAt()).isNull();
    assertThat(lead.getUpdatedAt()).isNull();
    assertThat(lead.getVersion()).isNull();
  }

  @Test
  void shouldMapEntityToResponse_whenValidEntity() {
    UUID leadId = UUID.randomUUID();
    UUID companyId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    Company company =
        Company.builder().id(companyId).name("Тестовая компания").industry("IT").build();

    Lead lead =
        Lead.builder()
            .id(leadId)
            .name("Иван Иванов")
            .email("ivan@example.com")
            .phone("+79991234567")
            .company(company)
            .status(LeadStatus.NEW)
            .createdAt(now)
            .updatedAt(now)
            .version(1L)
            .build();

    LeadResponse response = leadMapper.toResponse(lead);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(leadId);
    assertThat(response.name()).isEqualTo("Иван Иванов");
    assertThat(response.email()).isEqualTo("ivan@example.com");
    assertThat(response.phone()).isEqualTo("+79991234567");
    assertThat(response.companyId()).isEqualTo(companyId);
    assertThat(response.status()).isEqualTo(LeadStatus.NEW);
    assertThat(response.createdAt()).isEqualTo(now);
  }

  @Test
  void shouldMapEntityToResponse_whenCompanyIsNull() {
    UUID leadId = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    Lead lead =
        Lead.builder()
            .id(leadId)
            .name("Иван Иванов")
            .email("ivan@example.com")
            .phone("+79991234567")
            .company(null)
            .status(LeadStatus.NEW)
            .createdAt(now)
            .updatedAt(now)
            .version(1L)
            .build();

    LeadResponse response = leadMapper.toResponse(lead);

    assertThat(response).isNotNull();
    assertThat(response.id()).isEqualTo(leadId);
    assertThat(response.name()).isEqualTo("Иван Иванов");
    assertThat(response.email()).isEqualTo("ivan@example.com");
    assertThat(response.phone()).isEqualTo("+79991234567");
    assertThat(response.companyId()).isNull();
    assertThat(response.status()).isEqualTo(LeadStatus.NEW);
    assertThat(response.createdAt()).isEqualTo(now);
  }

  @Test
  void shouldHandleNullInput() {

    assertThat(leadMapper.toEntity(null)).isNull();
    assertThat(leadMapper.toResponse(null)).isNull();
  }
}
