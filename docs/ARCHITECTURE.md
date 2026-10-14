# Architecture

## Android
Single-activity Compose app. UI state is local to the composable; Android's fused location provider supplies the device's current position after the system permission launcher returns a grant. There is no background service and no silent tracking. A production version should move state and location operations into ViewModel/use-case/repository layers, persist only minimal session metadata, and add navigation tests.

## Web
TypeScript/Vite static consent page. Browser geolocation is called only after an explicit button click and only in a secure context. It sends coordinates to the configured backend only after a successful geolocation callback.

## Backend
Express API with Helmet, rate limiting, Zod coordinate validation, cryptographically random single-use invitation tokens, and TTL. The starter stores session state in process memory and intentionally offers no coordinate retrieval endpoint. It is not suitable for production or multiple instances without a database and identity/authentication layer.

## Trust boundaries
Phone number is contact information only, never a credential. An invitation token is a temporary capability and must be protected. A production requester must authenticate, recipient consent must be recorded server-side, all session reads must verify requester/recipient authorization, and revocation/expiry must be enforced server-side for every read.
