// services/emailService.js

/**
 * Send parent consent email via Resend API.
 * @param {object} env - Worker environment bindings
 * @param {object} params
 * @param {string} params.parentEmail - Parent's email address
 * @param {string} params.childName - Child's display name
 * @param {string} params.consentLink - Full URL to the consent form
 * @throws {Error} on Resend API failure
 */
export async function sendParentConsentEmail(env, { parentEmail, childName, consentLink }) {
  const html = `
<!DOCTYPE html>
<html lang="id">
<head><meta charset="UTF-8"></head>
<body style="margin:0;padding:0;background-color:#1a1625;font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;">
  <table width="100%" cellpadding="0" cellspacing="0" style="background-color:#1a1625;padding:40px 20px;">
    <tr><td align="center">
      <table width="560" cellpadding="0" cellspacing="0" style="background-color:#231e30;border-radius:16px;overflow:hidden;">
        <!-- Header -->
        <tr><td style="background:linear-gradient(135deg,#9b4dca,#7b2faa);padding:32px 40px;text-align:center;">
          <h1 style="margin:0;font-size:28px;color:#ffffff;letter-spacing:1px;">🐰 Cunny</h1>
        </td></tr>
        <!-- Body -->
        <tr><td style="padding:40px;">
          <p style="margin:0 0 16px;font-size:16px;color:#f5f0ff;line-height:1.6;">
            Halo,
          </p>
          <p style="margin:0 0 16px;font-size:16px;color:#f5f0ff;line-height:1.6;">
            <strong style="color:#e8b4f8;">${childName}</strong> ingin menggunakan <strong style="color:#e8b4f8;">Cunny</strong>,
            aplikasi belajar interaktif untuk anak-anak. Karena usianya masih di bawah 13 tahun,
            kami membutuhkan izin Anda sebagai orang tua/wali untuk membuat akun.
          </p>
          <p style="margin:0 0 24px;font-size:16px;color:#f5f0ff;line-height:1.6;">
            Silakan klik tombol di bawah untuk meninjau dan membuat akun:
          </p>
          <!-- CTA Button -->
          <table width="100%" cellpadding="0" cellspacing="0">
            <tr><td align="center" style="padding:8px 0 32px;">
              <a href="${consentLink}"
                 style="display:inline-block;background:linear-gradient(135deg,#9b4dca,#7b2faa);
                        color:#ffffff;text-decoration:none;font-size:16px;font-weight:600;
                        padding:14px 40px;border-radius:12px;letter-spacing:0.5px;">
                Buat Akun untuk ${childName}
              </a>
            </td></tr>
          </table>
          <p style="margin:0 0 8px;font-size:13px;color:#a89db8;line-height:1.5;">
            ⏳ Link ini berlaku selama <strong>7 hari</strong> sejak email ini dikirim.
          </p>
          <p style="margin:0;font-size:13px;color:#a89db8;line-height:1.5;">
            Jika Anda tidak merasa meminta ini, abaikan email ini.
          </p>
        </td></tr>
        <!-- Footer -->
        <tr><td style="padding:24px 40px;border-top:1px solid #2d2840;text-align:center;">
          <p style="margin:0;font-size:12px;color:#6b5f7d;">
            © ${new Date().getFullYear()} Cunny — Belajar dengan seru 🎮
          </p>
        </td></tr>
      </table>
    </td></tr>
  </table>
</body>
</html>`;

  const res = await fetch('https://api.resend.com/emails', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${env.RESEND_API_KEY}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      from: 'Cunny <noreply@eleonorez.xyz>',
      to: [parentEmail],
      subject: `Cunny — Izin Pembuatan Akun untuk ${childName}`,
      html,
    }),
  });

  if (!res.ok) {
    const body = await res.text();
    throw new Error(`Resend API error (${res.status}): ${body}`);
  }

  return await res.json();
}
