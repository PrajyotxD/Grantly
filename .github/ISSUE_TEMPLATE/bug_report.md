---
name: Bug report
about: Create a report to help us improve
title: '[BUG] '
labels: 'bug'
assignees: ''

---

**Describe the bug**
A clear and concise description of what the bug is.

**Environment**
- Android Version: [e.g. Android 12]
- API Level: [e.g. 31]
- Device: [e.g. Samsung Galaxy S21, Pixel 6]
- Grantly Version: [e.g. 1.0.0]
- Build Tools Version: [e.g. 34.0.0]

**To Reproduce**
Steps to reproduce the behavior:
1. Go to '...'
2. Click on '....'
3. Scroll down to '....'
4. See error

**Expected behavior**
A clear and concise description of what you expected to happen.

**Actual behavior**
A clear and concise description of what actually happened.

**Code Sample**
```java
// Please provide a minimal code sample that reproduces the issue
Grantly.requestPermissions(this)
    .permissions(Manifest.permission.CAMERA)
    .setCallbacks(new GrantlyCallback() {
        // Your callback implementation
    })
    .execute();
```

**Stack Trace**
If applicable, add the full stack trace:
```
// Paste your stack trace here
```

**Screenshots**
If applicable, add screenshots to help explain your problem.

**Additional context**
Add any other context about the problem here.

**Checklist**
- [ ] I have searched existing issues to ensure this is not a duplicate
- [ ] I have tested with the latest version of Grantly
- [ ] I have included all required information above
- [ ] I have provided a minimal code sample that reproduces the issue