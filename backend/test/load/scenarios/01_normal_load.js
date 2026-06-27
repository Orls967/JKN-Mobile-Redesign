import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Normal Load: 100 VU, 10 menit
export const options = {
  vus: 100,
  duration: '10m',
  thresholds: {
    // target: 0% error (tolerate up to 1% theoretically, but aiming for 0)
    'http_req_failed': ['rate<0.01'], 
    // target: P95 < 1s (1000ms)
    'http_req_duration': ['p(95)<1000'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/queues';

export default function () {
  const payload = JSON.stringify({
    faskes_id: 'FASKES-NORMAL-TEST',
    user_id: `user-norm-${uuidv4()}`,
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

  // Jeda 1 detik untuk menyimulasikan laju pengguna konstan dan realistis
  sleep(1);
}
