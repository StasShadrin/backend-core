package ru.mentee.power.crm.spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для создания лида с валидацией */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLeadRequest {
  @NotBlank(message = "{lead.name.notblank}")
  @Size(min = 2, max = 50, message = "{lead.name.size}")
  private String name;

  @NotBlank(message = "{lead.email.notblank}")
  @Email(message = "{lead.email.email}")
  @Size(max = 100, message = "{lead.email.size}")
  private String email;

  @NotBlank(message = "{lead.phone.notblank}")
  @Size(max = 20, message = "{lead.phone.size}")
  private String phone;

  @NotNull(message = "{company.id.notnull}")
  private UUID companyId;
}
