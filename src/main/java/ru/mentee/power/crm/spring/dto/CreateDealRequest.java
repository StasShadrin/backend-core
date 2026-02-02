package ru.mentee.power.crm.spring.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** DTO Deal*/
@Data
public class CreateDealRequest {
    @NotNull
    private String title;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private UUID companyId;
}