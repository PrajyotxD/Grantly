# Contributing to Grantly SDK

Thank you for your interest in contributing to Grantly! We welcome contributions from the community and are pleased to have you join us.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Pull Request Process](#pull-request-process)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)
- [Issue Reporting](#issue-reporting)
- [Feature Requests](#feature-requests)
- [Community](#community)

## Code of Conduct

This project and everyone participating in it is governed by our Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

### Our Standards

- Use welcoming and inclusive language
- Be respectful of differing viewpoints and experiences
- Gracefully accept constructive criticism
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites

- Android Studio Arctic Fox (2020.3.1) or later
- JDK 11 or later
- Android SDK with API level 23+ (Android 6.0)
- Git

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/Grantly.git
   cd Grantly
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/PrajyotxD/Grantly.git
   ```

## Development Setup

1. **Open the project in Android Studio**
   ```bash
   # Open Android Studio and select "Open an existing project"
   # Navigate to the cloned Grantly directory
   ```

2. **Sync the project**
   - Let Android Studio sync the project and download dependencies
   - Ensure all Gradle sync issues are resolved

3. **Run tests to verify setup**
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

## How to Contribute

### Types of Contributions

We welcome several types of contributions:

- **Bug fixes** - Fix issues in the existing codebase
- **Feature enhancements** - Add new functionality to existing features
- **New features** - Implement entirely new capabilities
- **Documentation** - Improve or add documentation
- **Tests** - Add or improve test coverage
- **Performance improvements** - Optimize existing code
- **Code quality** - Refactor code for better maintainability

### Before You Start

1. **Check existing issues** - Look for existing issues or discussions about your intended contribution
2. **Create an issue** - If no issue exists, create one to discuss your proposed changes
3. **Get feedback** - Wait for maintainer feedback before starting significant work
4. **Assign yourself** - Comment on the issue to let others know you're working on it

## Pull Request Process

### 1. Create a Branch

Create a new branch for your work:
```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/issue-number-description
```

### 2. Make Your Changes

- Write clean, readable code following our coding standards
- Add tests for new functionality
- Update documentation as needed
- Ensure all tests pass

### 3. Commit Your Changes

Use clear, descriptive commit messages:
```bash
git add .
git commit -m "Add support for custom permission icons

- Implement PermissionIconProvider interface
- Add default icon resources for common permissions
- Update GrantlyUtils to support custom icons
- Add unit tests for icon functionality

Fixes #123"
```

### 4. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a pull request on GitHub with:
- Clear title describing the change
- Detailed description of what was changed and why
- Reference to related issues
- Screenshots if UI changes are involved

### 5. Code Review Process

- Maintainers will review your PR
- Address any feedback or requested changes
- Keep your branch up to date with main:
  ```bash
  git fetch upstream
  git rebase upstream/main
  ```

## Coding Standards

### Java Code Style

- **Indentation**: 4 spaces (no tabs)
- **Line length**: Maximum 120 characters
- **Naming conventions**:
  - Classes: PascalCase (`PermissionManager`)
  - Methods: camelCase (`requestPermissions`)
  - Constants: UPPER_SNAKE_CASE (`DEFAULT_TIMEOUT`)
  - Variables: camelCase (`permissionList`)

### Code Organization

- **Package structure**: Follow the existing package hierarchy
- **Class organization**:
  1. Static fields
  2. Instance fields
  3. Constructors
  4. Public methods
  5. Private methods
  6. Inner classes

### Documentation

- **Public APIs**: Must have comprehensive Javadoc
- **Complex logic**: Add inline comments explaining the approach
- **TODOs**: Use `// TODO: description` format

### Example Code Style

```java
/**
 * Manages permission requests and handles Android version differences.
 * 
 * <p>This class provides a unified interface for requesting permissions
 * across different Android API levels, handling special permissions,
 * and managing permission state.</p>
 * 
 * @since 1.0.0
 */
public class PermissionManager {
    
    private static final String TAG = "PermissionManager";
    private static final int DEFAULT_REQUEST_CODE = 1001;
    
    private final Context context;
    private final PermissionChecker permissionChecker;
    
    /**
     * Creates a new PermissionManager instance.
     * 
     * @param context Application context
     * @param permissionChecker Permission checker implementation
     * @throws IllegalArgumentException if context is null
     */
    public PermissionManager(@NonNull Context context, 
                           @NonNull PermissionChecker permissionChecker) {
        this.context = Objects.requireNonNull(context, "Context cannot be null");
        this.permissionChecker = Objects.requireNonNull(permissionChecker, 
            "PermissionChecker cannot be null");
    }
    
    /**
     * Requests the specified permissions.
     * 
     * @param permissions Array of permissions to request
     * @param callback Callback to handle results
     */
    public void requestPermissions(@NonNull String[] permissions, 
                                 @NonNull PermissionCallback callback) {
        // Implementation here
    }
}
```

## Testing Guidelines

### Test Structure

- **Unit tests**: `src/test/java/` - Test individual components in isolation
- **Integration tests**: `src/androidTest/java/` - Test component interactions
- **UI tests**: Test user-facing functionality

### Writing Tests

1. **Test naming**: Use descriptive names that explain what is being tested
   ```java
   @Test
   public void requestPermissions_withValidPermissions_shouldInvokeCallback() {
       // Test implementation
   }
   ```

2. **Test structure**: Follow Arrange-Act-Assert pattern
   ```java
   @Test
   public void testMethod() {
       // Arrange
       String[] permissions = {Manifest.permission.CAMERA};
       PermissionCallback callback = mock(PermissionCallback.class);
       
       // Act
       permissionManager.requestPermissions(permissions, callback);
       
       // Assert
       verify(callback).onPermissionGranted(permissions);
   }
   ```

3. **Mock external dependencies**: Use Mockito for mocking Android framework components

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "PermissionManagerTest"

# Generate test coverage report
./gradlew jacocoTestReport
```

### Test Coverage

- Aim for at least 80% code coverage
- Focus on testing public APIs and critical paths
- Include edge cases and error conditions

## Documentation

### Types of Documentation

1. **API Documentation**: Comprehensive Javadoc for all public APIs
2. **User Guide**: README.md with usage examples
3. **Developer Guide**: This CONTRIBUTING.md file
4. **Code Comments**: Inline documentation for complex logic

### Documentation Standards

- **Javadoc**: Required for all public classes, methods, and fields
- **Examples**: Include code examples in documentation
- **Links**: Reference related classes and methods using `{@link}`
- **Since tags**: Use `@since` tags for version information

### Building Documentation

```bash
# Generate Javadoc
./gradlew javadoc

# View generated docs
open Grantly/build/docs/javadoc/index.html
```

## Issue Reporting

### Before Creating an Issue

1. **Search existing issues** - Check if the issue already exists
2. **Check documentation** - Ensure it's not a usage question
3. **Test with latest version** - Verify the issue exists in the current version

### Bug Reports

Include the following information:

- **Android version** and **API level**
- **Device information** (manufacturer, model)
- **Grantly SDK version**
- **Steps to reproduce** the issue
- **Expected behavior** vs **actual behavior**
- **Code sample** demonstrating the issue
- **Stack trace** if applicable
- **Screenshots** if relevant

### Bug Report Template

```markdown
**Environment:**
- Android Version: 
- API Level: 
- Device: 
- Grantly Version: 

**Description:**
Brief description of the issue

**Steps to Reproduce:**
1. Step one
2. Step two
3. Step three

**Expected Behavior:**
What should happen

**Actual Behavior:**
What actually happens

**Code Sample:**
```java
// Minimal code sample that reproduces the issue
```

**Additional Information:**
Any other relevant information
```

## Feature Requests

### Before Requesting a Feature

1. **Check existing requests** - Look for similar feature requests
2. **Consider alternatives** - Think about whether existing features can solve your need
3. **Evaluate scope** - Consider if the feature fits the project's goals

### Feature Request Template

```markdown
**Feature Description:**
Clear description of the proposed feature

**Use Case:**
Explain why this feature would be useful

**Proposed API:**
```java
// Example of how the feature might be used
```

**Alternatives Considered:**
Other approaches you've considered

**Additional Context:**
Any other relevant information
```

## Community

### Communication Channels

- **GitHub Issues** - Bug reports and feature requests
- **GitHub Discussions** - General questions and community discussions
- **Pull Requests** - Code contributions and reviews

### Getting Help

1. **Check documentation** - README.md and API documentation
2. **Search issues** - Look for similar questions
3. **Create discussion** - Use GitHub Discussions for questions
4. **Be specific** - Provide context and code examples

### Code Review Guidelines

#### For Contributors

- **Be responsive** - Address feedback promptly
- **Be open** - Accept constructive criticism
- **Ask questions** - If feedback is unclear, ask for clarification
- **Test thoroughly** - Ensure your changes work as expected

#### For Reviewers

- **Be constructive** - Provide helpful, actionable feedback
- **Be specific** - Point out exact issues and suggest improvements
- **Be timely** - Review PRs in a reasonable timeframe
- **Be encouraging** - Recognize good work and effort

### Recognition

Contributors will be recognized in:
- **CONTRIBUTORS.md** file
- **Release notes** for significant contributions
- **GitHub contributors** section

## Development Workflow

### Branch Strategy

- **main** - Stable release branch
- **develop** - Integration branch for features
- **feature/*** - Feature development branches
- **fix/*** - Bug fix branches
- **release/*** - Release preparation branches

### Release Process

1. **Feature freeze** - No new features for upcoming release
2. **Testing** - Comprehensive testing on multiple devices/API levels
3. **Documentation** - Update documentation and changelog
4. **Release candidate** - Create RC for final testing
5. **Release** - Tag and publish release

### Versioning

We follow [Semantic Versioning](https://semver.org/):
- **MAJOR** - Incompatible API changes
- **MINOR** - New functionality (backward compatible)
- **PATCH** - Bug fixes (backward compatible)

## Questions?

If you have questions about contributing, please:

1. Check this guide first
2. Search existing issues and discussions
3. Create a new discussion on GitHub
4. Reach out to maintainers if needed

Thank you for contributing to Grantly! 🎉