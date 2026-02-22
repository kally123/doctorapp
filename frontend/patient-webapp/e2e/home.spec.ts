import { test, expect } from '@playwright/test';

test.describe('Patient Web App - Home Page', () => {
  test('should load the home page', async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle(/Healthcare/i);
  });

  test('should have navigation menu', async ({ page }) => {
    await page.goto('/');
    const nav = page.locator('nav');
    await expect(nav).toBeVisible();
  });
});

