package ru.mentee.power.crm.spring.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mentee.power.crm.model.LeadStatus;

/** DTO для обновления лида с валидацией, возвращает Optional */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateLeadRequest {

  private Optional<@NotBlank @Size(min = 2, max = 50, message = "{lead.name.size}") String> name;

  private Optional<
          @NotBlank @Email(message = "{lead.email.email}")
          @Size(max = 100, message = "{lead.email.size}") String>
      email;

  private Optional<@Size(max = 20, message = "{lead.phone.size}") String> phone;

  private Optional<UUID> companyId;

  private Optional<LeadStatus> status;
}
