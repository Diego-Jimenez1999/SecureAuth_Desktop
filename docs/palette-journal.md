# Palette UX Journal 🎨

This journal documents critical, high-value UX and accessibility (a11y) learnings discovered while refining **SecureAuth_Desktop**.

## 2026-08-09 - Native Swing Form Submission and High-Impact Dialog Accessibility
**Learning:**
- In Swing dialog forms (such as `EmployeeRegistrationDialog`), rather than manually adding KeyListeners or ActionListeners to multiple text fields to catch the Enter key for submission, utilizing the native Swing root pane default button:
  ```java
  getRootPane().setDefaultButton(registrarButton);
  ```
  is the most elegant and idiomatic way. It automatically maps the Enter key across all form input fields safely without interfering with text editing.
- Associating `JLabel` components with their target inputs via `label.setLabelFor(input)` coupled with setting the accessible name `getAccessibleContext().setAccessibleName("Name")` is the standard for Swing screen-reader accessibility.
- Password fields (`JPasswordField`) already inherently announce themselves as security inputs, so setting a redundant accessible name on them should be avoided.

**Action:**
- Always prioritize `getRootPane().setDefaultButton(...)` for standard form dialogues.
- Link input labels with their targets and provide descriptive accessible names, taking care to keep password fields uncluttered by redundant announcements.
