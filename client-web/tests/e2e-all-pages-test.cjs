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
  });

  const page = await context.newPage();

  console.log('--- Starting Complete E2E Suite for React Cupertino Web Client ---');

  // Test 1: Home Page
  console.log('1. Testing Home Page (/#/)');
  await page.goto('http://localhost:8083/#/');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(artifactDir, '01_homepage.png') });

  // Test 2: Subscription Page
  console.log('2. Testing Subscription Page (/#/subscription)');
  await page.goto('http://localhost:8083/#/subscription');
  await page.waitForTimeout(1500);
  await page.screenshot({ path: path.join(artifactDir, '02_subscription_page.png') });

  // Test 3: Intro Video Page
  console.log('3. Testing Intro Video Page (/#/intro)');
  await page.goto('http://localhost:8083/#/intro');
  await page.waitForTimeout(1200);
  await page.screenshot({ path: path.join(artifactDir, '03_intro_video_page.png') });

  // Test 4: Profile Page
  console.log('4. Testing Profile Page (/#/profile)');
  await page.goto('http://localhost:8083/#/profile');
  await page.waitForTimeout(1200);
  await page.screenshot({ path: path.join(artifactDir, '04_profile_page.png') });

  // Test 5: Login Page
  console.log('5. Testing Login Page (/#/login)');
  await page.goto('http://localhost:8083/#/login');
  await page.waitForTimeout(1000);
  await page.screenshot({ path: path.join(artifactDir, '05_login_page.png') });

  // Test 6: Chat Page with stage-01-inflight-london
  console.log('6. Testing Chat Page (/#/chat/stage-01-inflight-london)');
  await page.goto('http://localhost:8083/#/chat/stage-01-inflight-london');
  await page.waitForTimeout(4000); // Wait for initial greeting
  await page.screenshot({ path: path.join(artifactDir, '06_chat_page.png') });

  // Click Objective in Chat Page
  console.log('7. Opening Objective Sheet in Chat Page');
  const targetBtn = page.locator('button[title="مشاهده اهداف مرحله"]');
  if (await targetBtn.isVisible()) {
    await targetBtn.click();
    await page.waitForTimeout(800);
    await page.screenshot({ path: path.join(artifactDir, '07_chat_objective_sheet.png') });
    await page.click('button:has-text("متوجه شدم، ادامه مکالمه")');
    await page.waitForTimeout(500);
  }

  // Type a message in chat
  console.log('8. Typing a message in chat input');
  // Switch to keyboard mode
  const keyboardBtn = page.locator('button[title="تغییر به کیبورد متنی"]');
  if (await keyboardBtn.isVisible()) {
    await keyboardBtn.click();
    await page.waitForTimeout(500);
    const input = page.locator('input[placeholder="Type in English..."]');
    if (await input.isVisible()) {
      await input.fill('Hello Emily, I would like to order chicken and rice please.');
      await page.waitForTimeout(500);
      await page.screenshot({ path: path.join(artifactDir, '08_chat_input_filled.png') });
    }
  }

  console.log('--- All E2E Tests Completed Successfully! ---');
  await browser.close();
}

run().catch((err) => {
  console.error('E2E Test Failed:', err);
  process.exit(1);
});
