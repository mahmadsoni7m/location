# Backend — consent invitation starter

## Status and important limitation
This is a minimal Express starter, not a production-ready multi-user location service. It uses in-memory Maps, so data is lost on restart and instances do not share state. It intentionally has no endpoint that returns coordinates. Before production, add authenticated requester and recipient identities, a persistent database with row-level access controls, audit events that exclude coordinates/tokens, deletion/retention jobs, CSRF/origin controls appropriate to your auth scheme, and security review.

## Run
Requires Node.js 20+. Copy `.env.example` to `.env`, set `TOKEN_PEPPER` to a random secret of 32+ characters and set `PUBLIC_BASE_URL` to the HTTPS origin of the deployed consent page. Never commit `.env`.
```sh
npm install
npm run dev
```

## API currently implemented
- `GET /health` — liveness only.
- `POST /api/invitations` — creates a random single-use invitation token; requires HTTPS `PUBLIC_BASE_URL` and configured pepper.
- `POST /api/invitations/:token/accept` — invoked only after the recipient clicks consent and browser geolocation returns a position. Validates coordinate bounds.
- `POST /api/sessions/:id/revoke` — revokes a session by bearer session ID. The ID is a capability, not identity; use proper authentication before production.

Rate limiting and Helmet headers are enabled. The session store is volatile and there is no location-read endpoint. Therefore this starter does not yet implement requester-visible live sharing/history and must not be represented as doing so.
