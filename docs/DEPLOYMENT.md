# Deployment and GitHub Actions

## Android CI
Workflow: `.github/workflows/android-build.yml`. Runs on `main` pushes, pull requests, and manual dispatch. Uses JDK 17, Android SDK 35, Gradle 8.9, runs unit tests and assembles debug APK. Artifact path is `app/build/outputs/apk/debug/app-debug.apk`. No signing secrets are needed for debug builds.

## Unsigned release workflow
`.github/workflows/release.yml` produces an unsigned release APK artifact. It is not a Play Store-ready signed release. If release signing is added, create a keystore outside the repo and store its base64 form/password/alias only in GitHub Actions Secrets. Never commit keystores or passwords.

## Web
1. From `web/`, run `npm ci` (after generating/committing a lockfile with `npm install`) and `npm run build`.
2. Set `VITE_API_BASE` to the HTTPS backend origin at build time.
3. Deploy `web/dist` to an HTTPS static host. No deployment has been performed by this repository.
4. Do not expose backend secrets as `VITE_*` variables; those are public browser values.

## Backend
1. Use Node.js 20+ on a trusted HTTPS hosting provider.
2. Configure `TOKEN_PEPPER` as a random secret with at least 32 characters and `PUBLIC_BASE_URL` as the HTTPS consent page origin.
3. Set a short `SESSION_TTL_MINUTES`; monitor abuse without logging coordinates/tokens.
4. Add a database and authentication before real users. Current Maps are volatile and do not scale across instances.
5. Set CORS to the exact web origin when browser access is enabled; do not use wildcard CORS with credentials.
6. Add authenticated session access, identity-bound authorization, deletion, retention, monitoring, and security review. The starter intentionally has no coordinate-read API.

## 2GIS configuration
No API key is used by the current fallback map-opening action. For SDKs, geocoding, or advanced features, consult official 2GIS developer documentation and store keys in appropriate server-side configuration where possible. Never commit private keys.

## Phone-only upload
Extract ZIP, create GitHub repository, upload its contents, then open Actions → Android CI → Run workflow. Download the generated artifact after the run completes.
