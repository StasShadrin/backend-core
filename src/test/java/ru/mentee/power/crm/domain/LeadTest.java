package ru.mentee.power.crm.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class LeadTest {
    private final Lead lead = new Lead("L1",
            "test@example.com",
            "+71234567890",
            "TestCorp",
            "NEW");

    @Test
    void shouldReturnIdWhenGetIdCalled() {
        String id = lead.getId();
        assertThat(id).isEqualTo("L1");
    }

    @Test
    void shouldReturnEmailWhenGetEmailCalled() {
        String email = lead.getEmail();
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnPhoneWhenGetPhoneCalled() {
        String phone = lead.getPhone();
        assertThat(phone).isEqualTo("+71234567890");
    }

    @Test
    void shouldReturnCompanyWhenGetCompanyCalled() {
        String company = lead.getCompany();
        assertThat(company).isEqualTo("TestCorp");
    }

    @Test
    void shouldReturnStatusWhenGetStatusCalled() {
        String status = lead.getStatus();
        assertThat(status).isEqualTo("NEW");
    }

    @Test
    void shouldReturnFormattedStringWhenToStringCalled() {
        assertThat(lead).hasToString("Lead{" +
                                     "id='" + lead.getId() + '\'' +
                                     ", email='" + lead.getEmail() + '\'' +
                                     ", phone='" + lead.getPhone() + '\'' +
                                     ", company='" + lead.getCompany() + '\'' +
                                     ", status='" + lead.getStatus() + '\'' +
                                     '}');
    }
}