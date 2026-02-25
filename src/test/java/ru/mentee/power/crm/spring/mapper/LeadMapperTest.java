package ru.mentee.power.crm.spring.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse.StatusEnum;

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
            .status(StatusEnum.NEW)
            .createdAt(now)
            .updatedAt(now)
            .version(1L)
            .build();

    LeadResponse response = leadMapper.toResponse(lead);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(leadId);
    assertThat(response.getName()).isEqualTo("Иван Иванов");
    assertThat(response.getEmail()).isEqualTo("ivan@example.com");
    assertThat(response.getPhone()).isEqualTo("+79991234567");
    assertThat(response.getCompanyId()).isEqualTo(companyId);
    assertThat(response.getStatus()).isEqualTo(StatusEnum.NEW);
    assertThat(response.getCreatedAt()).isEqualTo(now);
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
            .status(StatusEnum.NEW)
            .createdAt(now)
            .updatedAt(now)
            .version(1L)
            .build();

    LeadResponse response = leadMapper.toResponse(lead);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(leadId);
    assertThat(response.getName()).isEqualTo("Иван Иванов");
    assertThat(response.getEmail()).isEqualTo("ivan@example.com");
    assertThat(response.getPhone()).isEqualTo("+79991234567");
    assertThat(response.getCompanyId()).isNull();
    assertThat(response.getStatus()).isEqualTo(StatusEnum.NEW);
    assertThat(response.getCreatedAt()).isEqualTo(now);
  }

  @Test
  void shouldHandleNullInput() {

    assertThat(leadMapper.toEntity(null)).isNull();
    assertThat(leadMapper.toResponse(null)).isNull();
  }
}
