import http from 'k6/http';
import {check} from 'k6';
import {htmlReport} from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';

// ---------- تنظیمات ----------
const BASE_URL   = 'http://localhost:8081';
const HOME_PATH  = '/api/v1/home';
const TARGET_VUS = 50;
const P95_MS     = 1500;

// ---------- سناریو و آستانه ----------
export const options = {
    scenarios: {
        home_50_once: {
            executor: 'per-vu-iterations',
            vus: TARGET_VUS,
            iterations: 1,
            maxDuration: '1m',
        },
    },
    thresholds: {
        'http_req_failed': ['rate<0.01'],
        'http_req_duration{endpoint:home}': [`p(95)<${P95_MS}`],
        'checks': ['rate>0.99'],
    },
    summaryTrendStats: ['avg','med','p(90)','p(95)','p(99)'],
};

// ---------- خروجی گزارش ----------
export function handleSummary(data) {
    return {
        // این خطوط فایل‌ها را در پوشه پروژه شما روی کامپیوتر لوکال ایجاد می‌کنند
        'home-50-summary.html': htmlReport(data),
        'home-50-summary.json': JSON.stringify(data, null, 2),
    };
}

// ---------- منطق اصلی تست ----------
export default function () {
    const res = http.get(`${BASE_URL}${HOME_PATH}`, {
        tags: { endpoint: 'home' },
    });

    const body = res.json();
    const dataList = body && Array.isArray(body.data) ? body.data : null;

    check(res, {
        '1. status is 200': (r) => r.status === 200,
        '2. response has a "data" array': () => dataList !== null,
        '3. at least one "Banner" block exists': () =>
            dataList ? dataList.some(item => item.type === 'Banner') : false,
        '4. a "Categories" block exists': () =>
            dataList ? dataList.some(item => item.type === 'Categories') : false,
        '5. at least one "Scenarios" block exists': () =>
            dataList ? dataList.some(item => item.type === 'Scenarios') : false,
        '6. "IELTS Exam" scenario block exists by title': () =>
            dataList ? dataList.some(item => item.type === 'Scenarios' && item.title === 'IELTS Exam') : false,
    });
}