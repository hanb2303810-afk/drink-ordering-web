import { useEffect, useMemo, useState } from 'react'
import { createOrder, listDrinks, subscribeOrders } from '../api'
import type { DrinkDto, OrderDto, OrderStatus } from '../types'

const STATUS_LABEL: Record<OrderStatus, string> = {
  PENDING: 'Chờ pha chế',
  PREPARING: 'Đang pha chế',
  COMPLETED: 'Hoàn thành',
}

/**
 * Trang chính dành cho khách: chọn món, đặt nước, theo dõi đơn realtime qua SSE.
 * Sự kiện "orders" do server phát mỗi khi thread Barista đổi trạng thái đơn.
 */
export default function MenuPage() {
  const [drinks, setDrinks] = useState<DrinkDto[]>([])
  const [orders, setOrders] = useState<OrderDto[]>([])
  const [cart, setCart] = useState<Record<string, number>>({})
  const [customerName, setCustomerName] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let cancelled = false
    listDrinks()
      .then((menu) => { if (!cancelled) setDrinks(menu) })
      .catch((err) => { if (!cancelled) setError((err as Error).message) })
    const unsubscribe = subscribeOrders((next) => {
      if (!cancelled) setOrders(next)
    })
    return () => {
      cancelled = true
      unsubscribe()
    }
  }, [])

  const total = useMemo(
    () =>
      drinks.reduce(
        (sum, drink) => sum + (cart[drink.id] ?? 0) * drink.price,
        0,
      ),
    [drinks, cart],
  )

  function addToCart(id: string) {
    setCart((prev) => ({ ...prev, [id]: (prev[id] ?? 0) + 1 }))
  }

  function removeFromCart(id: string) {
    setCart((prev) => {
      const next = { ...prev }
      if (next[id] !== undefined) {
        next[id] = next[id] - 1
        if (next[id] <= 0) delete next[id]
      }
      return next
    })
  }

  async function submitOrder() {
    setBusy(true)
    setError('')
    setMessage('')
    try {
      const items = Object.entries(cart).map(([drinkId, quantity]) => ({
        drinkId,
        quantity,
      }))
      if (items.length === 0) {
        setError('Bạn chưa chọn món nào')
        return
      }
      if (!customerName.trim()) {
        setError('Vui lòng nhập tên để phục vụ gọi món')
        return
      }
      const order = await createOrder(customerName.trim(), items)
      setCart({})
      setMessage(`Đã đặt đơn #${order.orderId}. Đơn đang ở hàng đợi pha chế!`)
    } catch (err) {
      setError((err as Error).message)
    } finally {
      setBusy(false)
    }
  }

  const pending = orders.filter((o) => o.status !== 'COMPLETED').slice(0, 12)

  return (
    <div>
      <div className="board">
        <div className="menu">
          <h2>Bảng đồ uống</h2>
          {drinks.length === 0 && !error && <p className="muted">Đang tải menu…</p>}
          {error && <p className="error">{error}</p>}
          <div className="grid">
            {drinks.map((drink) => (
              <div className="card drink" key={drink.id}>
                <div className="drink-head">
                  <h3>{drink.name}</h3>
                  <span className="tag">{drink.type === 'COFFEE' ? 'Cà phê' : 'Trà sữa'}</span>
                </div>
                <p className="price">{fmtVnd(drink.price)}</p>
                <p className="muted small">
                  {drink.type === 'COFFEE'
                    ? `${drink.iced ? 'Đá' : 'Nóng'} · ${drink.extraShots} shot thêm`
                    : `${drink.sugarLevel}% đường · ${drink.toppingCount} topping`}
                  {' · '}pha ~{drink.prepTimeMs / 1000}s
                </p>
                <div className="row">
                  {((cart[drink.id] ?? 0) > 0) && (
                    <button className="btn ghost" onClick={() => removeFromCart(drink.id)}>
                      −
                    </button>
                  )}
                  <button className="btn" onClick={() => addToCart(drink.id)}>+ Thêm</button>
                  {((cart[drink.id] ?? 0) > 0) && (
                    <span className="muted small">x{cart[drink.id]}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="cart">
          <h2>Đơn của bạn</h2>
          <label>Tên khách</label>
          <input
            value={customerName}
            onChange={(e) => setCustomerName(e.target.value)}
            placeholder="VD: Duy"
          />
          {Object.keys(cart).length === 0 && (
            <p className="muted">Chưa chọn món nào.</p>
          )}
          <ul>
            {drinks
              .filter((d) => (cart[d.id] ?? 0) > 0)
              .map((d) => (
                <li key={d.id}>
                  {cart[d.id]} × {d.name}
                  <span>{fmtVnd(cart[d.id] * d.price)}</span>
                </li>
              ))}
          </ul>
          <div className="total">
            Tổng: <strong>{fmtVnd(total)}</strong>
          </div>
          <button
            className="btn primary"
            disabled={busy || Object.keys(cart).length === 0}
            onClick={submitOrder}
          >
            {busy ? 'Đang gửi…' : 'Đặt nước'}
          </button>
          {message && <p className="ok">{message}</p>}
          {error && <p className="error">{error}</p>}
        </div>
      </div>

      <section className="tracking">
        <h2>Theo dõi pha chế <span className="muted small">(realtime qua SSE)</span></h2>
        <div className="panel">
          {pending.length === 0 ? (
            <p className="muted">Chưa có đơn nào đang xử lý. Hãy là khách đầu tiên!</p>
          ) : (
            <div className="orders-line">
              {pending.map((order) => (
                <div
                  className={`order-chip ${order.status.toLowerCase()}`}
                  key={order.orderId}
                >
                  <div className="order-id">#{order.orderId} · {order.customerName}</div>
                  <div className="order-items">
                    {order.items.map((item) => (
                      <span key={item.drinkId}>
                        {item.quantity}× {item.drinkName}
                      </span>
                    ))}
                  </div>
                  <div className={`status status-${order.status.toLowerCase()}`}>
                    {STATUS_LABEL[order.status]}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

export function fmtVnd(value: number) {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
}

export { STATUS_LABEL }
