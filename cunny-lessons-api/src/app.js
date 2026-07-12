// app.js
import { Hono } from 'hono';
import { cors } from 'hono/cors';
import learningMaterialsRoutes from './routes/learningMaterialsRoutes.js';
import lessonsRoutes from './routes/lessonsRoutes.js';
import courseRoutes from './routes/courseRoutes.js';
import authRoutes from './routes/authRoutes.js';
import progressRoutes from './routes/progressRoutes.js';
import classRoutes from './routes/classRoutes.js';
import billingRoutes from './routes/billingRoutes.js';
import parentConsentRoutes from './routes/parentConsentRoutes.js';
import parentConsentWebRoutes from './routes/parentConsentWebRoutes.js';
import { runMigration } from './migrate.js';
import { getPreviewHtml } from './views/preview.js';
import { aiPathImages } from './assets/aiPathImages.js';

const app = new Hono();

app.get('/api/static/ai-path/:slug', async (c) => {
  const slugParam = c.req.param('slug');
  const slug = slugParam.endsWith('.png') ? slugParam.slice(0, -4) : slugParam;
  const base64Data = aiPathImages[slug];
  if (!base64Data) {
    return c.json({ error: true, message: 'Image not found' }, 404);
  }
  
  const binaryString = atob(base64Data);
  const bytes = new Uint8Array(binaryString.length);
  for (let i = 0; i < binaryString.length; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }
  
  return c.body(bytes.buffer, 200, {
    'Content-Type': 'image/png',
    'Cache-Control': 'public, max-age=31536000',
  });
});

app.use('*', cors({
  origin: '*',
}));

app.onError((err, c) => {
  console.error('API Error:', err);
  return c.json({ status: 'error', message: err.message || 'Internal Server Error' }, 500);
});

app.get('/', (c) => {
  const accept = c.req.header('Accept') || '';
  if (accept.includes('text/html')) {
    const url = new URL(c.req.url);
    const apiUrl = `${url.protocol}//${url.host}`;
    return c.html(getPreviewHtml(apiUrl));
  }
  return c.json({
    status: 'API is running on Cloudflare Workers',
    message: 'Welcome to CUNNY Content API',
    routes: [
      'GET /preview (HTML preview dashboard)',
      'GET /api/learning-materials',
      'GET /api/learning-materials/:id',
      'POST /api/learning-materials',
      'PUT /api/learning-materials/:id',
      'DELETE /api/learning-materials/:id',
      'GET /api/categories?language=id',
      'GET /api/categories/:slug/courses?language=id',
      'GET /api/courses?category=ai&language=id',
      'GET /api/courses/:slug/journey?language=id',
      'GET /api/courses/:slug/lessons?language=id',
      'GET /api/lessons?language=id',
      'GET /api/lessons/:slug?language=id',
      'POST /api/lessons',
      'PUT /api/lessons/:slug',
      'DELETE /api/lessons/:slug',
      'POST /api/auth/register',
      'GET /api/auth/me',
      'GET /api/me/progress',
      'PUT /api/me/progress',
      'POST /api/classes',
      'POST /api/classes/join',
      'GET /api/classes',
      'GET /api/classes/:id/progress',
      'POST /api/classes/:id/assignments',
      'GET /api/classes/:id/assignments',
      'POST /api/billing/verify',
      'GET /api/billing/status',
      'POST /api/auth/parent-consent',
      'GET /parent-consent/:token (HTML)',
      'POST /parent-consent/:token/create (HTML)',
      'GET /api/migrate',
    ],
  });
});

app.get('/preview', (c) => {
  const url = new URL(c.req.url);
  const apiUrl = `${url.protocol}//${url.host}`;
  return c.html(getPreviewHtml(apiUrl));
});

app.get('/api/migrate', async (c) => {
  const result = await runMigration(c);
  if (result.ok) return c.json({ error: false, message: 'Migration complete' }, 200);
  return c.json({ error: true, message: result.error }, 500);
});

// TEMPORARY DEBUG ENDPOINT — remove after debugging sync issue
app.get('/api/debug/progress-check', async (c) => {
  const { getClient } = await import('./config/database.js');
  let client;
  try {
    client = getClient(c.env);
    await client.connect();
    const users = await client.query('SELECT id, firebase_uid, display_name, email, xp, level, streak, energy FROM users ORDER BY id');
    const progress = await client.query('SELECT p.*, u.display_name FROM progress p JOIN users u ON p.user_id = u.id ORDER BY p.completed_at DESC LIMIT 50');
    const achievements = await client.query('SELECT a.*, u.display_name FROM achievements a JOIN users u ON a.user_id = u.id ORDER BY a.unlocked_at DESC LIMIT 50');
    return c.json({
      total_users: users.rows.length,
      users: users.rows,
      total_progress: progress.rows.length,
      progress: progress.rows,
      total_achievements: achievements.rows.length,
      achievements: achievements.rows,
    }, 200);
  } catch (err) {
    return c.json({ error: true, message: err.message }, 500);
  } finally {
    if (client) c.executionCtx.waitUntil(client.end());
  }
});

app.route('/api', learningMaterialsRoutes);
app.route('/api', lessonsRoutes);
app.route('/api', courseRoutes);
app.route('/api', authRoutes);
app.route('/api', progressRoutes);
app.route('/api', classRoutes);
app.route('/api', billingRoutes);
app.route('/api', parentConsentRoutes);
app.route('', parentConsentWebRoutes);

export default app;
