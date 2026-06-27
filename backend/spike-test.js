import http from 'k6/http';
import { check } from 'k6';

export const options = {
  scenarios: {
    spike: {
      executor: 'constant-vus',
      vus: 1000,
      duration: '5s',
    },
  },
  thresholds: {
    // Threshold durasi untuk response sukses (201 Created)
    'http_req_duration{status:201}': ['p(95)<2000'],  
    'http_req_failed': ['rate<0.01'],                 
  },
};

export default function () {
  // 1. URL SUDAH DIGANTI KE LOCALHOST
  const url = 'http://localhost:8080/api/queues';
  
  // 2. PAYLOAD SUDAH DISESUAIKAN TIPE DATANYA
  const payload = JSON.stringify({
    patientId: 'P001',
    faskesId: 1,           // <-- Menggunakan angka (bukan string)
    counterName: 'UMUM'    // <-- Menggunakan counterName
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-Idempotency-Key': `test-${Math.random().toString(36).substring(2)}` 
    },
  };

  const res = http.post(url, payload, params);

  // 3. MENAMBAHKAN STATUS 201 SEBAGAI STATUS SUKSES
  check(res, {
    'status is 201, 409, or 429': (r) => [201, 409, 429].includes(r.status),
    'not a 500 error': (r) => r.status !== 500,
  });
}