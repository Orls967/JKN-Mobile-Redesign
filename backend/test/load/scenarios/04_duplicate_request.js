import http from 'k6/http';
import { check } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Duplicate Request: 50 VU, tiap VU menembak 10 request bersamaan (batch)
export const options = {
  vus: 50,
  iterations: 50, // Tiap VU menjalankan function() 1 kali saja
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/queues';

export default function () {
  const faskesId = 'FASKES-DUPE-TEST';
  const userId = `user-dupe-${__VU}-${uuidv4()}`;
  const idempotencyKey = uuidv4(); // Sama untuk semua 10 request di VU ini

  const payload = JSON.stringify({
    faskes_id: faskesId,
    user_id: userId,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': idempotencyKey,
    },
  };

  // Persiapkan 10 request persis sama untuk dieksekusi serentak
  const reqs = Array(10).fill({
    method: 'POST',
    url: BASE_URL,
    body: payload,
    params: params,
  });

  // Eksekusi secara concurrent
  const responses = http.batch(reqs);

  // Verifikasi respons
  let successfulRequests = 0;
  let tooManyRequests = 0;

  responses.forEach((res) => {
    if (res.status === 200 || res.status === 202) {
      successfulRequests++;
    } else if (res.status === 429) {
      // 429 adalah idempotency conflict yang aman jika di-intercept rate limiter atau idempotency filter
      tooManyRequests++; 
    }
  });

  // Minimal ada 1 yang harus berhasil
  check(responses, {
    'at least 1 success per VU': () => successfulRequests >= 1,
  });
}
