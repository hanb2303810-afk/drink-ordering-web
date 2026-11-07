import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiLogin, saveSession } from '../api'

const PROMPTS: Record<string, string> = {
  barista1: 'Tài khoản barista (nhận đơn tự động từ hàng đợi)',
  phucvu1: 'Tài khoản nhân viên phục vụ (đặt đơn)',
  admin: 'Tài khoản quản trị (menu, vai trò, thống kê)',
}

/**
 * Đăng nhập nhân sự quán: gửi tài khoản seed, lấy JWT nhẹ mất vào localStorage.
 */
export default function LoginPage() {
  const [username, setUsername] = useState('phucvu1')
  const [password, setPassword] = useState('pass123')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const navigate = useNavigate()

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      const login = await apiLogin(username.trim(), password)
      saveSession(login)
      window.dispatchEvent(new Event('g04-session'))
      navigate(login.role === 'ADMIN' ? '/admin' : login.role === 'BARISTA' ? '/barista' : '/')
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login-wrap">
      <form className="card login-card" onSubmit={submit}>
        <h1>Đăng nhập</h1>
        <p className="muted">
          Khách vãng lai bấm vào "Bảng món" để đặt nước, không cần tài khoản.
        </p>

        <label>Tên đăng nhập</label>
        <input value={username} onChange={(e) => setUsername(e.target.value)} autoFocus />

        <label>Mật khẩu</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
        />

        <button className="btn primary" disabled={busy}>
          {busy ? 'Đang đăng nhập…' : 'Đăng nhập'}
        </button>

        {error && <p className="error">{error}</p>}

        <p className="muted small">{PROMPTS[username]}</p>

        <div className="hint">
          <span>Tài khoản demo:</span>
          <button type="button" onClick={() => { setUsername('phucvu1'); setPassword('pass123') }}>phucvu1/pass123</button>
          <button type="button" onClick={() => { setUsername('barista1'); setPassword('pass123') }}>barista1/pass123</button>
          <button type="button" onClick={() => { setUsername('admin'); setPassword('admin123') }}>admin/admin123</button>
        </div>
      </form>
    </div>
  )
}
