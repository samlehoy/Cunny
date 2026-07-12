// routes/parentConsentRoutes.js
import { Hono } from 'hono'
import { getClient } from '../config/database.js'
import { sendParentConsentEmail } from '../services/emailService.js'

const router = new Hono()

/**
 * POST /auth/parent-consent
 * Public — no auth required.
 * Creates a consent request and sends email to parent.
 */
router.post('/auth/parent-consent', async (c) => {
  const { child_name, parent_email, birth_year } = await c.req.json()

  // --- Validation ---
  if (!child_name || !child_name.trim()) {
    return c.json({ error: true, message: 'Nama anak wajib diisi' }, 400)
  }
  if (!parent_email || !parent_email.includes('@')) {
    return c.json({ error: true, message: 'Email orang tua tidak valid' }, 400)
  }

  const currentYear = new Date().getFullYear()
  const age = currentYear - birth_year
  if (!birth_year || age > 12) {
    return c.json({ error: true, message: 'Fitur ini hanya untuk anak usia 12 tahun ke bawah' }, 400)
  }

  let client
  try {
    client = getClient(c.env)
    await client.connect()

    // --- Cooldown check: max 1 request per parent_email per 60 seconds ---
    const lastRequest = await client.query(
      `SELECT created_at FROM parent_consent_requests
       WHERE parent_email = $1 ORDER BY created_at DESC LIMIT 1`,
      [parent_email]
    )
    if (lastRequest.rows.length > 0) {
      const lastTime = new Date(lastRequest.rows[0].created_at).getTime()
      const diffSeconds = (Date.now() - lastTime) / 1000
      if (diffSeconds < 60) {
        const remaining = Math.ceil(60 - diffSeconds)
        return c.json({
          error: true,
          message: `Mohon tunggu ${remaining} detik sebelum mengirim email lagi.`
        }, 429)
      }
    }

    // --- Daily limit check: max 10 requests per parent_email per day (bypassed for test emails) ---
    const isTestEmail = parent_email.endsWith('@example.com') || parent_email.includes('test')
    if (!isTestEmail) {
      const rateLimitResult = await client.query(
        `SELECT COUNT(*) as cnt FROM parent_consent_requests
         WHERE parent_email = $1 AND created_at > NOW() - INTERVAL '1 day'`,
        [parent_email]
      )
      if (parseInt(rateLimitResult.rows[0].cnt, 10) >= 10) {
        return c.json({
          error: true,
          message: 'Batas harian pengiriman email terlampaui. Coba lagi besok.'
        }, 429)
      }
    }

    // --- Generate token & insert ---
    const token = crypto.randomUUID()
    await client.query(
      `INSERT INTO parent_consent_requests (token, child_name, parent_email, birth_year, status, created_at, expires_at)
       VALUES ($1, $2, $3, $4, 'pending', NOW(), NOW() + INTERVAL '7 days')`,
      [token, child_name.trim(), parent_email, birth_year]
    )

    // --- Build consent link & send email ---
    const appUrl = c.env.APP_URL || 'https://cunny-content-api.muttaqien0111.workers.dev'
    const consentLink = `${appUrl}/parent-consent/${token}`

    await sendParentConsentEmail(c.env, {
      parentEmail: parent_email,
      childName: child_name.trim(),
      consentLink,
    })

    return c.json({ error: false, message: 'Email terkirim ke orang tua' }, 200)
  } catch (err) {
    console.error('Parent consent error:', err)
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

export default router
