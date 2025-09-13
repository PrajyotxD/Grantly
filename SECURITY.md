# Security Policy

## Supported Versions

We actively support the following versions of Grantly SDK with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |

## Reporting a Vulnerability

The Grantly team takes security bugs seriously. We appreciate your efforts to responsibly disclose your findings, and will make every effort to acknowledge your contributions.

### How to Report a Security Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

Instead, please report them using one of the following methods:

1. **GitHub Security Advisories** (Preferred)
   - Go to the [Security tab](https://github.com/PrajyotxD/Grantly/security) of our repository
   - Click "Report a vulnerability"
   - Fill out the security advisory form

2. **Private Issue**
   - Create a new issue and mark it as confidential
   - Include "SECURITY" in the title
   - Provide detailed information about the vulnerability

### What to Include in Your Report

Please include the following information in your security report:

- **Type of issue** (e.g., buffer overflow, SQL injection, cross-site scripting, etc.)
- **Full paths of source file(s)** related to the manifestation of the issue
- **The location of the affected source code** (tag/branch/commit or direct URL)
- **Any special configuration required** to reproduce the issue
- **Step-by-step instructions to reproduce the issue**
- **Proof-of-concept or exploit code** (if possible)
- **Impact of the issue**, including how an attacker might exploit the issue

### Response Timeline

- **Initial Response**: We will acknowledge receipt of your vulnerability report within 48 hours
- **Status Updates**: We will send you regular updates about our progress, at least every 7 days
- **Resolution**: We aim to resolve critical vulnerabilities within 30 days of initial report

### What to Expect

After you submit a report, here's what will happen:

1. **Acknowledgment**: We'll confirm receipt and begin investigating
2. **Investigation**: We'll work to reproduce and understand the vulnerability
3. **Fix Development**: We'll develop and test a fix
4. **Disclosure**: We'll coordinate with you on public disclosure timing
5. **Release**: We'll release the security fix and publish a security advisory

## Security Best Practices for Users

### For App Developers Using Grantly

1. **Keep Updated**: Always use the latest version of Grantly SDK
2. **Validate Permissions**: Only request permissions your app actually needs
3. **Handle Denials Gracefully**: Implement proper fallbacks when permissions are denied
4. **Secure Configuration**: Don't expose sensitive configuration in logs or debug builds
5. **Test Thoroughly**: Test permission flows on different Android versions and devices

### For SDK Configuration

1. **Disable Logging in Production**: Set `setLoggingEnabled(false)` in release builds
2. **Validate Custom Providers**: Ensure custom UI providers don't leak sensitive information
3. **ProGuard Rules**: Use the provided ProGuard rules to protect against reverse engineering
4. **Secure Defaults**: Use secure default configurations for production apps

### Common Security Considerations

1. **Permission Scope**: Only request the minimum permissions required
2. **Data Handling**: Be careful with permission-protected data
3. **UI Security**: Ensure custom dialogs don't expose sensitive information
4. **Logging**: Avoid logging sensitive permission-related data
5. **Testing**: Test permission flows with security in mind

## Security Features

### Built-in Security Measures

1. **Input Validation**: All public APIs validate input parameters
2. **Permission Validation**: Automatic validation of declared permissions
3. **State Management**: Secure handling of permission request state
4. **Error Handling**: Secure error messages that don't leak sensitive information
5. **Thread Safety**: Thread-safe implementation to prevent race conditions

### Privacy Protection

1. **No Data Collection**: Grantly SDK doesn't collect or transmit user data
2. **Local Processing**: All permission logic runs locally on the device
3. **No Network Access**: SDK doesn't require or use network permissions
4. **Minimal Permissions**: SDK only requires permissions necessary for its functionality

## Vulnerability Disclosure Policy

### Coordinated Disclosure

We follow a coordinated disclosure policy:

1. **Private Reporting**: Security issues should be reported privately first
2. **Investigation Period**: We investigate and develop fixes before public disclosure
3. **Coordinated Release**: We coordinate with reporters on disclosure timing
4. **Public Advisory**: We publish security advisories after fixes are available

### Recognition

We believe in recognizing security researchers who help improve our security:

- **Security Hall of Fame**: We maintain a list of security contributors
- **CVE Assignment**: We work with reporters to assign CVEs when appropriate
- **Public Recognition**: We acknowledge contributions in security advisories (with permission)

## Security Updates

### How We Communicate Security Updates

1. **GitHub Security Advisories**: Primary channel for security notifications
2. **Release Notes**: Security fixes are documented in CHANGELOG.md
3. **GitHub Releases**: Security releases are tagged and published
4. **Documentation Updates**: Security-related documentation is updated as needed

### Severity Levels

We classify security vulnerabilities using the following severity levels:

- **Critical**: Immediate threat to user security or privacy
- **High**: Significant security impact with likely exploitation
- **Medium**: Moderate security impact with possible exploitation
- **Low**: Minor security impact with unlikely exploitation

## Contact Information

For security-related questions or concerns:

- **Security Issues**: Use GitHub Security Advisories (preferred)
- **General Security Questions**: Create a GitHub Discussion
- **Documentation Issues**: Create a regular GitHub issue

## Legal

This security policy is subject to our project's license terms. By reporting security vulnerabilities, you agree to our coordinated disclosure policy and responsible disclosure practices.