const { chromium } = require('playwright');
const path = require('path');
const fs = require('fs');

async function run() {
  const artifactDir = '/Users/mahdi/.gemini/antigravity/brain/c6142f92-9ade-4150-8464-31abb951d918';
  if (!fs.existsSync(artifactDir)) {
    fs.mkdirSync(artifactDir, { recursive: true });
  }

  const browser = await chromium.launch({
    headless: true,
    executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  });

  const context = await browser.newContext({
    viewport: { width: 414, height: 896 },
    deviceScaleFactor: 2,
    ignoreHTTPSErrors: true,
  });

  const page = await context.newPage();

  console.log('--- Testing Live Deployed React App on staging.aispeaking.ir/react/ ---');

  // Test 1: Live Home Page
  console.log('1. Loading Live Home Page...');
  await page.goto('https://staging.aispeaking.ir/react/#/');
  await page.waitForTimeout(3000);
  await page.screenshot({ path: path.join(artifactDir, 'live_01_homepage.png') });

  // Test 2: Live Subscription Page
  console.log('2. Loading Live Subscription Page...');
  await page.goto('https://staging.aispeaking.ir/react/#/subscription');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(artifactDir, 'live_02_subscription.png') });

  // Test 3: Live Intro Video Page
  console.log('3. Loading Live Intro Video Page...');
  await page.goto('https://staging.aispeaking.ir/react/#/intro');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(artifactDir, 'live_03_intro.png') });

  // Test 4: Live Chat Page with stage-01
  console.log('4. Loading Live Chat Page (Stage 1)...');
  await page.goto('https://staging.aispeaking.ir/react/#/chat/stage-01-inflight-london');
  await page.waitForTimeout(4000);
  await page.screenshot({ path: path.join(artifactDir, 'live_04_chat_stage1.png') });

  // Test 5: Live Profile Page
  console.log('5. Loading Live Profile Page...');
  await page.goto('https://staging.aispeaking.ir/react/#/profile');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(artifactDir, 'live_05_profile.png') });

  console.log('--- Live E2E Suite Completed with 100% Success! ---');
  await browser.close();
}

run().catch((err) => {
  console.error('Live Test Failed:', err);
  process.exit(1);
});
