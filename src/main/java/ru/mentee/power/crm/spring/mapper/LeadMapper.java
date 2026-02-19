package ru.mentee.power.crm.spring.mapper;

import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.dto.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.LeadResponse;
import ru.mentee.power.crm.spring.dto.UpdateLeadRequest;

/** Маппит лида в DTO и обратно */
@Mapper(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LeadMapper {
  /** Возвращает Entity из DTO запрос */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "company", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  Lead toEntity(CreateLeadRequest request);

  /** Возвращает DTO ответ из Entity */
  @Mapping(source = "company.id", target = "companyId")
  LeadResponse toResponse(Lead entity);

  /** Обновление переданного DTO */
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "company", ignore = true)
  @Mapping(target = "status", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "name", source = "request.name", qualifiedByName = "unwrapOptional")
  @Mapping(target = "email", source = "request.email", qualifiedByName = "unwrapOptional")
  @Mapping(target = "phone", source = "request.phone", qualifiedByName = "unwrapOptional")
  void updateEntity(UpdateLeadRequest request, @MappingTarget Lead entity);

  /** Преобразует Optional<T> в T для UpdateLeadRequest,
   * если Optional содержит значение, возвращает его.
   * Если нет, возвращает null и поле не изменяется. */
  @Named("unwrapOptional")
  default <T> T unwrapOptional(Optional<T> optional) {
    return optional.isPresent() ? optional.orElse(null) : null;
  }
}
