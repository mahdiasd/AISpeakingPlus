const { chromium } = require('playwright');
const path = require('path');

async function run() {
  const artifactDir = '/Users/mahdi/.gemini/antigravity/brain/71b8dbb1-a0a7-4fa2-9b11-d23008274287';
  const browser = await chromium.launch({
    headless: true,
    executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
  });
  const context = await browser.newContext({
    viewport: { width: 414, height: 896 },
    deviceScaleFactor: 2,
  });
  const page = await context.newPage();

  // Clear any existing token first to test guest state
  await page.goto('http://localhost:8083/#/profile');
  await page.evaluate(() => localStorage.removeItem('token'));
  await page.reload();
  await page.waitForTimeout(1000);

  console.log('1. Captured guest profile');
  await page.screenshot({ path: path.join(artifactDir, 'profile_guest.png') });

  // Click login in guest banner
  console.log('2. Clicking login in guest banner');
  await page.click('button:has-text("ورود با شماره موبایل")');
  await page.waitForTimeout(800);

  // Click bypass test button
  console.log('3. Clicking 1-click test bypass login button');
  await page.click('button:has-text("ورود آنی با حساب تستی (Bypass)")');
  await page.waitForTimeout(2500);

  console.log('4. Captured logged-in profile');
  await page.screenshot({ path: path.join(artifactDir, 'profile_logged_in.png') });

  // Click Avatar to open AvatarPickerModal
  console.log('5. Opening Avatar Picker');
  await page.click('button[title="تغییر تصویر پروفایل"]');
  await page.waitForTimeout(800);
  await page.screenshot({ path: path.join(artifactDir, 'profile_avatar_picker.png') });

  // Close avatar picker
  await page.click('button:has-text("انصراف")');
  await page.waitForTimeout(500);

  // Click Edit Name
  console.log('6. Opening Edit Name Modal');
  await page.click('button[title="ویرایش نام"]');
  await page.waitForTimeout(800);
  await page.screenshot({ path: path.join(artifactDir, 'profile_edit_name.png') });

  // Close edit name
  await page.click('button:has-text("انصراف")');
  await page.waitForTimeout(500);

  // Click Level Picker
  console.log('7. Opening Level Picker Modal');
  await page.click('button[title="تغییر سطح زبان انگلیسی"]');
  await page.waitForTimeout(800);
  await page.screenshot({ path: path.join(artifactDir, 'profile_level_picker.png') });

  // Close level picker by clicking its close X
  await page.locator('div:has-text("سطح زبان انگلیسی (CEFR)") + button').click();
  await page.waitForTimeout(500);

  // Click Support & About
  console.log('8. Opening Support Modal');
  await page.click('button:has-text("ارتباط با پشتیبانی و راهنما")');
  await page.waitForTimeout(800);
  await page.screenshot({ path: path.join(artifactDir, 'profile_support_modal.png') });

  // Close support modal
  await page.click('button:has-text("بستن پنجره")');
  await page.waitForTimeout(500);

  // Click Back Button in header
  console.log('9. Testing Back Button navigation');
  await page.click('button:has-text("بازگشت")');
  await page.waitForTimeout(1000);
  const currentUrl = page.url();
  console.log('Navigated back to:', currentUrl);
  await page.screenshot({ path: path.join(artifactDir, 'profile_nav_back_home.png') });

  await browser.close();
  console.log('SUCCESS: All profile tests completed successfully!');
}

run().catch((err) => {
  console.error('Test failed:', err);
  process.exit(1);
});
