# Contributing to SmartWatts

Thank you for your interest in contributing to SmartWatts! This document provides guidelines and information for contributors.

## 🤝 How to Contribute

### 1. Fork the Repository
1. Go to [SmartWatts Repository](https://github.com/smartwatts/SmartWatts)
2. Click the "Fork" button in the top-right corner
3. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SmartWatts.git
   cd SmartWatts
   ```

### 2. Set Up Development Environment

#### Backend Development
```bash
# Install Java 17+
# Install Docker and Docker Compose

# Start services
docker-compose up -d

# Verify services are running
curl http://localhost:8080/actuator/health
```

#### Frontend Development
```bash
cd frontend
npm install
npm run dev
```

#### Edge Gateway Development
```bash
cd edge-gateway
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
python main.py
```

### 3. Create a Feature Branch
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/your-bug-fix
```

### 4. Make Your Changes
- Write clean, readable code
- Follow the coding standards (see below)
- Add tests for new functionality
- Update documentation as needed

### 5. Test Your Changes
```bash
# Backend tests
cd backend/service-name
./gradlew test

# Frontend tests
cd frontend
npm run test

# Edge Gateway tests
cd edge-gateway
python -m pytest
```

### 6. Commit Your Changes
```bash
git add .
git commit -m "feat: add new energy monitoring feature"
# or
git commit -m "fix: resolve database connection issue"
```

### 7. Push and Create Pull Request
```bash
git push origin feature/your-feature-name
```
Then create a Pull Request on GitHub.

## 📋 Coding Standards

### Java/Spring Boot
- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Add Javadoc comments for public methods
- Keep methods small and focused
- Use dependency injection properly

### TypeScript/React
- Follow [Airbnb TypeScript Style Guide](https://github.com/airbnb/typescript)
- Use functional components with hooks
- Prefer TypeScript over JavaScript
- Use meaningful component and prop names
- Keep components small and focused

### Python/FastAPI
- Follow [PEP 8](https://www.python.org/dev/peps/pep-0008/)
- Use type hints for all functions
- Add docstrings for all public functions
- Use meaningful variable names
- Keep functions small and focused

## 🧪 Testing Guidelines

### Backend Testing
- Write unit tests for all service methods
- Write integration tests for API endpoints
- Aim for 80%+ code coverage
- Use Mockito for mocking dependencies

### Frontend Testing
- Write unit tests for React components
- Write integration tests for user flows
- Use React Testing Library
- Test accessibility features

### Edge Gateway Testing
- Write unit tests for all services
- Write integration tests for API endpoints
- Test with mock devices
- Use pytest for testing framework

## 📝 Commit Message Convention

We use [Conventional Commits](https://www.conventionalcommits.org/) for commit messages:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

### Examples
```
feat(energy): add real-time energy monitoring
fix(api): resolve database connection timeout
docs(readme): update installation instructions
style(frontend): format components with prettier
refactor(edge): optimize MQTT message handling
test(analytics): add unit tests for forecasting
chore(deps): update Spring Boot to 3.2.0
```

## 🐛 Bug Reports

When reporting bugs, please include:

1. **Description**: Clear description of the bug
2. **Steps to Reproduce**: Detailed steps to reproduce the issue
3. **Expected Behavior**: What should happen
4. **Actual Behavior**: What actually happens
5. **Environment**: OS, browser, version information
6. **Screenshots**: If applicable
7. **Logs**: Relevant error logs

### Bug Report Template
```markdown
## Bug Description
Brief description of the bug

## Steps to Reproduce
1. Go to '...'
2. Click on '...'
3. Scroll down to '...'
4. See error

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- OS: [e.g. Windows 10, macOS 12, Ubuntu 20.04]
- Browser: [e.g. Chrome 91, Firefox 89]
- Version: [e.g. 1.0.0]

## Screenshots
If applicable, add screenshots

## Additional Context
Any other context about the problem
```

## ✨ Feature Requests

When requesting features, please include:

1. **Description**: Clear description of the feature
2. **Use Case**: Why this feature is needed
3. **Proposed Solution**: How you think it should work
4. **Alternatives**: Other solutions you've considered
5. **Additional Context**: Any other relevant information

### Feature Request Template
```markdown
## Feature Description
Brief description of the feature

## Use Case
Why is this feature needed?

## Proposed Solution
How should this feature work?

## Alternatives
What other solutions have you considered?

## Additional Context
Any other relevant information
```

## 🔍 Code Review Process

### For Contributors
1. Ensure your code follows the coding standards
2. Add tests for new functionality
3. Update documentation as needed
4. Respond to review feedback promptly
5. Keep commits focused and atomic

### For Reviewers
1. Review code for correctness and quality
2. Check for security vulnerabilities
3. Ensure tests are adequate
4. Verify documentation is updated
5. Provide constructive feedback

## 🏗️ Architecture Guidelines

### Backend Services
- Follow microservices principles
- Use Spring Boot best practices
- Implement proper error handling
- Use appropriate design patterns
- Maintain service boundaries

### Frontend
- Use React best practices
- Implement proper state management
- Use TypeScript for type safety
- Follow component composition patterns
- Ensure accessibility

### Edge Gateway
- Use FastAPI best practices
- Implement proper error handling
- Use async/await patterns
- Follow Python best practices
- Ensure thread safety

## 📚 Documentation

### Code Documentation
- Add Javadoc for Java methods
- Add JSDoc for TypeScript functions
- Add docstrings for Python functions
- Use meaningful comments
- Keep documentation up to date

### API Documentation
- Use OpenAPI/Swagger annotations
- Provide clear examples
- Document all parameters and responses
- Include error codes and messages

### User Documentation
- Write clear, concise instructions
- Include screenshots when helpful
- Use consistent terminology
- Keep documentation current

## 🚀 Release Process

### Version Numbering
We use [Semantic Versioning](https://semver.org/):
- `MAJOR`: Incompatible API changes
- `MINOR`: New functionality (backward compatible)
- `PATCH`: Bug fixes (backward compatible)

### Release Checklist
- [ ] All tests pass
- [ ] Documentation updated
- [ ] Version numbers updated
- [ ] Changelog updated
- [ ] Security scan completed
- [ ] Performance tests passed

## 🤔 Questions?

If you have questions about contributing:

1. Check the [GitHub Discussions](https://github.com/smartwatts/SmartWatts/discussions)
2. Look at existing issues and pull requests
3. Contact the maintainers
4. Join our community chat

## 📄 License

By contributing to SmartWatts, you agree that your contributions will be licensed under the MIT License.

## 🙏 Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project documentation
- Community acknowledgments

Thank you for contributing to SmartWatts! 🎉
