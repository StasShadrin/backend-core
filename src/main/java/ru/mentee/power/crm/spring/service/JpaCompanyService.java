package ru.mentee.power.crm.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.spring.repository.CompanyRepository;

/** Сервис для работы с компаниями. */
@Service
@RequiredArgsConstructor
public class JpaCompanyService {

  private final CompanyRepository companyRepository;

  /** Находит или создает новую компанию. */
  public Company findOrCreateByName(String name) {
    return companyRepository
        .findByName(name)
        .orElseGet(
            () -> {
              Company company = new Company();
              company.setName(name);
              return companyRepository.save(company);
            });
  }
}
