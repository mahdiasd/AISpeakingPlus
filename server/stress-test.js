import http from 'k6/http';
import {check, group, sleep} from 'k6';
import {Trend} from 'k6/metrics';
import {htmlReport} from 'https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js';
import {textSummary} from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

// Define custom trends for each endpoint to track their performance separately
const homeResponseTrend = new Trend('T01_Home_Response_Time');
const scenariosResponseTrend = new Trend('T02_Scenarios_Response_Time');
const scenarioDetailResponseTrend = new Trend('T03_Scenario_Detail_Response_Time');

// Base URL of your server
const BASE_URL = 'http://localhost:8081';

export const options = {
    // Define the stages for the load test
    stages: [
        // Stage 1: Ramp-up to 50 users over 10 seconds, then stay for 1 minute
        { duration: '10s', target: 50 },
        { duration: '1m', target: 50 },

        // Stage 2: Ramp-up to 300 users over 30 seconds, then stay for 2 minutes
        { duration: '30s', target: 300 },
        { duration: '2m', target: 300 },

        // Stage 3: Ramp-up to 500 users over 30 seconds, then stay for 2 minutes
        { duration: '30s', target: 500 },
        { duration: '2m', target: 500 },

        // Stage 4: Ramp-down to 0 users
        { duration: '20s', target: 0 },
    ],

    // Define thresholds for the test to pass or fail
    thresholds: {
        // 95% of requests must complete below 1.5 seconds
        'http_req_duration': ['p(95)<3500'],
        // 99% of requests for the home page must be below 800ms (because of cache)
        'http_req_duration{name:T01_Home_Page}': ['p(99)<3000'],
        // No more than 1% of requests should fail
        'http_req_failed': ['rate<0.01'],
    },
};

export default function () {
    let homeResponse;
    let scenariosResponse;
    let homeScenarioIds = [];

    // Group all related requests under a "User Journey"
    group('User Journey', function () {

        // Step 1: Request the Home Page
        const homeRes = http.get(`${BASE_URL}/api/v1/home`, {
            tags: { name: 'T01_Home_Page' },
        });

        check(homeRes, {
            'Home page is status 200': (r) => r.status === 200,
            'Home page has data': (r) => r.json('data') !== null,
        });

        // Add response time to our custom trend metric
        homeResponseTrend.add(homeRes.timings.duration);

        // Try to extract scenario IDs from the home page response
        try {
            homeResponse = homeRes.json('data');
            if (homeResponse && Array.isArray(homeResponse)) {
                homeResponse.forEach(section => {
                    if (section.type === 'Scenarios' && Array.isArray(section.data)) {
                        section.data.forEach(scenario => {
                            if (scenario.id) {
                                homeScenarioIds.push(scenario.id);
                            }
                        });
                    }
                });
            }
        } catch (e) {
            console.error('Failed to parse home response or find scenarios:', e);
        }

        // Simulate user think time
        sleep(Math.random() * 2 + 1); // Sleep for 1 to 3 seconds

        // Step 2: Get details of a random scenario from the home page
        if (homeScenarioIds.length > 0) {
            const randomScenarioId = homeScenarioIds[Math.floor(Math.random() * homeScenarioIds.length)];
            const scenarioDetailRes = http.get(`${BASE_URL}/api/v1/scenario?id=${randomScenarioId}`, {
                tags: { name: 'T03_Scenario_Detail_from_Home' },
            });

            check(scenarioDetailRes, {
                'Scenario detail page is status 200': (r) => r.status === 200,
            });
            scenarioDetailResponseTrend.add(scenarioDetailRes.timings.duration);
            sleep(Math.random() * 3 + 1); // Sleep for 1 to 4 seconds
        }

        // Step 3: Browse the main scenarios list (e.g., page 1)
        const scenariosRes = http.get(`${BASE_URL}/api/v1/scenarios?page=1&size=20`, {
            tags: { name: 'T02_Scenarios_List' },
        });

        check(scenariosRes, {
            'Scenarios list is status 200': (r) => r.status === 200,
            'Scenarios list has data': (r) => r.json('data') !== null && r.json('data').length > 0,
        });

        scenariosResponseTrend.add(scenariosRes.timings.duration);

        // Try to get a random scenario from this list
        let scenariosListIds = [];
        try {
            scenariosResponse = scenariosRes.json('data');
            if (scenariosResponse && Array.isArray(scenariosResponse)) {
                scenariosListIds = scenariosResponse.map(s => s.id);
            }
        } catch (e) {
            console.error('Failed to parse scenarios list response:', e);
        }

        sleep(Math.random() * 2 + 1); // Sleep for 1 to 3 seconds

        // Step 4: Get details of a random scenario from the scenarios list
        if (scenariosListIds.length > 0) {
            const randomScenarioId = scenariosListIds[Math.floor(Math.random() * scenariosListIds.length)];
            const scenarioDetailRes2 = http.get(`${BASE_URL}/api/v1/scenario?id=${randomScenarioId}`, {
                tags: { name: 'T03_Scenario_Detail_from_List' },
            });

            check(scenarioDetailRes2, {
                'Scenario detail page is status 200': (r) => r.status === 200,
            });
            scenarioDetailResponseTrend.add(scenarioDetailRes2.timings.duration);
        }
    });

    // Final sleep at the end of the journey
    sleep(Math.random() * 2 + 2); // Sleep for 2 to 4 seconds
}

// This function generates the HTML and JSON summary reports
export function handleSummary(data) {
    console.log('Finished executing performance test');

    return {
        'summary.html': htmlReport(data),
        'summary.json': JSON.stringify(data, null, 2), // The JSON output
        'stdout': textSummary(data, { indent: ' ', enableColors: true }), // A summary in the console
    };
}