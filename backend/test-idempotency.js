import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

// Mengubah metrik ke 201 karena Spring Boot mengembalikan HttpStatus.CREATED
const count201 = new Counter('status_201_sukses');
const count409 = new Counter('status_409_konflik');

export const options = {
  scenarios: {
    double_booking_test: {
      executor: 'per-vu-iterations',
      vus: 20,
      iterations: 1,
      maxDuration: '5s',
    },
  },
};

export default function () {
  // 1. URL DIPERBARUI SESUAI CONTROLLER
  const url = 'http://localhost:8080/api/queues'; 
  
  const payload = JSON.stringify({
    counterName: "Poli Umum",
     userId: "user_test_k6",
    faskesId: 2
});

  const params = {
    headers: {
    "Content-Type": "application/json",
    "X-Idempotency-Key": "TEST-K6-001"
    },
  };

  const res = http.post(url, payload, params);

  // Debugging log untuk melihat respons asli server
  console.log(`Status HTTP: ${res.status} | Jawaban Server: ${res.body}`);

  // 3. TARGET STATUS DIPERBARUI KE 201 (CREATED)
  if (res.status === 201) count201.add(1);
  if (res.status === 409) count409.add(1);

  check(res, {
    'status adalah 201 (Created) atau 409 (Conflict)': (r) => r.status === 201 || r.status === 409,
  });
}