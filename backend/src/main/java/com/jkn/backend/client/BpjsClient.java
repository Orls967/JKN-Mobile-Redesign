package com.jkn.backend.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BpjsClient {

    private static final Logger log = LoggerFactory.getLogger(BpjsClient.class);
    
    // Properti khusus untuk Chaos Testing
    private boolean simulatedFailure = false;

    /**
     * Dummy method untuk memanggil BPJS External API.
     * Dibungkus dengan @CircuitBreaker. Jika terjadi failure berulang,
     * Resilience4j akan membuka circuit dan melemparkan CallNotPermittedException.
     */
    @CircuitBreaker(name = "bpjs_api")
    public String checkBpjs(String nik) {
        log.info("Memanggil external BPJS API untuk NIK: {}", nik);
        
        if (simulatedFailure) {
            log.error("Simulasi kegagalan koneksi ke BPJS API (Timeout/500)");
            throw new RuntimeException("Koneksi ke BPJS gagal");
        }
        
        return "DATA_VALID_BPJS";
    }
    
    public void setSimulatedFailure(boolean simulatedFailure) {
        this.simulatedFailure = simulatedFailure;
    }
}
