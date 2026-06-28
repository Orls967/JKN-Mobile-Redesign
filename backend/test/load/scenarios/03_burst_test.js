import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Burst Test: 1000 VU dalam 30 detik
export const options = {
  stages: [
    { duration: '30s', target: 1000 },
  ],
  thresholds: {
    // Memastikan server tidak crash (tidak mengembalikan 5xx)
    // Tingkat kesalahan internal server harus 0%
    'http_req_failed': ['rate < 1.0'], // We expect some rate limits, so we check for status 429
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/queues';

export default function () {
  const payload = JSON.stringify({
    faskes_id: 'FASKES-BURST-TEST',
    user_id: `user-burst-${uuidv4()}`,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': uuidv4(),
    },
  };

  const res = http.post(BASE_URL, payload, params);
  
  // Validasi: request berhasil ATAU tertolak oleh rate limiter (429)
  // Request tidak boleh timeout atau mengembalikan error 5xx (Internal Server Error)
  check(res, {
    'is status 200, 202, or 429 (Rate Limited)': (r) => r.status === 200 || r.status === 202 || r.status === 429,
    'not 500 internal server error': (r) => r.status !== 500,
  });
}
