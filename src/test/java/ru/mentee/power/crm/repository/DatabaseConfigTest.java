package ru.mentee.power.crm.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConfigTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldConnectToH2Database() {
        String databaseProductName = jdbcTemplate.execute((ConnectionCallback<String>) connection ->
                connection.getMetaData().getDatabaseProductName()
        );

        assertThat(databaseProductName).isEqualTo("H2");
    }

    @Test
    void shouldHaveLeadsTableCreated() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'LEADS'",
                Integer.class
        );

        assertThat(count).isEqualTo(1);
    }
}