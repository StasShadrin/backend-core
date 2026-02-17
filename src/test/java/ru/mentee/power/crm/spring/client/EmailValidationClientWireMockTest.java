package ru.mentee.power.crm.spring.client;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class EmailValidationClientWireMockTest {

  @RegisterExtension
  static WireMockExtension wireMockExtension =
      WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

  @Autowired private EmailValidationClient emailValidationClient;

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("email.validation.base-url", wireMockExtension::baseUrl);
  }

  @Test
  void shouldReturnValid_whenEmailIsCorrect() {
    wireMockExtension.stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("john@example.com"))
            .willReturn(
                okJson(
                    """
                                {
                                    "email": "john@example.com",
                                    "valid": true,
                                    "reason": "Email exists"
                                }
                                """)));

    EmailValidationResponse response = emailValidationClient.validateEmail("john@example.com");

    assertThat(response).isNotNull();
    assertThat(response.valid()).isTrue();
    assertThat(response.email()).isEqualTo("john@example.com");
  }

  @Test
  void shouldReturnInvalid_whenEmailIsIncorrect() {
    wireMockExtension.stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("invalid-email"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBodyFile("invalid-email-response.json")));

    EmailValidationResponse response = emailValidationClient.validateEmail("invalid-email");

    assertThat(response).isNotNull();
    assertThat(response.valid()).isFalse();
  }

  @Test
  void shouldHandleServerError_whenExternalServiceFails() {
    wireMockExtension.stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .withQueryParam("email", equalTo("test@test.com"))
            .willReturn(serverError().withBody("Internal Server Error")));

    EmailValidationResponse response = emailValidationClient.validateEmail("test@test.com");

    assertThat(response).isNotNull();
    assertThat(response.valid()).isTrue();
    assertThat(response.email()).isEqualTo("test@test.com");
    assertThat(response.reason()).contains("недоступен");
  }

  @Test
  void shouldHandleTimeout_whenExternalServiceIsSlow() {
    wireMockExtension.stubFor(
        get(urlPathEqualTo("/api/validate/email"))
            .willReturn(okJson("{\"valid\": true}").withFixedDelay(15000)));

    EmailValidationResponse response = emailValidationClient.validateEmail("slow@test.com");

    assertThat(response).isNotNull();
    assertThat(response.valid()).isTrue(); // fallback
    assertThat(response.reason()).contains("недоступен");
  }
}
