# Consent web page
Mobile-first static TypeScript/Vite page. Deploy the `dist/` output to an HTTPS static host. Set `VITE_API_BASE` at build time to the deployed backend origin. Without this setting, the page intentionally does not request geolocation.

The invitation token is carried in the query string but no coordinates are placed in URLs. For a production deployment, configure a strict Referrer-Policy, avoid third-party scripts, and exchange invitation tokens for authenticated server-side session state. Query-string tokens can leak through browser history or copied URLs; use short TTL, single-use tokens, and consider a fragment-based bootstrap exchange.
