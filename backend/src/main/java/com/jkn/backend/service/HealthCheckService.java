package com.jkn.backend.service;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Health Check Service (TASK-07-C).
 *
 * Melakukan pengecekan kesehatan terhadap:
 * - Database: SELECT 1 (lightweight ping)
 * - Connection Pool: HikariCP stats
 * - External BPJS: stub (not_configured)
 *
 * Hasil di-cache selama 10 detik agar health check tidak membebani DB.
 */
@Service
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);
    private static final long CACHE_DURATION_MS = 10_000; // 10 detik

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Value("${info.app.version:0.0.1-SNAPSHOT}")
    private String appVersion;

    // Cache
    private volatile Map<String, Object> cachedResult;
    private volatile long lastCheckTime = 0;

    public HealthCheckService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Full health check — GET /health
     */
    public Map<String, Object> getFullHealth() {
        long now = System.currentTimeMillis();

        // Return cache jika masih valid
        if (cachedResult != null && (now - lastCheckTime) < CACHE_DURATION_MS) {
            return cachedResult;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> checks = new LinkedHashMap<>();

        // 1. Database check
        checks.put("database", checkDatabase());

        // 2. Connection Pool check
        checks.put("connection_pool", checkConnectionPool());

        // 3. External BPJS (stub)
        Map<String, Object> bpjsCheck = new LinkedHashMap<>();
        bpjsCheck.put("status", "not_configured");
        bpjsCheck.put("circuit_state", "N/A");
        checks.put("external_bpjs", bpjsCheck);

        // Determine overall status
        String overallStatus = determineOverallStatus(checks);

        result.put("status", overallStatus);
        result.put("version", appVersion);
        result.put("uptime_seconds", getUptimeSeconds());
        result.put("checks", checks);

        // Update cache
        this.cachedResult = result;
        this.lastCheckTime = now;

        return result;
    }

    /**
     * Readiness probe — GET /health/ready
     */
    public boolean isReady() {
        Map<String, Object> health = getFullHealth();
        String status = (String) health.get("status");
        return "healthy".equals(status) || "degraded".equals(status);
    }

    /**
     * Liveness probe — GET /health/live
     */
    public boolean isAlive() {
        // Proses hidup jika thread ini bisa berjalan
        return true;
    }

    private Map<String, Object> checkDatabase() {
        Map<String, Object> dbCheck = new LinkedHashMap<>();
        try {
            long start = System.currentTimeMillis();
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long latency = System.currentTimeMillis() - start;

            dbCheck.put("status", "ok");
            dbCheck.put("latency_ms", latency);
        } catch (Exception e) {
            log.error("Database health check failed", e);
            dbCheck.put("status", "error");
            dbCheck.put("error", e.getMessage());
        }
        return dbCheck;
    }

    private Map<String, Object> checkConnectionPool() {
        Map<String, Object> poolCheck = new LinkedHashMap<>();
        try {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                HikariPoolMXBean poolMXBean = hikariDataSource.getHikariPoolMXBean();
                if (poolMXBean != null) {
                    poolCheck.put("status", "ok");
                    poolCheck.put("active", poolMXBean.getActiveConnections());
                    poolCheck.put("idle", poolMXBean.getIdleConnections());
                    poolCheck.put("waiting", poolMXBean.getThreadsAwaitingConnection());
                } else {
                    poolCheck.put("status", "ok");
                    poolCheck.put("note", "Pool MXBean not available yet");
                }
            } else {
                poolCheck.put("status", "ok");
                poolCheck.put("note", "Non-HikariCP datasource");
            }
        } catch (Exception e) {
            log.error("Connection pool health check failed", e);
            poolCheck.put("status", "error");
            poolCheck.put("error", e.getMessage());
        }
        return poolCheck;
    }

    private String determineOverallStatus(Map<String, Object> checks) {
        boolean hasError = false;
        boolean hasDegraded = false;

        for (Map.Entry<String, Object> entry : checks.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> check = (Map<String, Object>) entry.getValue();
                String status = (String) check.get("status");

                if ("error".equals(status)) {
                    // Database down = unhealthy
                    if ("database".equals(entry.getKey())) {
                        return "unhealthy";
                    }
                    hasDegraded = true;
                }
            }
        }

        if (hasDegraded) return "degraded";
        return "healthy";
    }

    private long getUptimeSeconds() {
        return ManagementFactory.getRuntimeMXBean().getUptime() / 1000;
    }
}
