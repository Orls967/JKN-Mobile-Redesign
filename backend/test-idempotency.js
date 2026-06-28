import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const count201 = new Counter('status_201_sukses');
const count409 = new Counter('status_409_konflik');

export const options = {
  scenarios: {
    double_booking_test: {
      executor: 'per-vu-iterations',
      vus: 20,
      iterations: 1,
      maxDuration: '100ms', // Memaksa 20 request dilepas dalam window 100ms
    },
  },
  thresholds: {
    // Memastikan distribusi status code persis sesuai target
    'status_201_sukses': ['count==1'],
    'status_409_konflik': ['count==19'],
    'http_req_failed': ['rate<1'] // 409 secara teknis gagal, jadi kita abaikan threshold bawaan, gunakan custom
  }
};

export default function () {
  const url = 'http://localhost:8080/api/queues'; 
  
  const payload = JSON.stringify({
    counterName: "Poli Umum",
    userId: "test_user_id",
    faskesId: 2
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      "X-Idempotency-Key": "test-idem-001"
    },
  };

  const res = http.post(url, payload, params);

  if (res.status === 201) count201.add(1);
  if (res.status === 409) count409.add(1);

  check(res, {
    'is status 201 or 409': (r) => r.status === 201 || r.status === 409,
    'no 500 error': (r) => r.status !== 500,
  });
}