import { useEffect, useState } from 'react'
import { listOrders, subscribeOrders } from '../api'
import { fmtVnd, STATUS_LABEL } from './MenuPage'
import type { OrderDto, OrderStatus } from '../types'

const STATUS_LABEL_BARISTA: Record<OrderStatus, string> = STATUS_LABEL

/**
 * Quầy barista: đơn vào tự động từ OrderQueue của domain (mỗi Barista một thread).
 * Trang này chỉ hiển thị realtime; mở nhiều tab nhiều barista cùng xử lý
 * chứng minh đơn không trùng orderId và xử lý song song.
 */
export default function BaristaPage() {
  const [orders, setOrders] = useState<OrderDto[]>([])
  const [error, setError] = useState('')

  useEffect(() => {
    const unsubscribe = subscribeOrders(setOrders)
    listOrders().catch((err) => setError((err as Error).message))
    return () => unsubscribe()
  }, [])

  const columns: Array<{ status: OrderStatus; title: string }> = [
    { status: 'PENDING', title: 'Hàng đợi' },
    { status: 'PREPARING', title: 'Đang pha' },
    { status: 'COMPLETED', title: 'Xong' },
  ]

  return (
    <div>
      <h2>Quầy pha chế</h2>
      <p className="muted">
        Bảng realtime cập nhật mỗi khi thread Barista đổi trạng thái đơn.
        Mở thêm tab khác đăng nhập barista2 là có 2 barista xử lý song song.
      </p>
      {error && <p className="error">{error}</p>}
      <div className="kanban">
        {columns.map((column) => {
          const list = orders.filter((o) => o.status === column.status)
          return (
            <div className="kanban-col" key={column.status}>
              <h3>
                {column.title} <span className="count">{list.length}</span>
              </h3>
              {list.length === 0 && <p className="muted small">Trống</p>}
              {list.map((order) => (
                <div className="card order" key={order.orderId}>
                  <div className="order-id">#{order.orderId}</div>
                  <div>{order.customerName}</div>
                  <ul className="small">
                    {order.items.map((item) => (
                      <li key={item.drinkId}>
                        {item.quantity}× {item.drinkName}
                        <span>{fmtVnd(item.subtotal)}</span>
                      </li>
                    ))}
                  </ul>
                  <div className="row between">
                    <span className={`tag status-${order.status.toLowerCase()}`}>
                      {STATUS_LABEL_BARISTA[order.status]}
                    </span>
                    <span className="muted small">{fmtVnd(order.total)}</span>
                  </div>
                </div>
              ))}
            </div>
          )
        })}
      </div>
    </div>
  )
}
