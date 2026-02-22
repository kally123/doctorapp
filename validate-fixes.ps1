#!/usr/bin/env pwsh
# Validation script to check if all GitHub CI fixes are in place

Write-Host "`n================================" -ForegroundColor Cyan
Write-Host "GitHub Build Fixes Validator" -ForegroundColor Cyan
Write-Host "================================`n" -ForegroundColor Cyan

$allGood = $true

# Check 1: Frontend package.json scripts
Write-Host "Checking Frontend Configurations..." -ForegroundColor Yellow

$patientPackageJson = Get-Content "frontend/patient-webapp/package.json" -Raw | ConvertFrom-Json
if ($patientPackageJson.scripts.'type-check') {
    Write-Host "  ✓ Patient Webapp has type-check script" -ForegroundColor Green
} else {
    Write-Host "  ✗ Patient Webapp missing type-check script" -ForegroundColor Red
    $allGood = $false
}

$doctorPackageJson = Get-Content "frontend/doctor-dashboard/package.json" -Raw | ConvertFrom-Json
if ($doctorPackageJson.scripts.'type-check') {
    Write-Host "  ✓ Doctor Dashboard has type-check script" -ForegroundColor Green
} else {
    Write-Host "  ✗ Doctor Dashboard missing type-check script" -ForegroundColor Red
    $allGood = $false
}

# Check 2: Frontend test configs
Write-Host "`nChecking Frontend Test Configurations..." -ForegroundColor Yellow

if (Test-Path "frontend/patient-webapp/jest.config.js") {
    Write-Host "  ✓ Patient Webapp Jest config exists" -ForegroundColor Green
} else {
    Write-Host "  ✗ Patient Webapp Jest config missing" -ForegroundColor Red
    $allGood = $false
}

if (Test-Path "frontend/patient-webapp/jest.setup.js") {
    Write-Host "  ✓ Patient Webapp Jest setup exists" -ForegroundColor Green
} else {
    Write-Host "  ✗ Patient Webapp Jest setup missing" -ForegroundColor Red
    $allGood = $false
}

if (Test-Path "frontend/doctor-dashboard/src/test/setup.ts") {
    Write-Host "  ✓ Doctor Dashboard test setup exists" -ForegroundColor Green
} else {
    Write-Host "  ✗ Doctor Dashboard test setup missing" -ForegroundColor Red
    $allGood = $false
}

# Check 3: Backend test files
Write-Host "`nChecking Backend Test Files..." -ForegroundColor Yellow

$services = Get-ChildItem -Path "backend/*-service" -Directory
$servicesWithTests = 0
$servicesMissingTests = @()

foreach ($service in $services) {
    $serviceName = $service.Name
    $testDir = "$($service.FullName)\src\test"

    if (Test-Path $testDir) {
        $testFiles = Get-ChildItem -Path $testDir -Recurse -Filter "*Test*.java"
        if ($testFiles.Count -gt 0) {
            $servicesWithTests++
        } else {
            $servicesMissingTests += $serviceName
        }
    } else {
        $servicesMissingTests += $serviceName
    }
}

Write-Host "  ✓ $servicesWithTests / $($services.Count) services have tests" -ForegroundColor $(if ($servicesWithTests -eq $services.Count) { "Green" } else { "Yellow" })

if ($servicesMissingTests.Count -gt 0) {
    Write-Host "    Missing: $($servicesMissingTests -join ', ')" -ForegroundColor Red
    $allGood = $false
}

# Check 4: Backend Maven plugins
Write-Host "`nChecking Backend Maven Configuration..." -ForegroundColor Yellow

$pomContent = Get-Content "backend/pom.xml" -Raw

if ($pomContent -match "maven-surefire-plugin") {
    Write-Host "  ✓ Maven Surefire plugin configured" -ForegroundColor Green
} else {
    Write-Host "  ✗ Maven Surefire plugin missing" -ForegroundColor Red
    $allGood = $false
}

if ($pomContent -match "maven-failsafe-plugin") {
    Write-Host "  ✓ Maven Failsafe plugin configured" -ForegroundColor Green
} else {
    Write-Host "  ✗ Maven Failsafe plugin missing" -ForegroundColor Red
    $allGood = $false
}

# Check 5: API Gateway test
Write-Host "`nChecking API Gateway Test..." -ForegroundColor Yellow
if (Test-Path "backend/api-gateway/src/test") {
    Write-Host "  ✓ API Gateway has test directory" -ForegroundColor Green
} else {
    Write-Host "  ✗ API Gateway missing test directory" -ForegroundColor Red
    $allGood = $false
}

# Summary
Write-Host "`n================================" -ForegroundColor Cyan
if ($allGood) {
    Write-Host "✅ ALL CHECKS PASSED!" -ForegroundColor Green
    Write-Host "`nYou're ready to commit and push!" -ForegroundColor Green
    Write-Host "`nRun these commands:" -ForegroundColor Yellow
    Write-Host "  git add ." -ForegroundColor White
    Write-Host "  git commit -m `"Fix GitHub CI builds: Add missing tests and configs`"" -ForegroundColor White
    Write-Host "  git push origin main" -ForegroundColor White
} else {
    Write-Host "❌ SOME CHECKS FAILED" -ForegroundColor Red
    Write-Host "`nPlease review the errors above." -ForegroundColor Yellow
}
Write-Host "================================`n" -ForegroundColor Cyan

