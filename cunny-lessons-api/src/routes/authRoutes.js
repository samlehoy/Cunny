import { Hono } from 'hono'
import { getClient } from '../config/database.js'
import { requireAuth } from '../middleware/auth.js'

const router = new Hono()

router.post('/auth/register', requireAuth, async (c) => {
  const firebaseUid = c.get('firebaseUid')
  const { display_name, role, birth_year, parent_email } = await c.req.json()
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    const result = await client.query(
      `INSERT INTO users (firebase_uid, display_name, role, xp, level, streak, energy, last_active_date, updated_at, birth_year, parent_email)
       VALUES ($1, $2, $3, 0, 1, 0, 5, CURRENT_DATE, NOW(), $4, $5)
       ON CONFLICT (firebase_uid) DO UPDATE SET display_name = $2, updated_at = NOW()
       RETURNING *`,
      [firebaseUid, display_name || 'User', role || 'student', birth_year || null, parent_email || null]
    )
    return c.json({ error: false, user: result.rows[0] }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

router.get('/auth/me', requireAuth, async (c) => {
  const firebaseUid = c.get('firebaseUid')
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    const result = await client.query('SELECT * FROM users WHERE firebase_uid = $1', [firebaseUid])
    if (result.rows.length === 0) {
      return c.json({ error: true, message: 'User not found. Register first.' }, 404)
    }
    return c.json({ error: false, user: result.rows[0] }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

router.delete('/auth/me', requireAuth, async (c) => {
  const firebaseUid = c.get('firebaseUid')
  let client
  try {
    client = getClient(c.env)
    await client.connect()
    
    // Begin transaction for atomic account deletion
    await client.query('BEGIN')
    
    try {
      // 1. Get user id
      const userResult = await client.query('SELECT id FROM users WHERE firebase_uid = $1', [firebaseUid])
      if (userResult.rows.length === 0) {
        await client.query('ROLLBACK')
        return c.json({ error: true, message: 'User not found' }, 404)
      }
      const userId = userResult.rows[0].id
      
      // 2. Delete classes owned by this teacher (cascade deletes class members & assignments)
      await client.query('DELETE FROM classes WHERE owner_teacher_id = $1', [userId])
      
      // 3. Delete user row (cascade deletes progress, achievements, class_members)
      await client.query('DELETE FROM users WHERE id = $1', [userId])
      
      await client.query('COMMIT')
    } catch (txErr) {
      await client.query('ROLLBACK')
      throw txErr
    }
    
    return c.json({ error: false, message: 'User account and all related data deleted successfully' }, 200)
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500)
  } finally {
    if (client) c.executionCtx.waitUntil(client.end())
  }
})

export default router
