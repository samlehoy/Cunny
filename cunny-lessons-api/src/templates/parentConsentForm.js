// templates/parentConsentForm.js

/**
 * Render the parent consent form HTML page.
 * @param {string} token - Consent request token
 * @param {string} childName - Child's display name
 * @param {string|null} errorMessage - Optional validation error to display
 * @returns {string} Full HTML page
 */
export function renderConsentForm(token, childName, errorMessage = null) {
  return `<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Buat Akun — Cunny</title>
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
      overflow: hidden;
      box-shadow: 0 8px 32px rgba(0,0,0,0.4);
    }
    .card-header {
      background: linear-gradient(135deg, #9b4dca, #7b2faa);
      padding: 28px 32px;
      text-align: center;
    }
    .card-header h1 {
      font-size: 24px;
      color: #fff;
      letter-spacing: 1px;
    }
    .card-body {
      padding: 32px;
    }
    .card-body h2 {
      font-size: 20px;
      color: #e8b4f8;
      margin-bottom: 8px;
    }
    .card-body .subtitle {
      font-size: 14px;
      color: #a89db8;
      margin-bottom: 24px;
      line-height: 1.5;
    }
    .error-box {
      background-color: rgba(220, 53, 69, 0.15);
      border: 1px solid rgba(220, 53, 69, 0.4);
      color: #ff8a95;
      padding: 12px 16px;
      border-radius: 10px;
      font-size: 14px;
      margin-bottom: 20px;
      line-height: 1.4;
    }
    .form-group {
      margin-bottom: 18px;
    }
    .form-group label {
      display: block;
      font-size: 13px;
      color: #a89db8;
      margin-bottom: 6px;
      font-weight: 500;
    }
    .form-group input {
      width: 100%;
      padding: 12px 14px;
      background-color: #231e30;
      border: 1px solid #2d2840;
      border-radius: 10px;
      color: #f5f0ff;
      font-size: 15px;
      transition: border-color 0.2s;
      outline: none;
    }
    .form-group input:focus {
      border-color: #9b4dca;
    }
    .form-group input::placeholder {
      color: #6b5f7d;
    }
    .btn-submit {
      width: 100%;
      padding: 14px;
      background: linear-gradient(135deg, #9b4dca, #7b2faa);
      color: #fff;
      border: none;
      border-radius: 12px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      letter-spacing: 0.5px;
      margin-top: 8px;
      transition: opacity 0.2s;
    }
    .btn-submit:hover { opacity: 0.9; }
    .btn-submit:active { opacity: 0.8; }
    .footer-info {
      padding: 20px 32px;
      border-top: 1px solid #2d2840;
      text-align: center;
    }
    .footer-info p {
      font-size: 12px;
      color: #6b5f7d;
      line-height: 1.5;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="card-header">
      <h1>🐰 Cunny</h1>
    </div>
    <div class="card-body">
      <h2>Buat Akun untuk ${escapeHtml(childName)}</h2>
      <p class="subtitle">
        Sebagai orang tua/wali, Anda dapat membuat akun Cunny untuk anak Anda.
        Isi formulir di bawah ini untuk membuatkan akun.
      </p>
      ${errorMessage ? `<div class="error-box">${escapeHtml(errorMessage)}</div>` : ''}
      <form method="POST" action="/parent-consent/${token}/create">
        <div class="form-group">
          <label for="child_name">Nama Anak</label>
          <input type="text" id="child_name" name="child_name" value="${escapeHtml(childName)}" required />
        </div>
        <div class="form-group">
          <label for="email">Email Anak</label>
          <input type="email" id="email" name="email" placeholder="email.anak@contoh.com" required />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input type="password" id="password" name="password" minlength="8" placeholder="Minimal 8 karakter" required />
        </div>
        <div class="form-group">
          <label for="confirm_password">Konfirmasi Password</label>
          <input type="password" id="confirm_password" name="confirm_password" minlength="8" placeholder="Ulangi password" required />
        </div>
        <button type="submit" class="btn-submit">Buat Akun</button>
      </form>
    </div>
    <div class="footer-info">
      <p>Dengan membuat akun, Anda menyetujui bahwa anak Anda menggunakan Cunny untuk belajar.</p>
    </div>
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
