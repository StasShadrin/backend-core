package ru.mentee.power.crm.spring.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.dto.generated.CreateLeadRequest;
import ru.mentee.power.crm.spring.dto.generated.LeadResponse;
import ru.mentee.power.crm.spring.dto.generated.UpdateLeadRequest;

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
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "version", ignore = true)
  void updateEntity(UpdateLeadRequest request, @MappingTarget Lead entity);
}
