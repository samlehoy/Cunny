import { Hono } from 'hono'
import { getClient } from '../config/database.js'
import { requireAuth } from '../middleware/auth.js'

const router = new Hono()
router.use('/me/*', requireAuth)

router.get('/me/progress', async (c) => {
  const firebaseUid = c.get('firebaseUid')
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    const userResult = await client.query('SELECT id FROM users WHERE firebase_uid = $1', [firebaseUid])
    if (userResult.rows.length === 0) return c.json({ error: true, message: 'User not found' }, 404)
    const userId = userResult.rows[0].id
    const progressResult = await client.query('SELECT * FROM progress WHERE user_id = $1 ORDER BY completed_at DESC', [userId])
    const badgesResult = await client.query('SELECT * FROM achievements WHERE user_id = $1', [userId])
    return c.json({
      error: false,
      progress: progressResult.rows,
      badges: badgesResult.rows,
    }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

router.put('/me/progress', async (c) => {
  const firebaseUid = c.get('firebaseUid')
  const { lesson_slug, score } = await c.req.json()
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    const userResult = await client.query('SELECT id FROM users WHERE firebase_uid = $1', [firebaseUid])
    if (userResult.rows.length === 0) return c.json({ error: true, message: 'User not found' }, 404)
    const userId = userResult.rows[0].id
    const result = await client.query(
      `INSERT INTO progress (user_id, lesson_slug, score, completed_at)
       VALUES ($1, $2, $3, NOW())
       ON CONFLICT (user_id, lesson_slug)
       DO UPDATE SET score = GREATEST(progress.score, $3), completed_at = NOW()
       RETURNING *`,
      [userId, lesson_slug, score || 0]
    )
    return c.json({ error: false, progress: result.rows[0] }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

router.put('/me/gamification', async (c) => {
  const firebaseUid = c.get('firebaseUid')
  const { xp, level, streak, energy, last_active_date } = await c.req.json()
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    
    const result = await client.query(
      `UPDATE users
       SET xp = COALESCE($1, xp),
           level = COALESCE($2, level),
           streak = COALESCE($3, streak),
           energy = COALESCE($4, energy),
           last_active_date = COALESCE($5, last_active_date),
           updated_at = NOW()
       WHERE firebase_uid = $6
       RETURNING *`,
      [
        xp !== undefined ? xp : null,
        level !== undefined ? level : null,
        streak !== undefined ? streak : null,
        energy !== undefined ? energy : null,
        last_active_date !== undefined ? last_active_date : null,
        firebaseUid
      ]
    )

    if (result.rows.length === 0) {
      return c.json({ error: true, message: 'User not found' }, 404)
    }

    return c.json({ error: false, user: result.rows[0] }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

router.put('/me/badges', async (c) => {
  const firebaseUid = c.get('firebaseUid')
  const { badges } = await c.req.json()
  if (!badges || !Array.isArray(badges)) {
    return c.json({ error: true, message: 'badges array is required' }, 400)
  }
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    const userResult = await client.query('SELECT id FROM users WHERE firebase_uid = $1', [firebaseUid])
    if (userResult.rows.length === 0) return c.json({ error: true, message: 'User not found' }, 404)
    const userId = userResult.rows[0].id

    const upserted = []
    for (const badgeSlug of badges) {
      const result = await client.query(
        `INSERT INTO achievements (user_id, badge_slug, unlocked_at)
         VALUES ($1, $2, NOW())
         ON CONFLICT (user_id, badge_slug) DO NOTHING
         RETURNING *`,
        [userId, badgeSlug]
      )
      if (result.rows.length > 0) upserted.push(result.rows[0])
    }

    return c.json({ error: false, badges: upserted }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

export default router
