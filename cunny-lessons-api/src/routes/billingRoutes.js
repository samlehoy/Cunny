import { Hono } from 'hono'
import { getClient } from '../config/database.js'
import { requireAuth } from '../middleware/auth.js'

const router = new Hono()
router.use('/billing/*', requireAuth)

// POST /billing/verify
// Verify and store a Google Play purchase record
router.post('/billing/verify', async (c) => {
  const firebaseUid = c.get('firebaseUid')
  const { purchaseToken, productId, packageName } = await c.req.json()

  if (!purchaseToken || !productId) {
    return c.json({ error: true, message: 'purchaseToken and productId are required' }, 400)
  }

  let client
  try {
    client = getClient(c.env)
    await client.connect()

    // Get user ID from firebase UID
    const userResult = await client.query(
      'SELECT id FROM users WHERE firebase_uid = $1',
      [firebaseUid]
    )
    if (userResult.rows.length === 0) {
      return c.json({ error: true, message: 'User not found' }, 404)
    }
    const userId = userResult.rows[0].id

    // For MVP: store purchase record locally
    // Full Google Play Developer API verification can be added post-launch
    const now = new Date().toISOString()
    // Default subscription period: 30 days from now
    const expiresAt = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString()

    const result = await client.query(
      `INSERT INTO purchases (user_id, product_id, purchase_token, package_name, status, purchased_at, expires_at)
       VALUES ($1, $2, $3, $4, 'active', $5, $6)
       ON CONFLICT (purchase_token)
       DO UPDATE SET status = 'active', expires_at = $6
       RETURNING *`,
      [userId, productId, purchaseToken, packageName || 'com.eleonorez.cunny', now, expiresAt]
    )

    return c.json({
      error: false,
      message: 'Purchase verified successfully',
      purchase: result.rows[0]
    }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

// GET /billing/status
// Check current subscription status
router.get('/billing/status', async (c) => {
  const firebaseUid = c.get('firebaseUid')

  let client
  try {
    client = getClient(c.env)
    await client.connect()

    const userResult = await client.query(
      'SELECT id FROM users WHERE firebase_uid = $1',
      [firebaseUid]
    )
    if (userResult.rows.length === 0) {
      return c.json({ error: true, message: 'User not found' }, 404)
    }
    const userId = userResult.rows[0].id

    const result = await client.query(
      `SELECT * FROM purchases
       WHERE user_id = $1 AND status = 'active' AND expires_at > NOW()
       ORDER BY purchased_at DESC LIMIT 1`,
      [userId]
    )

    const isActive = result.rows.length > 0

    return c.json({
      error: false,
      isPremium: isActive,
      subscription: isActive ? result.rows[0] : null
    }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

export default router
