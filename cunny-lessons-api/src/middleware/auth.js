// auth.js — Firebase ID token verification for Hono/Cloudflare Workers
import { jwk } from 'hono/jwk'

const JWKS_URI = 'https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com'
const FIREBASE_PROJECT_ID = 'cunny-e1337'

const _jwkMw = jwk({ jwks_uri: JWKS_URI, alg: ['RS256'] })

async function verifyToken(c) {
  const mw = _jwkMw
  let passed = false
  const fakeNext = async () => { passed = true }
  await mw(c, fakeNext)
  if (!passed) return null
  const payload = c.get('jwtPayload')
  if (!payload || payload.aud !== FIREBASE_PROJECT_ID) return null
  return payload
}

export async function requireAuth(c, next) {
  const authHeader = c.req.header('Authorization')
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return c.json({ error: true, message: 'Missing or invalid Authorization header' }, 401)
  }
  try {
    c.set('firebaseUid', null)
    c.set('email', null)
    const payload = await verifyToken(c)
    if (!payload) return c.json({ error: true, message: 'Token verification failed' }, 401)
    c.set('firebaseUid', payload.sub)
    c.set('email', payload.email || null)
    await next()
  } catch (err) {
    return c.json({ error: true, message: 'Token verification failed: ' + err.message }, 401)
  }
}

export async function optionalAuth(c, next) {
  try {
    c.set('firebaseUid', null)
    c.set('email', null)
    const payload = await verifyToken(c)
    if (payload) {
      c.set('firebaseUid', payload.sub)
      c.set('email', payload.email || null)
    }
  } catch {
    c.set('firebaseUid', null)
    c.set('email', null)
  }
  await next()
}
