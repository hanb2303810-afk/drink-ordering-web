import { useEffect, useMemo, useState } from 'react'
import { subscribeOrders } from '../api'
import type { OrderDto } from '../types'
import { fmtVnd, STATUS_LABEL } from './MenuPage'

/**
 * Trang "Theo dõi đơn": bảng tổng hợp toàn bộ đơn mới nhất, cập nhật realtime
 * qua cùng luồng SSE với trang khách (event "orders" của /api/stream/orders).
 * Khác với bảng nhỏ trong giỏ ở trang chủ: bảng này hiển thị cả đơn COMPLETED,
 * kèm giờ đặt và tổng tiền, dòng mới nhất lên đầu.
 */
export default function TrackPage() {
  const [orders, setOrders] = useState<OrderDto[]>([])

  useEffect(() => {
    let cancelled = false
    const unsubscribe = subscribeOrders((next) => {
      if (!cancelled) setOrders(next)
    })
    return () => {
      cancelled = true
      unsubscribe()
    }
  }, [])

  const sorted = useMemo(
    () => [...orders].sort((a, b) => b.orderId - a.orderId),
    [orders],
  )

  return (
    <div>
      <h1>Theo dõi đơn realtime</h1>
      <p className="muted small">
        Bảng tự cập nhật mỗi khi thread Barista đổi trạng thái đơn (SSE) — mới nhất lên đầu.
      </p>
      {sorted.length === 0 && (
        <p className="muted">Chưa có đơn nào. Hãy là khách đầu tiên!</p>
      )}
      {sorted.length > 0 && (
        <table className="table">
          <thead>
            <tr>
              <th>#</th>
              <th>Khách</th>
              <th>Món</th>
              <th>Trạng thái</th>
              <th>Giờ đặt</th>
              <th>Tổng</th>
            </tr>
          </thead>
          <tbody>
            {sorted.map((order) => (
              <tr key={order.orderId}>
                <td>
                  <strong>#{order.orderId}</strong>
                </td>
                <td>{order.customerName}</td>
                <td>
                  {order.items.map((item) => (
                    <div key={item.drinkId} className="muted small">
                      {item.quantity}× {item.drinkName}
                    </div>
                  ))}
                </td>
                <td>
                  <span className={`status status-${order.status.toLowerCase()}`}>
                    {STATUS_LABEL[order.status]}
                  </span>
                </td>
                <td className="muted small">
                  {new Date(order.createdAt).toLocaleTimeString('vi-VN')}
                </td>
                <td>{fmtVnd(order.total)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  )
}
