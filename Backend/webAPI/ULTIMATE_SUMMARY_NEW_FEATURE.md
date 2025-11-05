# ULTIMATE SUMMARY — Single- CAPTCHA Registration / In-memory OTP Feature

Date: 2025-11-05
Repository: SWP391_Backend (working branch: feature/ai-status-logic-consistency)
Project area: Backend/webAPI

## Purpose
This document summarizes the new registration/OTP feature work and all related changes implemented in the `webAPI` backend. It collects the important code changes, helper scripts, security notes, testing and deployment steps, and next steps.

Primary goals implemented:
- Use a single Google reCAPTCHA v2 checkbox check at the initial registration form only.
- Remove CAPTCHA checks from OTP sending and from the final registration confirmation API.
- Replace the database OTP table with an in-memory thread-safe cache (ConcurrentHashMap) for OTP storing and verification.
- Add extensive debug logging around reCAPTCHA verification to help diagnose issues and confirm Google API calls.


## High-level flow after changes

1. User fills registration form (name, phone, email, password) and completes the reCAPTCHA widget (one-time check).
2. User submits the registration form (server verifies CAPTCHA once here).
3. OTP modal opens (no CAPTCHA). User clicks "Gửi mã OTP" → backend sends OTP to email and saves OTP to in-memory cache with 5-minute expiry.
4. User enters OTP in the modal and clicks "Xác thực và Đăng ký" → backend validates OTP only (no CAPTCHA verification at this step) and creates the account.


## Files changed or created (important)

Note: all paths are relative to the `Backend/webAPI` folder.

### Backend source files (Java)
- `src/java/controller/registerController.java`
  - Removed server-side CAPTCHA requirement at the final registration API (OTP confirmation endpoint).
  - Request class updated to remove `gRecaptchaResponse` field.
  - Logs added to indicate the CAPTCHA verification was intentionally skipped at this stage.

- `src/java/controller/SendRegistrationOtpController.java`
  - Removed CAPTCHA verification when sending OTP.
  - Now accepts only `email` and coordinates with `RegistrationOtpDAO`.

- `src/java/controller/ResendVerificationController.java`
  - Adjusted timestamp handling to use `LocalDateTime` where appropriate (compatibility with in-memory DAO).

- `src/java/dao/RegistrationOtpDAO.java` (rewritten)
  - Replaced previous DB-based implementation with an in-memory `ConcurrentHashMap<String, OtpRecord>`.
  - Methods:
    - `saveOtp(email, otp)` — stores OTP and expiration time.
    - `verifyOtp(email, otp)` — checks OTP, expiry, used flag.
    - `markOtpAsUsed(email)` — marks OTP consumed.
    - `cleanupExpiredOtps()` — removes expired/used entries periodically/on-access.
  - OTP expiry: default 5 minutes.
  - Thread-safe for servlet environment.

- `src/java/util/RecaptchaVerifier.java`
  - Extended logging added to trace reCAPTCHA verify requests and responses (token length, secret found, HTTP response, Google JSON response).
  - Used by login flow; not used for OTP send/final registration anymore.

- `src/java/controller/loginController.java` (unchanged flow but extended logging)
  - Logging added to print request receipt and verification flow for easier debugging.


### Frontend (web)
- `web/index.html`
  - Removed reCAPTCHA widget from OTP modal.
  - Kept reCAPTCHA in registration and login forms.
  - Updated `regOtpSendOtp()` to send only email (no gRecaptchaResponse parameter).


### Helper scripts and auxiliary files (kept)
All of these files were created or present during development and are intentionally kept. They are useful for testing, local deployment, or documentation.

- `SET_RECAPTCHA_SECRET.ps1` and `SET_RECAPTCHA_SECRET.bat`
  - Purpose: quick helpers to set `RECAPTCHA_SECRET` environment variable on Windows.
  - Security note: both scripts contain the secret value hard-coded. **This is a security risk.** Consider replacing the hard-coded secret with a placeholder and prompting the user at runtime, or move scripts out of the repo into a secure location.

- `scripts/probe_admin.ps1`
  - Quick probe script to check `/api/admin/dashboard` across candidate server paths.

- `scripts/smoke_user_management.ps1`
  - Comprehensive smoke test (login, create user, upload avatar, edit, delete). Useful for integration checks.

- `scripts/login_fetch_users.ps1`, `scripts/login_and_probe.ps1`
  - Lightweight login + fetch/probe helpers for development and tests.

- `web/swagger-ui/README.md`
  - README file belonging to the Swagger UI distribution included in the `web` folder.


## Files intentionally left in repo (summary)
- `SET_RECAPTCHA_SECRET.ps1` (contains secret) — KEEP for now, but consider replacing hard-coded secret.
- `SET_RECAPTCHA_SECRET.bat` — KEEP for parity with Windows scripts.
- `scripts/*.ps1` — KEEP (useful test helpers and smoke tests).
- `web/swagger-ui/README.md` — KEEP (library distribution file).


