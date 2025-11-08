# SmartWatts Testing Guide

This document provides comprehensive information about the SmartWatts testing infrastructure and how to run tests.

## Overview

SmartWatts has a comprehensive test suite covering:
- **Frontend Unit Tests**: 22 test files (Jest + React Testing Library)
- **Backend Controller Tests**: 16 test files (JUnit 5 + MockMvc)
- **Backend Service Tests**: 5 test files (JUnit 5 + Mockito)
- **E2E Edge Case Tests**: 7 test files (Playwright)
- **E2E Visual Regression Tests**: 6 test files (Playwright)
- **E2E Load Testing**: 5 test files (Playwright)

**Total: 61 test files**

## Test Coverage Targets

- **Frontend Unit Tests**: 100% coverage (branches, functions, lines, statements)
- **Backend Unit Tests**: 100% minimum coverage
- **E2E Tests**: All critical user flows and edge cases

## Running Tests

### Frontend Tests

#### Unit Tests
```bash
cd frontend

# Run all unit tests
npm run test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage

# Run tests in CI mode
npm run test:ci
```

#### E2E Tests
```bash
cd frontend

# Run all E2E tests
npm run test:e2e

# Run E2E tests with UI
npm run test:e2e:ui

# Run E2E tests in headed mode
npm run test:e2e:headed

# Run specific E2E test suites
npm run test:e2e:visual    # Visual regression tests
npm run test:e2e:load      # Load testing
npm run test:e2e:edge      # Edge case tests
```

#### All Frontend Tests
```bash
cd frontend

# Run all frontend tests
./scripts/test-all.sh

# Run with coverage
./scripts/test-all.sh --coverage

# Run only unit tests
./scripts/test-all.sh --unit-only

# Run only E2E tests
./scripts/test-all.sh --e2e-only

# Include visual regression
./scripts/test-all.sh --visual

# Include load testing
./scripts/test-all.sh --load
```

### Backend Tests

#### Individual Service Tests
```bash
cd backend

# Run tests for a specific service
cd user-service
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# View coverage report
open build/reports/jacoco/html/index.html
```

#### All Backend Tests
```bash
cd backend

# Run tests for all services
./scripts/test-all.sh

# Run with coverage
./scripts/test-all.sh --coverage

# Run tests for specific service
./scripts/test-all.sh --service user-service
```

### Complete Test Suite

```bash
# From project root

# Run all tests (frontend + backend)
./scripts/test-all.sh

# Run with coverage
./scripts/test-all.sh --coverage

# Run only frontend
./scripts/test-all.sh --frontend-only

# Run only backend
./scripts/test-all.sh --backend-only
```

## Test Structure

### Frontend Unit Tests
```
frontend/
├── __tests__/
│   ├── components/
│   │   ├── dashboard/          # Dashboard widget tests
│   │   ├── modals/             # Modal component tests
│   │   └── ui/                 # UI component tests
│   ├── hooks/                  # Hook tests
│   ├── contexts/               # Context tests
│   └── utils/                  # Utility tests
```

### Backend Tests
```
backend/
├── user-service/
│   └── src/test/java/
│       ├── controller/         # Controller tests
│       └── service/            # Service tests
├── device-service/
│   └── src/test/java/
│       ├── controller/
│       └── service/
└── ...
```

### E2E Tests
```
frontend/e2e/
├── edge-cases/                 # Edge case scenarios
│   ├── error-handling.spec.ts
│   ├── data-boundaries.spec.ts
│   ├── concurrent-operations.spec.ts
│   ├── session-expiry.spec.ts
│   ├── permissions.spec.ts
│   ├── invalid-input.spec.ts
│   └── timeouts.spec.ts
├── visual-regression/          # Visual regression tests
│   ├── dashboard.spec.ts
│   ├── device-management.spec.ts
│   ├── forms.spec.ts
│   ├── modals.spec.ts
│   ├── responsive.spec.ts
│   └── themes.spec.ts
└── load-testing/               # Load testing
    ├── api-endpoints.spec.ts
    ├── concurrent-users.spec.ts
    ├── page-load.spec.ts
    ├── memory-leaks.spec.ts
    └── database-queries.spec.ts
```

## Test Configuration

### Jest Configuration
- **Location**: `frontend/jest.config.js`
- **Coverage Threshold**: 100% for all metrics
- **Test Environment**: jsdom
- **Setup File**: `frontend/jest.setup.js`

### Playwright Configuration
- **Location**: `frontend/playwright.config.ts`
- **Base URL**: `http://localhost:3000`
- **Browsers**: Chromium, Firefox, WebKit
- **Mobile Viewports**: Pixel 5, iPhone 12

### Gradle/JaCoCo Configuration
- **Coverage Tool**: JaCoCo 0.8.11
- **Minimum Coverage**: 100%
- **Exclusions**: DTOs, Models, Entities, Config, Exceptions, Application classes
- **Reports**: HTML, XML, CSV

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Tests

on: [push, pull_request]

jobs:
  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
      - run: cd frontend && npm install
      - run: cd frontend && npm run test:ci
      - run: cd frontend && npm run test:e2e

  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - run: cd backend && ./scripts/test-all.sh --coverage
```

## Coverage Reports

### Frontend Coverage
```bash
cd frontend
npm run test:coverage
# Open: coverage/lcov-report/index.html
```

### Backend Coverage
```bash
cd backend/user-service
./gradlew test jacocoTestReport
# Open: build/reports/jacoco/html/index.html
```

## Best Practices

1. **Write tests before fixing bugs** - Create a failing test first
2. **Keep tests isolated** - Each test should be independent
3. **Use descriptive test names** - Follow pattern: `should [expected behavior] when [condition]`
4. **Mock external dependencies** - Don't make real API calls in unit tests
5. **Test edge cases** - Include boundary conditions and error scenarios
6. **Maintain test coverage** - Keep coverage above thresholds
7. **Run tests frequently** - Run tests before committing code

## Troubleshooting

### Frontend Tests
- **Issue**: Tests fail with "Cannot find module"
  - **Solution**: Run `npm install` to ensure all dependencies are installed

- **Issue**: E2E tests fail with timeout
  - **Solution**: Ensure dev server is running: `npm run dev`

### Backend Tests
- **Issue**: Tests fail with database connection errors
  - **Solution**: Ensure PostgreSQL is running and configured correctly

- **Issue**: JaCoCo reports not generated
  - **Solution**: Run `./gradlew clean test jacocoTestReport`

## Additional Resources

- [Jest Documentation](https://jestjs.io/)
- [React Testing Library](https://testing-library.com/react)
- [Playwright Documentation](https://playwright.dev/)
- [JUnit 5 Documentation](https://junit.org/junit5/)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/)

