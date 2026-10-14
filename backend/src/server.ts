import express from 'express';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import { randomBytes, createHash, timingSafeEqual } from 'node:crypto';
import { z } from 'zod';

const app = express();
app.disable('x-powered-by');
app.use(helmet());
app.use(express.json({ limit: '12kb' }));
app.use(rateLimit({ windowMs: 60_000, limit: 60, standardHeaders: true, legacyHeaders: false }));

const port = Number(process.env.PORT ?? 8787);
const pepper = process.env.TOKEN_PEPPER ?? '';
const ttlMinutes = Math.min(1440, Math.max(5, Number(process.env.SESSION_TTL_MINUTES ?? 60)));
const publicBase = process.env.PUBLIC_BASE_URL ?? '';
if (pepper.length < 32) console.warn('TOKEN_PEPPER must be at least 32 characters. Invitation creation is disabled.');
if (process.env.NODE_ENV === 'production' && !publicBase.startsWith('https://')) {
  console.warn('PUBLIC_BASE_URL must use HTTPS in production.');
}

type Invitation = { tokenHash: string; createdAt: number; expiresAt: number; accepted: boolean; revoked: boolean; sessionId?: string };
type Session = { id: string; createdAt: number; expiresAt: number; revoked: boolean; latitude?: number; longitude?: number; accuracy?: number; updatedAt?: number };
const invitations = new Map<string, Invitation>();
const sessions = new Map<string, Session>();

const coordsSchema = z.object({
  latitude: z.number().min(-90).max(90),
  longitude: z.number().min(-180).max(180),
  accuracy: z.number().finite().min(0).max(100_000)
}).strict();

function hash(value: string) { return createHash('sha256').update(pepper).update(':').update(value).digest('hex'); }
function safeEqual(a: string, b: string) {
  const aa = Buffer.from(a); const bb = Buffer.from(b);
  return aa.length === bb.length && timingSafeEqual(aa, bb);
}
function cleanExpired() {
  const now = Date.now();
  for (const [key, item] of invitations) if (item.expiresAt <= now || item.revoked) invitations.delete(key);
  for (const [key, item] of sessions) if (item.expiresAt <= now || item.revoked) sessions.delete(key);
}
function validSession(id: string) {
  const item = sessions.get(id);
  return item && !item.revoked && item.expiresAt > Date.now() ? item : undefined;
}
app.get('/health', (_req, res) => res.json({ ok: true }));

// The creator receives the raw token once. Store only its hash; never log tokens.
app.post('/api/invitations', (req, res) => {
  if (pepper.length < 32 || !publicBase.startsWith('https://')) return res.status(503).json({ error: 'service_not_configured' });
  cleanExpired();
  const token = randomBytes(32).toString('base64url');
  const now = Date.now();
  invitations.set(hash(token), { tokenHash: hash(token), createdAt: now, expiresAt: now + ttlMinutes * 60_000, accepted: false, revoked: false });
  res.status(201).json({ invitationUrl: `${publicBase}/?invite=${encodeURIComponent(token)}`, expiresAt: new Date(now + ttlMinutes * 60_000).toISOString() });
});

// Acceptance is an explicit POST triggered by a user click in the consent page.
// This starter backend stores the latest location in memory; it is NOT durable or multi-instance safe.
app.post('/api/invitations/:token/accept', (req, res) => {
  cleanExpired();
  const token = req.params.token;
  if (!token || token.length > 100) return res.status(400).json({ error: 'invalid_invitation' });
  const item = invitations.get(hash(token));
  if (!item || item.revoked || item.expiresAt <= Date.now() || item.accepted) return res.status(404).json({ error: 'invitation_expired_or_used' });
  const parsed = coordsSchema.safeParse(req.body);
  if (!parsed.success) return res.status(400).json({ error: 'invalid_location' });
  const id = randomBytes(24).toString('base64url');
  const now = Date.now();
  sessions.set(id, { id, createdAt: now, expiresAt: Math.min(item.expiresAt, now + ttlMinutes * 60_000), revoked: false, ...parsed.data, updatedAt: now });
  item.accepted = true; item.sessionId = id;
  res.status(201).json({ sessionId: id, expiresAt: new Date(sessions.get(id)!.expiresAt).toISOString() });
});

// A bearer session ID only permits its holder to revoke. No endpoint returns coordinates.
// Production deployments must add authenticated requester/recipient identity and a database.
app.post('/api/sessions/:id/revoke', (req, res) => {
  const id = req.params.id;
  if (!id || id.length > 100) return res.status(400).json({ error: 'invalid_session' });
  const session = sessions.get(id);
  if (!session || !safeEqual(session.id, id)) return res.status(404).json({ error: 'session_not_found' });
  session.revoked = true;
  sessions.delete(id);
  res.json({ revoked: true });
});

app.use((_req, res) => res.status(404).json({ error: 'not_found' }));
app.listen(port, () => console.log(`Consent backend listening on ${port}`));