## Security note
- The `SET_RECAPTCHA_SECRET` scripts include the secret in cleartext. This file should be considered sensitive. Recommended actions:
  1. Replace the hard-coded secret in these helper scripts with a placeholder and instructions (e.g., `%RECAPTCHA_SECRET%` or prompt input), or
  2. Remove the scripts from the repository and store them in a secure vault (or a private admin-only repo), or
  3. Keep scripts but add them to `.gitignore` (after removing secret) and store the actual secret in CI environment variables or OS key store.

If you want, I can update these scripts now to prompt for the secret rather than keeping it in the repo.


## How to test (quick checklist)
1. Ensure `RECAPTCHA_SECRET` environment variable is set on server/host (and restart NetBeans/Tomcat):

```powershell
# Windows PowerShell (example - persistent machine env, requires admin)
setx RECAPTCHA_SECRET "<your-secret-goes-here>" /M
# Then restart NetBeans/Tomcat
```

2. Build and deploy the webAPI war:

```powershell
cd c:\AK\HOCKI5\SWP391\Code\TestAIChatBox\SWP391_Backend\Backend\webAPI
ant clean dist
# then deploy dist/webAPI.war into Tomcat or run via NetBeans
```

3. Registration flow test (end-to-end):
   - Open `http://localhost:8080/webAPI/` in browser.
   - Fill Registration form and check CAPTCHA (only required at this point).
   - Click "Đăng ký" → OTP modal opens (no CAPTCHA widget present).
   - Click "Gửi mã OTP" to request OTP send.
   - Verify email for OTP code (code expires in 5 minutes).
   - Enter OTP in modal and click "Xác thực và Đăng ký" → expected success.

4. Login test: Confirm reCAPTCHA verifies on login and RecaptchaVerifier calls Google (check logs for the extended debug output).


## Logs you should see (examples)
- `[RecaptchaVerifier] START VERIFICATION: token length=...`  
- `[RecaptchaVerifier] Secret key found in environment`  
- `[RecaptchaVerifier] SENDING REQUEST TO GOOGLE: https://www.google.com/recaptcha/api/siteverify`  
- `[RecaptchaVerifier] GOOGLE RESPONSE CODE: 200`  
- `[RecaptchaVerifier] GOOGLE RESPONSE: { "success": true, ... }`  
- `[RegistrationOtpDAO] ✅ OTP saved to IN-MEMORY CACHE for email: ...`  
- `[registerController] ✅ CAPTCHA verification SKIPPED (already verified at register form)`  
- `[registerController] OTP verified successfully for email: ...`


## Branching & version control notes
- Current working branch in repo: `feature/ai-status-logic-consistency`.
- Suggested new branch name for a dedicated feature branch if you want to capture these changes separately:
  - `feature/single-captcha-inmemory-otp`

If you'd like me to create and push a new branch with a commit that adds this SUMMARY file (and optionally updated scripts), I can do it — but pushing requires network/credentials from your environment (I'll prompt before pushing).


## Files created as documentation during development (previously present but user reverted some)
- `IN_MEMORY_OTP_CACHE.md` — full design & rationale doc (if present earlier) — if you want it re-created I can add it to the repo.
- `FIX_FINAL_REGISTRATION_CAPTCHA.md` — step-by-step change log describing removal of CAPTCHA at final registration (can re-create if needed).
- `FIX_OTP_DATABASE_ERROR.md` — notes on the database error and the in-memory replacement (can re-create if needed).

Let me know if you want any of those detailed documents re-created in this commit.


## Next steps I can perform (choose any)
- [ ] Commit this SUMMARY file in a new branch and push to remote (branch name: `feature/single-captcha-inmemory-otp`).
- [ ] Replace the hard-coded secret in `SET_RECAPTCHA_SECRET.*` scripts with a prompt/placeholder and commit the safer versions.
- [ ] Move backup copies of any files to `_backup/cleanup-YYYYMMDD-HHMMSS/` and note them in the SUMMARY.
- [ ] Recreate any of the detailed docs (`IN_MEMORY_OTP_CACHE.md`, `FIX_*`) and include them in the repo.


## Action requested from you
- I'll leave all files intact (per your instruction). Please reply if you want me to also:
  - Commit this `ULTIMATE_SUMMARY_NEW_FEATURE.md` into a new branch and push it to the remote repo now, or
  - Make security edits to the `SET_RECAPTCHA_SECRET` helpers first, then commit and push.


## Final note
This SUMMARY file is intentionally comprehensive: it centralizes operational notes, files to keep, and testing guidance to make QA and deployment straightforward. If you want a condensed `README.md` with a short checklist instead, I can produce that as a companion.
