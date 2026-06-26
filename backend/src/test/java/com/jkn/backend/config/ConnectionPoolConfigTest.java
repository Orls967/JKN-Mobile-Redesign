package com.jkn.backend.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConnectionPoolConfigTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void hikariCp_isConfiguredWithCalibratedParameters() {
        // Assert that the active DataSource is HikariDataSource
        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        
        HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

        // Verify TASK-04-B Calibration
        assertThat(hikariDataSource.getPoolName()).isEqualTo("JknHikariPool");
        assertThat(hikariDataSource.getMaximumPoolSize()).isEqualTo(20);
        assertThat(hikariDataSource.getMinimumIdle()).isEqualTo(10);
        assertThat(hikariDataSource.getConnectionTimeout()).isEqualTo(5000L);
        assertThat(hikariDataSource.getIdleTimeout()).isEqualTo(600000L);
        assertThat(hikariDataSource.getMaxLifetime()).isEqualTo(1800000L);
    }
}
