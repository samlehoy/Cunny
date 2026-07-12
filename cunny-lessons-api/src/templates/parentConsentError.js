// templates/parentConsentError.js

/**
 * Render the error page for invalid/expired consent links.
 * @param {string} message - Error message to display
 * @returns {string} Full HTML page
 */
export function renderConsentError(message) {
  return `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Link Tidak Valid — Cunny</title>
  <style>
    *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
    body {
      background-color: #0f0b1a;
      font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
      color: #f5f0ff;
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 24px;
    }
    .card {
      background-color: #1a1625;
      border-radius: 20px;
      width: 100%;
      max-width: 440px;
      text-align: center;
      padding: 48px 32px;
      box-shadow: 0 8px 32px rgba(0,0,0,0.4);
    }
    .emoji { font-size: 64px; margin-bottom: 20px; }
    .card h1 {
      font-size: 24px;
      color: #e8b4f8;
      margin-bottom: 16px;
    }
    .card p {
      font-size: 16px;
      color: #a89db8;
      line-height: 1.6;
    }
    .footer {
      margin-top: 32px;
      font-size: 12px;
      color: #6b5f7d;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="emoji">😔</div>
    <h1>Link Tidak Valid</h1>
    <p>${escapeHtml(message)}</p>
    <p class="footer">Hubungi kami jika Anda membutuhkan bantuan.</p>
  </div>
</body>
</html>`;
}

/** Escape HTML special characters to prevent XSS */
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
