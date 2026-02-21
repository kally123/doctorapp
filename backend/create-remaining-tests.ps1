# Script to create test structure for remaining backend services
# Run this from the backend directory

$services = @(
    @{name='consultation-service'; package='consultation'},
    @{name='content-service'; package='content'},
    @{name='ehr-service'; package='ehr'},
    @{name='notification-service'; package='notification'},
    @{name='order-service'; package='order'},
    @{name='payment-service'; package='payment'},
    @{name='prescription-service'; package='prescription'},
    @{name='review-service'; package='review'}
)

foreach ($service in $services) {
    $serviceName = $service.name
    $packageName = $service.package
    $className = (Get-Culture).TextInfo.ToTitleCase($packageName) + "ServiceApplicationTests"

    Write-Host "Creating test structure for $serviceName..." -ForegroundColor Green

    # Create directories
    $testJavaDir = ".\$serviceName\src\test\java\com\healthapp\$packageName"
    $testResourcesDir = ".\$serviceName\src\test\resources"

    New-Item -ItemType Directory -Force -Path $testJavaDir | Out-Null
    New-Item -ItemType Directory -Force -Path $testResourcesDir | Out-Null

    # Create test class
    $testClass = @"
package com.healthapp.$packageName;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class $className {

    @Test
    void contextLoads() {
        // Basic smoke test to ensure application context loads
    }
}
"@

    $testClassPath = "$testJavaDir\$className.java"
    Set-Content -Path $testClassPath -Value $testClass -Encoding UTF8

    # Create test configuration
    $testConfig = @"
spring:
  application:
    name: $serviceName-test

  r2dbc:
    url: r2dbc:postgresql://localhost:5432/test_db
    username: test_user
    password: test_password

  data:
    redis:
      host: localhost
      port: 6379

  flyway:
    enabled: false

logging:
  level:
    root: WARN
    com.healthapp: INFO
"@

    $testConfigPath = "$testResourcesDir\application-test.yml"
    Set-Content -Path $testConfigPath -Value $testConfig -Encoding UTF8

    Write-Host "  ✓ Created test class: $testClassPath" -ForegroundColor Gray
    Write-Host "  ✓ Created test config: $testConfigPath" -ForegroundColor Gray
}

Write-Host "`nAll test structures created successfully!" -ForegroundColor Green
Write-Host "Run 'mvn clean test' to verify the tests work." -ForegroundColor Yellow

