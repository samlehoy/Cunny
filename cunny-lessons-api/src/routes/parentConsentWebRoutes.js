// routes/parentConsentWebRoutes.js
import { Hono } from 'hono'
import { getClient } from '../config/database.js'
import { renderConsentForm } from '../templates/parentConsentForm.js'
import { renderConsentSuccess } from '../templates/parentConsentSuccess.js'
import { renderConsentError } from '../templates/parentConsentError.js'

const router = new Hono()

/**
 * Look up a consent request token and validate it.
 * Returns { valid, row, errorHtml } — if not valid, errorHtml is ready to return.
 */
async function resolveToken(c, client, token) {
  const result = await client.query(
    'SELECT * FROM parent_consent_requests WHERE token = $1',
    [token]
  )

  if (result.rows.length === 0) {
    return { valid: false, errorHtml: renderConsentError('Link tidak ditemukan. Pastikan Anda menggunakan link yang benar dari email.') }
  }

  const row = result.rows[0]

  if (row.status === 'completed') {
    return { valid: false, errorHtml: renderConsentError('Akun sudah dibuat sebelumnya. Anak Anda bisa langsung login di app Cunny.') }
  }

  if (new Date(row.expires_at) < new Date()) {
    await client.query(
      "UPDATE parent_consent_requests SET status = 'expired' WHERE id = $1",
      [row.id]
    )
    return { valid: false, errorHtml: renderConsentError('Link kadaluarsa. Silakan minta link baru melalui app Cunny.') }
  }

  return { valid: true, row }
}

/**
 * GET /parent-consent/:token
 * Public — renders the consent form.
 */
router.get('/parent-consent/:token', async (c) => {
  const token = c.req.param('token')
  let client
  try {
    client = getClient(c.env)
    await client.connect()

    const { valid, row, errorHtml } = await resolveToken(c, client, token)
    if (!valid) return c.html(errorHtml)

    return c.html(renderConsentForm(token, row.child_name))
  } catch (err) {
    console.error('Consent form error:', err)
    return c.html(renderConsentError('Terjadi kesalahan. Silakan coba lagi nanti.'))
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

/**
 * POST /parent-consent/:token/create
 * Public — processes the form, creates Firebase account + DB user.
 */
router.post('/parent-consent/:token/create', async (c) => {
  const token = c.req.param('token')
  const body = await c.req.parseBody()
  const { child_name, email, password, confirm_password } = body

  // --- Validation ---
  if (!child_name || !email || !password || !confirm_password) {
    return c.html(renderConsentForm(token, child_name || '', 'Semua kolom wajib diisi.'))
  }
  if (password !== confirm_password) {
    return c.html(renderConsentForm(token, child_name, 'Password dan konfirmasi password tidak sama.'))
  }
  if (password.length < 8) {
    return c.html(renderConsentForm(token, child_name, 'Password minimal 8 karakter.'))
  }

  let client
  try {
    client = getClient(c.env)
    await client.connect()

    // --- Verify token ---
    const { valid, row, errorHtml } = await resolveToken(c, client, token)
    if (!valid) return c.html(errorHtml)

    // --- Create Firebase account ---
    const signUpRes = await fetch(
      `https://identitytoolkit.googleapis.com/v1/accounts:signUp?key=${c.env.FIREBASE_API_KEY}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          password,
          displayName: child_name,
          returnSecureToken: true,
        }),
      }
    )

    const signUpData = await signUpRes.json()

    if (signUpData.error) {
      const firebaseMsg = signUpData.error.message || ''
      const friendlyMessages = {
        'EMAIL_EXISTS': 'Email ini sudah digunakan. Gunakan email lain.',
        'WEAK_PASSWORD': 'Password terlalu lemah. Gunakan kombinasi huruf, angka, dan simbol.',
        'INVALID_EMAIL': 'Format email tidak valid.',
      }
      // Check if any known error key is a prefix of the Firebase message
      const matchedKey = Object.keys(friendlyMessages).find((key) => firebaseMsg.startsWith(key))
      const errorText = matchedKey ? friendlyMessages[matchedKey] : `Gagal membuat akun: ${firebaseMsg}`
      return c.html(renderConsentForm(token, child_name, errorText))
    }

    const firebaseUid = signUpData.localId
    const idToken = signUpData.idToken

    // --- Update Firebase profile displayName ---
    await fetch(
      `https://identitytoolkit.googleapis.com/v1/accounts:update?key=${c.env.FIREBASE_API_KEY}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          idToken,
          displayName: child_name,
          returnSecureToken: false,
        }),
      }
    )

    // --- Insert user into DB ---
    await client.query(
      `INSERT INTO users (firebase_uid, display_name, email, role, birth_year, parent_email, xp, level, streak, energy)
       VALUES ($1, $2, $3, 'student', $4, $5, 0, 1, 0, 5)`,
      [firebaseUid, child_name, email, row.birth_year, row.parent_email]
    )

    // --- Mark consent request as completed ---
    await client.query(
      "UPDATE parent_consent_requests SET status = 'completed', completed_at = NOW() WHERE id = $1",
      [row.id]
    )

    return c.html(renderConsentSuccess(child_name))
  } catch (err) {
    console.error('Account creation error:', err)
    return c.html(renderConsentForm(token, child_name || '', `Terjadi kesalahan: ${err.message}`))
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

export default router
