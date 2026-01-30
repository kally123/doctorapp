#!/bin/bash
# Healthcare Platform Load Testing Script
# Uses k6 for load testing (https://k6.io)

# Configuration
BASE_URL="${BASE_URL:-http://localhost:8080}"
VIRTUAL_USERS="${VUS:-50}"
DURATION="${DURATION:-5m}"

echo "Healthcare Platform Load Test"
echo "=============================="
echo "Target: $BASE_URL"
echo "Virtual Users: $VIRTUAL_USERS"
echo "Duration: $DURATION"
echo ""

# Create k6 test script
cat > /tmp/healthcare-load-test.js << 'EOF'
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const searchLatency = new Trend('search_latency');
const bookingLatency = new Trend('booking_latency');

// Test configuration
export const options = {
  stages: [
    { duration: '1m', target: __ENV.VUS || 50 },    // Ramp up
    { duration: '3m', target: __ENV.VUS || 50 },    // Steady state
    { duration: '1m', target: 0 },                   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95% of requests under 2s
    errors: ['rate<0.05'],               // Error rate under 5%
    search_latency: ['p(95)<1000'],      // Search p95 under 1s
    booking_latency: ['p(95)<3000'],     // Booking p95 under 3s
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Test data
const specialties = ['Cardiology', 'Dermatology', 'Pediatrics', 'Orthopedics'];
const cities = ['New York', 'Los Angeles', 'Chicago', 'Houston'];

export default function () {
  const scenario = Math.random();

  if (scenario < 0.4) {
    // 40% - Search doctors
    searchDoctors();
  } else if (scenario < 0.6) {
    // 20% - View doctor profile
    viewDoctorProfile();
  } else if (scenario < 0.8) {
    // 20% - Browse articles
    browseArticles();
  } else if (scenario < 0.95) {
    // 15% - Check appointment slots
    checkAppointmentSlots();
  } else {
    // 5% - Book appointment (authenticated)
    bookAppointment();
  }

  sleep(1);
}

function searchDoctors() {
  const specialty = specialties[Math.floor(Math.random() * specialties.length)];
  const city = cities[Math.floor(Math.random() * cities.length)];

  const start = Date.now();
  const response = http.get(
    `${BASE_URL}/api/v1/search/doctors?specialty=${specialty}&city=${city}&page=0&size=10`
  );
  searchLatency.add(Date.now() - start);

  const success = check(response, {
    'search status is 200': (r) => r.status === 200,
    'search returns results': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.content && Array.isArray(body.content);
      } catch {
        return false;
      }
    },
  });

  errorRate.add(!success);
}

function viewDoctorProfile() {
  // Use a sample doctor ID (would be fetched from search in real test)
  const doctorId = 'sample-doctor-id-123';
  
  const response = http.get(`${BASE_URL}/api/v1/doctors/${doctorId}`);

  const success = check(response, {
    'profile status is 200 or 404': (r) => r.status === 200 || r.status === 404,
  });

  errorRate.add(!success);
}

function browseArticles() {
  // Get articles list
  const response = http.get(`${BASE_URL}/api/v1/articles?page=0&size=10`);

  const success = check(response, {
    'articles status is 200': (r) => r.status === 200,
  });

  errorRate.add(!success);

  // 50% chance to view article detail
  if (Math.random() < 0.5 && response.status === 200) {
    try {
      const articles = JSON.parse(response.body).content;
      if (articles && articles.length > 0) {
        const article = articles[Math.floor(Math.random() * articles.length)];
        http.get(`${BASE_URL}/api/v1/articles/${article.slug}`);
      }
    } catch (e) {
      // Ignore parse errors
    }
  }
}

function checkAppointmentSlots() {
  const doctorId = 'sample-doctor-id-123';
  const date = new Date().toISOString().split('T')[0];

  const response = http.get(
    `${BASE_URL}/api/v1/doctors/${doctorId}/slots?date=${date}`
  );

  const success = check(response, {
    'slots status is 200 or 404': (r) => r.status === 200 || r.status === 404,
  });

  errorRate.add(!success);
}

function bookAppointment() {
  // This would require authentication in a real test
  const payload = JSON.stringify({
    doctorId: 'sample-doctor-id-123',
    slotId: 'sample-slot-id',
    reason: 'Load test appointment',
    type: 'VIDEO',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      // 'Authorization': 'Bearer <token>' // Would be needed
    },
  };

  const start = Date.now();
  const response = http.post(
    `${BASE_URL}/api/v1/appointments`,
    payload,
    params
  );
  bookingLatency.add(Date.now() - start);

  // Expect 401 without auth or 201/400 with auth
  const success = check(response, {
    'booking response valid': (r) => 
      r.status === 201 || r.status === 400 || r.status === 401 || r.status === 403,
  });

  errorRate.add(!success);
}

export function handleSummary(data) {
  return {
    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
    '/tmp/load-test-results.json': JSON.stringify(data, null, 2),
  };
}

function textSummary(data, options) {
  // Custom summary formatting
  let summary = '\n=== Load Test Summary ===\n\n';
  
  summary += `Total Requests: ${data.metrics.http_reqs.values.count}\n`;
  summary += `Request Rate: ${data.metrics.http_reqs.values.rate.toFixed(2)}/s\n`;
  summary += `Failed Requests: ${data.metrics.http_req_failed?.values.passes || 0}\n`;
  summary += `\nLatency:\n`;
  summary += `  p50: ${data.metrics.http_req_duration.values['p(50)'].toFixed(2)}ms\n`;
  summary += `  p95: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms\n`;
  summary += `  p99: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms\n`;
  
  return summary;
}
EOF

# Run k6 test
if command -v k6 &> /dev/null; then
    k6 run \
        --env BASE_URL="$BASE_URL" \
        --env VUS="$VIRTUAL_USERS" \
        /tmp/healthcare-load-test.js
else
    echo "k6 is not installed. Install from https://k6.io/docs/getting-started/installation/"
    echo ""
    echo "The test script has been created at /tmp/healthcare-load-test.js"
    echo "You can run it manually with: k6 run /tmp/healthcare-load-test.js"
fi
