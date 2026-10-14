# Phone Location — 2GIS

A consent-first Android starter for sharing **your own** current location and opening coordinates in 2GIS. It does not locate a person from their phone number.

## Current implementation status
- Android app: Kotlin, Jetpack Compose, Material 3, Android fused location provider.
- User can request their current location, see coordinates/accuracy/time, open a map URL, and share a location link via Android's share sheet.
- International phone input is validated for basic E.164 shape and supports `+992`. It does not authenticate the phone owner.
- Responsive Tajik consent page (`web/`) requests browser geolocation only after a click. It requires a configured backend and HTTPS before attempting submission.
- Express starter (`backend/`) creates random single-use invitation tokens, applies basic rate limiting and validation, and supports session revocation.
- GitHub Actions builds the debug APK and uploads it as an artifact.

**Not yet production complete:** the app's invitation button is deliberately labelled as a sample because it is not wired to the backend. The backend is an in-memory starter and does not provide authenticated requester access, persistent history, or a live location-read API. Do not use it for real users until the hardening steps in `docs/DEPLOYMENT.md` are completed. The UI does not silently track or run location in the background.

## What it cannot do
- It cannot find someone using only a phone number.
- It cannot bypass Android/browser permission prompts.
- It cannot guarantee a live location unless the person shares it and the required service is deployed.
- It cannot use 2GIS as a phone-number tracking service.

## Repository structure
- `app/` — Android app.
- `web/` — recipient consent page.
- `backend/` — invitation API starter.
- `docs/` — architecture, privacy, and deployment notes.
- `.github/workflows/` — CI build and unsigned release workflow.

## Build APK with GitHub Actions (phone-only)
1. Download and extract `PhoneLocation2GIS.zip` on your phone.
2. Create a new repository on GitHub named `PhoneLocation2GIS`.
3. Upload the **contents** of the extracted folder (not an extra nested folder) to the repository's `main` branch. GitHub mobile web may require uploading files through the browser; the GitHub app may not support every folder upload flow. If folder upload is awkward, use GitHub's web upload or a mobile Git client.
4. Open the repository → **Actions** → **Android CI** → **Run workflow**. A push to `main` also starts it.
5. Wait until the workflow is green. Open the run, find **Artifacts**, and download `PhoneLocation2GIS-debug-apk`.
6. Extract the artifact ZIP to get `app-debug.apk`.
7. On Android, open the APK and allow installation from that source if Android asks. Install only APKs built from a repository you trust.

## Build locally (optional)
Requires JDK 17 and Android SDK 35. The provided `gradlew` bootstrap script downloads Gradle 8.9, but GitHub Actions uses the pinned Gradle setup action directly. Run `./gradlew testDebugUnitTest assembleDebug`.

## 2GIS
The Android app uses 2GIS's documented `dgis://2gis.ru/routeSearch/rsType/pedestrian/to/<lon>,<lat>` deep-link pattern; if 2GIS cannot be opened it falls back to Android's `geo:` intent. The route deep link is documented by 2GIS Help. url2GIS deeplink documentationhttps://help.2gis.com/question/developers-launching-2gis-navigation-using-deeplink For a production embedded map or richer search, obtain credentials and follow official 2GIS documentation. No private API key is included. Never assume 2GIS can find a phone from a number.

## Backend and web deployment
See `docs/DEPLOYMENT.md`, `web/README.md`, and `backend/README.md`. Production requires HTTPS hosting, a configured `VITE_API_BASE`, a secure `TOKEN_PEPPER`, and—before real user use—authenticated identities plus persistent database authorization. No service has been deployed by this ZIP.

## Tajik quick guide
1. ZIP-ро кушоед ва ҳамаи файлҳои дохили онро ба репозитории нави GitHub бор кунед.
2. Дар GitHub бахши **Actions** → **Android CI** → **Run workflow**-ро пахш кунед.
3. Пас аз сабз шудани workflow, дар саҳифаи run қисми **Artifacts**-ро кушоед ва `PhoneLocation2GIS-debug-apk`-ро гиред.
4. ZIP-и artifact-ро кушоед, `app-debug.apk`-ро дар телефон насб кунед.
5. Барои гирифтани макони худ иҷозати GPS-ро танҳо вақте диҳед, ки мехоҳед. Барои мубодила тугмаи мубодиларо истифода баред.
6. Барои ҷойгиршавии шахси дигар рақами ӯ кофӣ нест. Ӯ бояд даъватро кушода, худаш розигӣ диҳад. Backend-и ин лоиҳа ҳоло намунавӣ аст ва барои пайгирии воқеӣ пурра омода нест.

## Tests
The repository contains basic Android phone-format unit tests. This environment did not execute an Android/Gradle build; GitHub Actions is configured to perform it. Review CI results before installation.
