package ru.mentee.power.crm.spring.service;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.mentee.power.crm.spring.dto.generated.LeadResponse.StatusEnum;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import ru.mentee.power.crm.entity.Company;
import ru.mentee.power.crm.entity.Lead;
import ru.mentee.power.crm.spring.exception.BadRequestException;
import ru.mentee.power.crm.spring.exception.DuplicateEmailException;
import ru.mentee.power.crm.spring.repository.CompanyRepository;
import ru.mentee.power.crm.spring.repository.JpaLeadRepository;

@SpringBootTest
@WireMockTest(httpPort = 8089)
class LeadServiceRetryTest {

  @Autowired private JpaLeadService leadService;
  @Autowired private JpaLeadRepository leadRepository;
  @Autowired private CompanyRepository companyRepository;

  private Company testCompany;
  private Lead validLead;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("email.validation.base-url", () -> "http://localhost:8089");
    registry.add("resilience4j.retry.instances.email-validation.wait-duration", () -> "100ms");
    registry.add("resilience4j.retry.instances.email-validation.max-attempts", () -> "3");
  }

  @BeforeEach
  void setUp() {
    leadRepository.deleteAll();
    companyRepository.deleteAll();

    testCompany =
        companyRepository.save(Company.builder().name("Test Company").industry("IT").build());

    validLead =
        Lead.builder()
            .name("Иван Иванов")
            .email("test@example.com")
            .phone("+79991234567")
            .status(StatusEnum.NEW)
            .company(testCompany)
            .build();
  }

  @Test
  void shouldRetryAndSucceed_whenFirstTwoAttemptsFail() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com"))
            .willReturn(serverError())
            .inScenario("Retry Scenario")
            .whenScenarioStateIs(Scenario.STARTED)
            .willSetStateTo("First Retry"));

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com"))
            .willReturn(serverError())
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("First Retry")
            .willSetStateTo("Second Retry"));

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com"))
            .willReturn(
                okJson(
                    """
                                    {
                                        "email": "test@example.com",
                                        "valid": true,
                                        "reason": "OK"
                                    }
                                    """))
            .inScenario("Retry Scenario")
            .whenScenarioStateIs("Second Retry"));

    Lead created = leadService.createLead(validLead);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getEmail()).isEqualTo("test@example.com");

    verify(
        3,
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com")));
  }

  @Test
  void shouldUseFallback_whenAllRetriesFailWith5xx() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com"))
            .willReturn(serverError().withBody("Service Unavailable")));

    Lead created = leadService.createLead(validLead);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();

    verify(
        3,
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com")));
  }

  @Test
  void shouldNotRetry_whenClientError4xxOccurs() {
    String invalidEmail = "invalid-format";
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo(invalidEmail))
            .willReturn(badRequest().withBody("{\"error\": \"Invalid email format\"}")));

    Lead invalidLead =
        Lead.builder()
            .name("Иван Иванов")
            .email(invalidEmail)
            .phone("+79991234567")
            .status(StatusEnum.NEW)
            .company(testCompany)
            .build();

    assertThatThrownBy(() -> leadService.createLead(invalidLead))
        .isInstanceOf(BadRequestException.class);

    verify(
        1,
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo(invalidEmail)));
  }

  @Test
  void shouldThrowException_whenEmailAlreadyExists() {
    Lead existingLead =
        Lead.builder()
            .name("Existing Lead")
            .email("test@example.com")
            .phone("+79991112233")
            .status(StatusEnum.NEW)
            .company(testCompany)
            .build();
    leadRepository.save(existingLead);

    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com"))
            .willReturn(
                okJson(
                    """
                                    {
                                        "email": "test@example.com",
                                        "valid": true,
                                        "reason": "OK"
                                    }
                                    """)));

    assertThatThrownBy(() -> leadService.createLead(validLead))
        .isInstanceOf(DuplicateEmailException.class);

    verify(0, getRequestedFor(urlPathEqualTo("/api/validate/email")));
  }

  @Test
  void shouldSucceedOnFirstAttempt_withValidEmail() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com"))
            .willReturn(
                okJson(
                    """
                                    {
                                        "email": "test@example.com",
                                        "valid": true,
                                        "reason": "Email is valid"
                                    }
                                    """)));

    Lead created = leadService.createLead(validLead);

    assertThat(created).isNotNull();
    assertThat(created.getId()).isNotNull();
    assertThat(created.getEmail()).isEqualTo("test@example.com");

    verify(
        1,
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@example.com")));
  }

  @Test
  void shouldRejectInvalidEmail_withoutCreatingLead() {
    stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("bad@example.com"))
            .willReturn(
                okJson(
                    """
                                    {
                                        "email": "bad@example.com",
                                        "valid": false,
                                        "reason": "Disposable email domain"
                                    }
                                    """)));

    Lead invalidLead =
        Lead.builder()
            .name("Bad Lead")
            .email("bad@example.com")
            .phone("+79991112233")
            .status(StatusEnum.NEW)
            .company(testCompany)
            .build();

    assertThatThrownBy(() -> leadService.createLead(invalidLead))
        .isInstanceOf(BadRequestException.class);

    assertThat(leadRepository.findByEmailNative("bad@example.com")).isEmpty();

    verify(
        1,
        getRequestedFor(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("bad@example.com")));
  }
}
