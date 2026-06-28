import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

// Connectivity Issue: 100 VU, 30% request di-abort (timeout) untuk chaos testing
export const options = {
  vus: 100,
  duration: '3m',
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/queues';

export default function () {
  const payload = JSON.stringify({
    faskes_id: 'FASKES-CHAOS-TEST',
    user_id: `user-chaos-${uuidv4()}`,
  });

  const isNetworkDrop = Math.random() < 0.3; // 30% probabilitas

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Idempotency-Key': uuidv4(),
    },
    // Jika chaos terpilih, paksa timeout dalam 1 milidetik agar langsung terputus (abort)
    timeout: isNetworkDrop ? '1ms' : '10s', 
  };

  const res = http.post(BASE_URL, payload, params);
  
  if (isNetworkDrop) {
    // Karena timeout sangat ekstrim, kita ekspektasikan error 0 (k6 error request timeout)
    check(res, {
      'request aborted (timeout)': (r) => r.error && r.error.includes('timeout'),
    });
  } else {
    // Sisanya harus sukses
    check(res, {
      'is status 200 or 202': (r) => r.status === 200 || r.status === 202,
    });
  }

  sleep(1);
}
