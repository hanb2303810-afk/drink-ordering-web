// Types đồng bộ với DTO server (com.group04.dto.DtoMapper) - sửa server nhớ sửa lại đây.

export type OrderStatus = 'PENDING' | 'PREPARING' | 'COMPLETED'

export type Role = 'SERVER' | 'BARISTA' | 'ADMIN'

export interface DrinkDto {
  id: string
  name: string
  basePrice: number
  price: number
  type: 'COFFEE' | 'MILK_TEA'
  iced: boolean | null
  extraShots: number | null
  sugarLevel: number | null
  toppingCount: number | null
  prepTimeMs: number
}

export interface DrinkRequest {
  id: string
  name: string
  basePrice: number
  type: 'COFFEE' | 'MILK_TEA'
  iced?: boolean
  extraShots?: number
  sugarLevel?: number
  toppingCount?: number
}

export interface OrderItemDto {
  drinkId: string
  drinkName: string
  quantity: number
  unitPrice: number
  subtotal: number
}

export interface OrderDto {
  orderId: number
  customerName: string
  status: OrderStatus
  createdAt: string
  total: number
  items: OrderItemDto[]
}

export interface UserDto {
  username: string
  role: Role
}

export interface LoginResponse {
  token: string
  username: string
  role: Role
}

export interface StatsDto {
  totalOrders: number
  pendingOrders: number
  preparingOrders: number
  completedOrders: number
  revenue: number
  completedCount: number
  topDrinks: Array<{
    drinkId: string
    drinkName: string
    quantity: number
    revenue: number
  }>
}
