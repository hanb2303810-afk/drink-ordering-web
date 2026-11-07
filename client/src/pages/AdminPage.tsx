import { useEffect, useState } from 'react'
import {
  adminOrders,
  adminStats,
  adminUsers,
  changeRole,
  clearSession,
  createDrink,
  deleteDrink,
  updateDrink,
} from '../api'
import { fmtVnd } from './MenuPage'
import type { DrinkDto, DrinkRequest, OrderDto, Role, StatsDto, UserDto } from '../types'

/**
 * Trang quản trị 4 tab: menu, tài khoản (gán vai trò), đơn hàng, thống kê.
 * Toàn bộ API /api/admin/** bị server chặn bằng ROLE_ADMIN - route guard chỉ UX.
 */
export default function AdminPage() {
  const [tab, setTab] = useState<'drinks' | 'users' | 'orders' | 'stats'>('drinks')

  return (
    <div>
      <div className="tabs">
        {(
          [
            ['drinks', 'Menu đồ uống'],
            ['users', 'Tài khoản & vai trò'],
            ['orders', 'Đơn hàng'],
            ['stats', 'Thống kê'],
          ] as const
        ).map(([key, label]) => (
          <button
            key={key}
            className={`btn ${tab === key ? 'primary' : 'ghost'}`}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
        <span className="spacer" />
        <button
          className="btn ghost"
          onClick={() => {
            clearSession()
            window.location.href = '/login'
          }}
        >
          Đăng xuất
        </button>
      </div>
      {tab === 'drinks' && <DrinksTab />}
      {tab === 'users' && <UsersTab />}
      {tab === 'orders' && <OrdersTab />}
      {tab === 'stats' && <StatsTab />}
    </div>
  )
}

function DrinksTab() {
  const [drinks, setDrinks] = useState<DrinkDto[]>([])
  const [error, setError] = useState('')

  async function reload() {
    try {
      setDrinks(await (await fetch('/api/drinks')).json() as DrinkDto[])
    } catch (err) {
      setError((err as Error).message)
    }
  }

  useEffect(() => {
    reload()
  }, [])

  async function handleDelete(id: string) {
    setError('')
    try {
      await deleteDrink(id)
      await reload()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  return (
    <section>
      <h2>Menu</h2>
      <NewDrinkForm onDone={reload} />
      {error && <p className="error">{error}</p>}
      <table className="table">
        <thead>
          <tr>
            <th>Mã</th>
            <th>Tên</th>
            <th>Loại</th>
            <th>Giá</th>
            <th>Thời gian pha</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {drinks.map((drink) => (
            <tr key={drink.id}>
              <td>{drink.id}</td>
              <td>{drink.name}</td>
              <td>{drink.type === 'COFFEE' ? 'Cà phê' : 'Trà sữa'}</td>
              <td>{fmtVnd(drink.price)}</td>
              <td>{drink.prepTimeMs / 1000}s</td>
              <td className="row end">
                <EditDrinkButton drink={drink} onDone={reload} />
                <button className="btn danger" onClick={() => handleDelete(drink.id)}>
                  Xoá
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

function EditDrinkButton({ drink, onDone }: { drink: DrinkDto; onDone: () => Promise<void> }) {
  // Form sửa dùng chính DrinkForm, mở dạng <details> gọn thay vì modal riêng.
  return (
    <details>
      <summary className="btn ghost">Sửa</summary>
      <DrinkForm
        initial={drink}
        onSubmit={async (body) => {
          await updateDrink(drink.id, body)
          onDone()
        }}
      />
    </details>
  )
}

function NewDrinkForm({ onDone }: { onDone: () => Promise<void> }) {
  return (
    <div className="card">
      <DrinkForm
        initial={null}
        onSubmit={async (body) => {
          await createDrink(body)
          onDone()
        }}
      />
    </div>
  )
}

function DrinkForm({
  initial,
  onSubmit,
}: {
  initial: DrinkDto | null
  onSubmit: (body: DrinkRequest) => Promise<void>
}) {
  const [form, setForm] = useState<DrinkRequest>(
    initial
      ? {
          id: initial.id,
          name: initial.name,
          basePrice: initial.basePrice,
          type: initial.type,
          iced: initial.iced ?? undefined,
          extraShots: initial.extraShots ?? undefined,
          sugarLevel: initial.sugarLevel ?? undefined,
          toppingCount: initial.toppingCount ?? undefined,
        }
      : {
          id: '',
          name: '',
          basePrice: 25000,
          type: 'COFFEE',
          iced: true,
          extraShots: 0,
        },
  )
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function submit(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError('')
    try {
      await onSubmit(form)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form onSubmit={submit} className="drink-form">
      <div className="row">
        <div>
          <label>Mã</label>
          <input
            value={form.id}
            onChange={(e) => setForm({ ...form, id: e.target.value })}
            disabled={!!initial}
            placeholder="CF01"
          />
        </div>
        <div>
          <label>Tên</label>
          <input
            value={form.name}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
            placeholder="Cà phê sữa đá"
          />
        </div>
        <div>
          <label>Loại</label>
          <select
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value as DrinkRequest['type'] })}
          >
            <option value="COFFEE">Cà phê</option>
            <option value="MILK_TEA">Trà sữa</option>
          </select>
        </div>
        <div>
          <label>Giá cơ bản (đ)</label>
          <input
            type="number"
            value={form.basePrice}
            onChange={(e) => setForm({ ...form, basePrice: Number(e.target.value) })}
          />
        </div>
        {form.type === 'COFFEE' ? (
          <>
            <div>
              <label>Đá?</label>
              <input
                type="checkbox"
                checked={form.iced ?? false}
                onChange={(e) => setForm({ ...form, iced: e.target.checked })}
              />
            </div>
            <div>
              <label>Shot thêm</label>
              <input
                type="number"
                value={form.extraShots ?? 0}
                onChange={(e) => setForm({ ...form, extraShots: Number(e.target.value) })}
              />
            </div>
          </>
        ) : (
          <>
            <div>
              <label>Đường (%)</label>
              <input
                type="number"
                value={form.sugarLevel ?? 70}
                onChange={(e) => setForm({ ...form, sugarLevel: Number(e.target.value) })}
              />
            </div>
            <div>
              <label>Topping</label>
              <input
                type="number"
                value={form.toppingCount ?? 0}
                onChange={(e) => setForm({ ...form, toppingCount: Number(e.target.value) })}
              />
            </div>
          </>
        )}
      </div>
      <button className="btn primary" disabled={busy}>
        {initial ? 'Lưu thay đổi' : 'Thêm món'}
      </button>
      {error && <p className="error">{error}</p>}
    </form>
  )
}

function UsersTab() {
  const [users, setUsers] = useState<UserDto[]>([])
  const [error, setError] = useState('')

  async function reload() {
    try {
      setUsers(await adminUsers())
    } catch (err) {
      setError((err as Error).message)
      if ((err as Error).message.includes('đăng nhập')) {
        clearSession()
      }
    }
  }

  useEffect(() => {
    reload()
  }, [])

  async function handleRole(username: string, role: Role) {
    setError('')
    try {
      // Server chặn tự đổi vai trò của chính mình - UI disable luôn cho dễ hiểu.
      if (username === currentSelf()) {
        setError('Không được tự đổi vai trò của chính mình')
        return
      }
      await changeRole(username, role)
      await reload()
    } catch (err) {
      setError((err as Error).message)
    }
  }

  return (
    <section>
      <h2>Tài khoản</h2>
      {error && <p className="error">{error}</p>}
      <table className="table">
        <thead>
          <tr>
            <th>Tài khoản</th>
            <th>Vai trò</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.username}>
              <td>{user.username}</td>
              <td>
                <select
                  value={user.role}
                  disabled={user.username === currentSelf()}
                  onChange={(e) => handleRole(user.username, e.target.value as Role)}
                >
                  <option value="SERVER">Phục vụ</option>
                  <option value="BARISTA">Pha chế</option>
                  <option value="ADMIN">Quản trị</option>
                </select>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

function currentSelf(): string {
  try {
    const raw = localStorage.getItem('g04_login')
    return raw ? (JSON.parse(raw) as { username: string }).username : ''
  } catch {
    return ''
  }
}

function OrdersTab() {
  const [orders, setOrders] = useState<OrderDto[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    adminOrders()
      .then(setOrders)
      .catch((err) => setError((err as Error).message))
  }, [])

  return (
    <section>
      <h2>Đơn hàng</h2>
      {error && <p className="error">{error}</p>}
      <table className="table">
        <thead>
          <tr>
            <th>#</th>
            <th>Khách</th>
            <th>Món</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.orderId}>
              <td>{order.orderId}</td>
              <td>{order.customerName}</td>
              <td className="small">
                {order.items.map((item) => `${item.quantity}× ${item.drinkName}`).join(', ')}
              </td>
              <td>{fmtVnd(order.total)}</td>
              <td>
                <span className={`tag status-${order.status.toLowerCase()}`}>
                  {order.status === 'PENDING' ? 'Chờ pha' : order.status === 'PREPARING' ? 'Đang pha' : 'Xong'}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  )
}

function StatsTab() {
  const [stats, setStats] = useState<StatsDto | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    adminStats()
      .then(setStats)
      .catch((err) => setError((err as Error).message))
  }, [])

  if (error) {
    return <p className="error">{error}</p>
  }
  if (!stats) {
    return <p className="muted">Đang tải…</p>
  }

  const maxQty = Math.max(1, ...stats.topDrinks.map((d) => d.quantity))

  return (
    <section>
      <h2>Thống kê</h2>
      <div className="stat-grid">
        <div className="card stat">
          <div className="stat-num">{stats.totalOrders}</div>
          <div>Tổng đơn</div>
        </div>
        <div className="card stat">
          <div className="stat-num">{stats.pendingOrders}</div>
          <div>Chờ pha</div>
        </div>
        <div className="card stat">
          <div className="stat-num">{stats.preparingOrders}</div>
          <div>Đang pha</div>
        </div>
        <div className="card stat">
          <div className="stat-num">{stats.completedOrders}</div>
          <div>Hoàn thành</div>
        </div>
        <div className="card stat">
          <div className="stat-num">{fmtVnd(stats.revenue)}</div>
          <div>Doanh thu (đơn xong)</div>
        </div>
        <div className="card stat">
          <div className="stat-num">{stats.completedCount}</div>
          <div>Barista đang chạy</div>
        </div>
      </div>
      <h3>Món bán chạy</h3>
      {stats.topDrinks.length === 0 && <p className="muted">Chưa có dữ liệu.</p>}
      <div className="bars">
        {stats.topDrinks.map((drink) => (
          <div className="bar-row" key={drink.drinkId}>
            <span className="bar-label">{drink.drinkName}</span>
            <div className="bar-track">
              <div
                className="bar-fill"
                style={{ width: `${(drink.quantity / maxQty) * 100}%` }}
              />
            </div>
            <span className="bar-qty">
              {drink.quantity} phần · {fmtVnd(drink.revenue)}
            </span>
          </div>
        ))}
      </div>
    </section>
  )
}
