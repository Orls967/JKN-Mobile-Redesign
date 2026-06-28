import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Peak Traffic: 500 VU, 5 menit (jam sibuk)
export const options = {
  vus: 500,
  duration: '5m',
  thresholds: {
    // target: <1% error
    'http_req_failed': ['rate<0.01'], 
    // target: P95 < 2s (2000ms)
    'http_req_duration': ['p(95)<2000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/queues';

export default function () {
  const payload = JSON.stringify({
    faskes_id: 'FASKES-PEAK-TEST',
    user_id: `user-peak-${uuidv4()}`,
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': uuidv4(),
    },
  };

  const res = http.post(BASE_URL, payload, params);
  
  check(res, {
    'is status 200 or 202': (r) => r.status === 200 || r.status === 202,
  });

  // Jeda kecil karena antrean sangat padat
  sleep(0.5);
}
