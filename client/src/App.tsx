import { useEffect, useState } from 'react'
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { clearSession, currentLogin } from './api'
import LoginPage from './pages/LoginPage'
import MenuPage from './pages/MenuPage'
import BaristaPage from './pages/BaristaPage'
import AdminPage from './pages/AdminPage'
import type { LoginResponse } from './types'

/**
 * Router ứng dụng. Route guard chỉ là UX - quyền thực sự do server chặn
 * (AuthInterceptor), client điều hướng về login khi 401 từng bước xem xét.
 */
function RequireRole({ role, children }: { role?: string; children: React.ReactNode }) {
  const login = currentLogin()
  const location = useLocation()
  if (!login) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }
  if (role === 'ADMIN' && login.role !== 'ADMIN') {
    return <Navigate to="/login" replace />
  }
  if (role === 'BARISTA' && !(login.role === 'BARISTA' || login.role === 'ADMIN')) {
    return <Navigate to="/login" replace />
  }
  return children
}

function Header() {
  const [login, setLogin] = useState<LoginResponse | null>(currentLogin())
  useEffect(() => {
    // Đồng bộ header khi trang khác đổi phiên đăng nhập (custom window event).
    const handler = () => setLogin(currentLogin())
    window.addEventListener('g04-session', handler)
    return () => window.removeEventListener('g04-session', handler)
  }, [])

  function logout() {
    clearSession()
    setLogin(null)
    window.location.href = '/'
  }

  const baristaVisible = login && (login.role === 'BARISTA' || login.role === 'ADMIN')

  return (
    <header className="topbar">
      <Link to="/" className="brand">☕ Quán Zero-Four — Đặt nước realtime</Link>
      <nav>
        <Link to="/">Bảng món</Link>
        <Link to="/track">Theo dõi đơn</Link>
        {baristaVisible && <Link to="/barista">Quầy barista</Link>}
        {login?.role === 'ADMIN' && <Link to="/admin">Quản trị</Link>}
      </nav>
      <div className="topbar-right">
        {login ? (
          <>
            <span className="who">
              {login.username} · {login.role}
            </span>
            <button className="btn ghost" onClick={logout}>Đăng xuất</button>
          </>
        ) : (
          <Link className="btn" to="/login">
            Đăng nhập
          </Link>
        )}
      </div>
    </header>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Header />
      <main className="page">
        <Routes>
          <Route path="/" element={<MenuPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/track" element={<TrackRedirect />} />
          <Route
            path="/barista"
            element={
              <RequireRole role="BARISTA">
                <BaristaPage />
              </RequireRole>
            }
          />
          <Route
            path="/admin"
            element={
              <RequireRole role="ADMIN">
                <AdminPage />
              </RequireRole>
            }
          />
        </Routes>
      </main>
    </BrowserRouter>
  )
}

/** /track không có nội dung riêng: dẫn về trang chủ nơi có bảng theo dõi realtime. */
function TrackRedirect() {
  return <Navigate to="/" replace />
}
